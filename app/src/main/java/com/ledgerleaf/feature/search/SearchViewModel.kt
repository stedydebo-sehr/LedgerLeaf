package com.ledgerleaf.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledgerleaf.domain.model.Expense
import com.ledgerleaf.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class SearchDateFilter(val label: String) {
    ANY("Any date"),
    TODAY("Today"),
    THIS_WEEK("This week"),
    THIS_MONTH("This month"),
    THIS_YEAR("This year")
}

data class SearchFilters(
    val notesQuery: String = "",
    val categoryId: String? = null,
    val paymentMethodId: String? = null,
    val dateFilter: SearchDateFilter = SearchDateFilter.ANY,
    val minimumAmount: String = "",
    val maximumAmount: String = "",
    val favoritesOnly: Boolean = false,
    val recurringOnly: Boolean = false
) {
    val hasStructuredFilter: Boolean
        get() = categoryId != null ||
            paymentMethodId != null ||
            dateFilter != SearchDateFilter.ANY ||
            minimumAmount.isNotBlank() ||
            maximumAmount.isNotBlank() ||
            favoritesOnly ||
            recurringOnly

    val canSearch: Boolean
        get() = notesQuery.isNotBlank() || hasStructuredFilter
}

data class SearchChoice(val id: String, val label: String)

data class SearchUiState(
    val filters: SearchFilters = SearchFilters(),
    val results: List<Expense> = emptyList(),
    val categories: List<SearchChoice> = emptyList(),
    val paymentMethods: List<SearchChoice> = emptyList(),
    val resultTotalMinor: Long = 0L,
    val currencyCode: String = "INR",
    val amountError: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    repository: ExpenseRepository
) : ViewModel() {
    private val filters = MutableStateFlow(SearchFilters())

    val uiState: StateFlow<SearchUiState> = combine(
        repository.observeActiveExpenses(),
        filters
    ) { expenses, current ->
        val amountRange = parseAmountRange(current.minimumAmount, current.maximumAmount)
        val filtered = if (!current.canSearch || amountRange.error != null) {
            emptyList()
        } else {
            expenses.filter { expense -> matches(expense, current, amountRange.minMinor, amountRange.maxMinor) }
        }

        SearchUiState(
            filters = current,
            results = filtered,
            categories = expenses
                .map { SearchChoice(it.category.id, it.category.name) }
                .distinctBy { it.id }
                .sortedBy { it.label.lowercase() },
            paymentMethods = expenses
                .map { SearchChoice(it.paymentMethod.id, it.paymentMethod.name) }
                .distinctBy { it.id }
                .sortedBy { it.label.lowercase() },
            resultTotalMinor = filtered.sumOf { it.amountMinor },
            currencyCode = expenses.firstOrNull()?.currencyCode ?: "INR",
            amountError = amountRange.error
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        SearchUiState()
    )

    fun setNotesQuery(value: String) = update { copy(notesQuery = value) }
    fun setCategory(id: String?) = update { copy(categoryId = id) }
    fun setPaymentMethod(id: String?) = update { copy(paymentMethodId = id) }
    fun setDateFilter(value: SearchDateFilter) = update { copy(dateFilter = value) }
    fun setMinimumAmount(value: String) = update { copy(minimumAmount = sanitizeAmount(value)) }
    fun setMaximumAmount(value: String) = update { copy(maximumAmount = sanitizeAmount(value)) }
    fun setFavoritesOnly(value: Boolean) = update { copy(favoritesOnly = value) }
    fun setRecurringOnly(value: Boolean) = update { copy(recurringOnly = value) }

    fun clearFilters() {
        filters.value = SearchFilters()
    }

    private fun update(block: SearchFilters.() -> SearchFilters) {
        filters.value = filters.value.block()
    }
}

private data class AmountRange(val minMinor: Long?, val maxMinor: Long?, val error: String?)

private fun parseAmountRange(minimum: String, maximum: String): AmountRange {
    fun parse(value: String): Long? {
        if (value.isBlank()) return null
        return runCatching {
            BigDecimal(value).setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact()
        }.getOrNull()
    }

    val min = parse(minimum)
    val max = parse(maximum)
    val error = when {
        minimum.isNotBlank() && min == null -> "Enter a valid minimum amount."
        maximum.isNotBlank() && max == null -> "Enter a valid maximum amount."
        min != null && min < 0L -> "Minimum amount cannot be negative."
        max != null && max < 0L -> "Maximum amount cannot be negative."
        min != null && max != null && min > max -> "Minimum amount cannot exceed maximum amount."
        else -> null
    }
    return AmountRange(min, max, error)
}

private fun matches(
    expense: Expense,
    filters: SearchFilters,
    minMinor: Long?,
    maxMinor: Long?
): Boolean {
    if (filters.notesQuery.isNotBlank() && !expense.notes.contains(filters.notesQuery.trim(), ignoreCase = true)) return false
    if (filters.categoryId != null && expense.category.id != filters.categoryId) return false
    if (filters.paymentMethodId != null && expense.paymentMethod.id != filters.paymentMethodId) return false
    if (filters.favoritesOnly && !expense.isFavorite) return false
    if (filters.recurringOnly && !expense.isRecurring) return false
    if (minMinor != null && expense.amountMinor < minMinor) return false
    if (maxMinor != null && expense.amountMinor > maxMinor) return false
    if (!matchesDate(expense, filters.dateFilter)) return false
    return true
}

private fun matchesDate(expense: Expense, dateFilter: SearchDateFilter): Boolean {
    if (dateFilter == SearchDateFilter.ANY) return true
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    val expenseDate = Instant.ofEpochMilli(expense.occurredAtEpochMillis).atZone(zone).toLocalDate()

    return when (dateFilter) {
        SearchDateFilter.ANY -> true
        SearchDateFilter.TODAY -> expenseDate == today
        SearchDateFilter.THIS_WEEK -> {
            val start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val end = start.plusDays(6)
            !expenseDate.isBefore(start) && !expenseDate.isAfter(end)
        }
        SearchDateFilter.THIS_MONTH -> expenseDate.year == today.year && expenseDate.month == today.month
        SearchDateFilter.THIS_YEAR -> expenseDate.year == today.year
    }
}

private fun sanitizeAmount(value: String): String {
    val filtered = value.filter { it.isDigit() || it == '.' }
    val dot = filtered.indexOf('.')
    return if (dot < 0) filtered else filtered.substring(0, dot + 1) + filtered.substring(dot + 1).replace(".", "")
}
