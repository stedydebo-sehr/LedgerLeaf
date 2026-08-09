package com.ledgerleaf.feature.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledgerleaf.domain.model.Expense
import com.ledgerleaf.domain.repository.ExpenseRepository
import com.ledgerleaf.feature.shared.patternKey
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class RecurringExpenseItem(
    val expense: Expense,
    val nextDueDate: LocalDate,
    val isDue: Boolean
)

data class RecurringUiState(
    val due: List<RecurringExpenseItem> = emptyList(),
    val upcoming: List<RecurringExpenseItem> = emptyList()
)

@HiltViewModel
class RecurringViewModel @Inject constructor(
    repository: ExpenseRepository
) : ViewModel() {
    val uiState: StateFlow<RecurringUiState> = repository.observeActiveExpenses()
        .map { expenses -> buildState(expenses) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecurringUiState())
}

private fun buildState(expenses: List<Expense>): RecurringUiState {
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)

    val items = expenses
        .filter { it.isRecurring && (it.recurringFrequency == "WEEKLY" || it.recurringFrequency == "MONTHLY") }
        .groupBy { it.patternKey() to it.recurringFrequency }
        .values
        .map { series -> series.maxBy { it.occurredAtEpochMillis } }
        .map { latest ->
            val occurred = Instant.ofEpochMilli(latest.occurredAtEpochMillis).atZone(zone).toLocalDate()
            val nextDue = when (latest.recurringFrequency) {
                "WEEKLY" -> occurred.plusWeeks(1)
                else -> occurred.plusMonths(1)
            }
            RecurringExpenseItem(latest, nextDue, !today.isBefore(nextDue))
        }
        .sortedBy { it.nextDueDate }

    return RecurringUiState(
        due = items.filter { it.isDue },
        upcoming = items.filterNot { it.isDue }
    )
}
