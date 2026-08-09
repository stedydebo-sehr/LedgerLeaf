package com.ledgerleaf.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledgerleaf.domain.model.Expense
import com.ledgerleaf.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class HistoryGroup(val label: String) {
    DAY("Day"),
    WEEK("Week"),
    MONTH("Month"),
    YEAR("Year")
}

data class HistorySection(
    val key: String,
    val label: String,
    val expenses: List<Expense>,
    val totalMinor: Long
)

data class HistoryUiState(
    val group: HistoryGroup = HistoryGroup.DAY,
    val sections: List<HistorySection> = emptyList(),
    val expenseCount: Int = 0,
    val totalMinor: Long = 0L,
    val currencyCode: String = "INR"
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    repository: ExpenseRepository
) : ViewModel() {
    private val selectedGroup = MutableStateFlow(HistoryGroup.DAY)

    val uiState: StateFlow<HistoryUiState> = combine(
        repository.observeActiveExpenses(),
        selectedGroup
    ) { expenses, group ->
        val sections = buildSections(expenses, group)
        HistoryUiState(
            group = group,
            sections = sections,
            expenseCount = expenses.size,
            totalMinor = expenses.sumOf { it.amountMinor },
            currencyCode = expenses.firstOrNull()?.currencyCode ?: "INR"
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        HistoryUiState()
    )

    fun setGroup(group: HistoryGroup) {
        selectedGroup.value = group
    }
}

private fun buildSections(expenses: List<Expense>, group: HistoryGroup): List<HistorySection> {
    val zone = ZoneId.systemDefault()
    val weekFields = WeekFields.of(Locale.getDefault())
    val grouped = linkedMapOf<String, MutableList<Expense>>()
    val labels = mutableMapOf<String, String>()

    expenses.forEach { expense ->
        val date = Instant.ofEpochMilli(expense.occurredAtEpochMillis)
            .atZone(zone)
            .toLocalDate()
        val key: String
        val label: String

        when (group) {
            HistoryGroup.DAY -> {
                key = date.toString()
                label = date.format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy"))
            }
            HistoryGroup.WEEK -> {
                val week = date.get(weekFields.weekOfWeekBasedYear())
                val weekYear = date.get(weekFields.weekBasedYear())
                key = "%04d-%02d".format(Locale.ROOT, weekYear, week)
                label = "Week $week, $weekYear"
            }
            HistoryGroup.MONTH -> {
                key = "%04d-%02d".format(Locale.ROOT, date.year, date.monthValue)
                label = date.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
            }
            HistoryGroup.YEAR -> {
                key = date.year.toString()
                label = date.year.toString()
            }
        }

        grouped.getOrPut(key) { mutableListOf() }.add(expense)
        labels[key] = label
    }

    return grouped.map { (key, items) ->
        HistorySection(
            key = key,
            label = labels.getValue(key),
            expenses = items,
            totalMinor = items.sumOf { it.amountMinor }
        )
    }
}
