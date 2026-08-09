package com.ledgerleaf.feature.monthlyclosing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledgerleaf.domain.model.Expense
import com.ledgerleaf.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private data class ClosingPeriod(val start: LocalDate, val end: LocalDate)

data class MonthlyClosingUiState(
    val startDate: LocalDate = LocalDate.now().withDayOfMonth(1),
    val endDate: LocalDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()),
    val expenses: List<Expense> = emptyList(),
    val totalMinor: Long = 0L,
    val currencyCode: String = "INR"
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MonthlyClosingViewModel @Inject constructor(repository: ExpenseRepository) : ViewModel() {
    private val period = MutableStateFlow(defaultPeriod())

    val uiState: StateFlow<MonthlyClosingUiState> = period.flatMapLatest { selected ->
        val zone = ZoneId.systemDefault()
        val from = selected.start.atStartOfDay(zone).toInstant().toEpochMilli()
        val to = selected.end.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
        repository.observeActiveExpensesInRange(from, to).map { expenses ->
            MonthlyClosingUiState(
                startDate = selected.start,
                endDate = selected.end,
                expenses = expenses,
                totalMinor = expenses.sumOf { it.amountMinor },
                currencyCode = expenses.firstOrNull()?.currencyCode ?: "INR"
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MonthlyClosingUiState())

    fun setPeriod(start: LocalDate, end: LocalDate): Boolean {
        if (end.isBefore(start)) return false
        period.value = ClosingPeriod(start, end)
        return true
    }

    fun previousMonth() {
        val month = period.value.start.minusMonths(1).withDayOfMonth(1)
        period.value = ClosingPeriod(month, month.withDayOfMonth(month.lengthOfMonth()))
    }

    fun nextMonth() {
        val month = period.value.start.plusMonths(1).withDayOfMonth(1)
        period.value = ClosingPeriod(month, month.withDayOfMonth(month.lengthOfMonth()))
    }

    private companion object {
        fun defaultPeriod(): ClosingPeriod {
            val today = LocalDate.now()
            val start = today.withDayOfMonth(1)
            return ClosingPeriod(start, start.withDayOfMonth(start.lengthOfMonth()))
        }
    }
}
