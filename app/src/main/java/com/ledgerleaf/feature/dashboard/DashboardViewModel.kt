package com.ledgerleaf.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledgerleaf.core.datastore.ThemeMode
import com.ledgerleaf.domain.model.AppPreferences
import com.ledgerleaf.domain.model.Expense
import com.ledgerleaf.domain.repository.ExpenseRepository
import com.ledgerleaf.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val expenses: ExpenseRepository,
    private val settings: SettingsRepository
) : ViewModel() {
    private val monthOffset = MutableStateFlow(0)

    val uiState: StateFlow<DashboardUiState> = combine(
        expenses.observeActiveExpenses(),
        settings.preferences,
        monthOffset
    ) { items, prefs, offset ->
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val anchor = today.plusMonths(offset.toLong())
        val period = currentLedgerPeriod(anchor, prefs.monthStartDay)
        val monthly = items.filter { expense ->
            val date = Instant.ofEpochMilli(expense.occurredAtEpochMillis).atZone(zone).toLocalDate()
            !date.isBefore(period.start) && !date.isAfter(period.end)
        }.sortedByDescending { it.occurredAtEpochMillis }
        val total = monthly.sumOf { it.amountMinor }
        val budget = prefs.monthlyBudgetMinor
        val income = prefs.monthlyIncomeMinor
        val remaining = budget?.minus(total)
        val savings = income?.minus(total)
        val favoriteCount = items.count { it.isFavorite }
        val recurringDueCount = items
            .filter { it.isRecurring && (it.recurringFrequency == "WEEKLY" || it.recurringFrequency == "MONTHLY") }
            .groupBy { expense ->
                listOf(
                    expense.category.id,
                    expense.subcategories.map { it.id }.sorted().joinToString(","),
                    expense.paymentMethod.id,
                    expense.notes.trim().lowercase(),
                    expense.recurringFrequency.orEmpty()
                ).joinToString("|")
            }
            .values
            .map { series -> series.maxBy { it.occurredAtEpochMillis } }
            .count { expense ->
                val occurred = Instant.ofEpochMilli(expense.occurredAtEpochMillis).atZone(zone).toLocalDate()
                val nextDue = if (expense.recurringFrequency == "WEEKLY") occurred.plusWeeks(1) else occurred.plusMonths(1)
                !today.isBefore(nextDue)
            }
        val frequentLabel = mostFrequentLabel(items)
        val previousPeriod = previousLedgerPeriod(period.start, prefs.monthStartDay)
        val previousPeriodHasExpenses = items.any { expense ->
            val date = Instant.ofEpochMilli(expense.occurredAtEpochMillis).atZone(zone).toLocalDate()
            !date.isBefore(previousPeriod.start) && !date.isAfter(previousPeriod.end)
        }

        DashboardUiState(
            periodTitle = period.start.format(DateTimeFormatter.ofPattern("MMMM yyyy")).uppercase(),
            periodLabel = periodLabel(period),
            monthTotalMinor = total,
            transactionCount = monthly.size,
            remainingBudgetMinor = remaining,
            savingsMinor = savings,
            budgetProgress = if (budget != null && budget > 0L) total.toFloat() / budget.toFloat() else null,
            recent = monthly.take(8),
            favoriteCount = favoriteCount,
            recurringDueCount = recurringDueCount,
            frequentLabel = frequentLabel,
            showMonthlyClosingBanner = previousPeriodHasExpenses,
            preferences = prefs
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        DashboardUiState()
    )

    fun previousMonth() { monthOffset.value -= 1 }
    fun nextMonth() { monthOffset.value += 1 }
    fun returnToCurrentMonth() { monthOffset.value = 0 }

    fun togglePaperMode(isCurrentlyDark: Boolean) {
        viewModelScope.launch {
            settings.setThemeMode(if (isCurrentlyDark) ThemeMode.LIGHT else ThemeMode.DARK)
        }
    }
}

private data class LedgerPeriod(val start: LocalDate, val end: LocalDate)

private fun currentLedgerPeriod(today: LocalDate, startDay: Int): LedgerPeriod {
    val safeDay = startDay.coerceIn(1, 28)
    val thisMonthStart = today.withDayOfMonth(safeDay)
    val start = if (today.isBefore(thisMonthStart)) thisMonthStart.minusMonths(1) else thisMonthStart
    return LedgerPeriod(start = start, end = start.plusMonths(1).minusDays(1))
}

private fun previousLedgerPeriod(currentStart: LocalDate, startDay: Int): LedgerPeriod {
    val safeDay = startDay.coerceIn(1, 28)
    val start = currentStart.minusMonths(1).withDayOfMonth(safeDay)
    return LedgerPeriod(start = start, end = currentStart.minusDays(1))
}

private fun periodLabel(period: LedgerPeriod): String {
    val start = DateTimeFormatter.ofPattern("dd MMM")
    val end = DateTimeFormatter.ofPattern("dd MMM")
    return "${period.start.format(start)} – ${period.end.format(end)}"
}

private fun mostFrequentLabel(expenses: List<Expense>): String? = expenses
    .groupingBy { expense ->
        buildString {
            append(expense.category.name)
            if (expense.subcategories.isNotEmpty()) {
                append(" · ")
                append(expense.subcategories.joinToString { it.name })
            }
        }
    }
    .eachCount()
    .maxByOrNull { it.value }
    ?.key

data class DashboardUiState(
    val periodTitle: String = "CURRENT LEDGER",
    val periodLabel: String = "Current month",
    val monthTotalMinor: Long = 0,
    val transactionCount: Int = 0,
    val remainingBudgetMinor: Long? = null,
    val savingsMinor: Long? = null,
    val budgetProgress: Float? = null,
    val recent: List<Expense> = emptyList(),
    val favoriteCount: Int = 0,
    val recurringDueCount: Int = 0,
    val frequentLabel: String? = null,
    val showMonthlyClosingBanner: Boolean = false,
    val preferences: AppPreferences = AppPreferences()
)
