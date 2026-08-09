package com.ledgerleaf.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledgerleaf.domain.model.AppPreferences
import com.ledgerleaf.domain.model.Expense
import com.ledgerleaf.domain.repository.ExpenseRepository
import com.ledgerleaf.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class DashboardViewModel @Inject constructor(expenses: ExpenseRepository, settings: SettingsRepository) : ViewModel() {
    val uiState: StateFlow<DashboardUiState> = combine(expenses.observeActiveExpenses(), settings.preferences) { items, prefs ->
        val month = YearMonth.now()
        val monthly = items.filter { YearMonth.from(Instant.ofEpochMilli(it.occurredAtEpochMillis).atZone(ZoneId.systemDefault())) == month }
        DashboardUiState(monthly.sumOf { it.amountMinor }, monthly.take(5), prefs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())
}

data class DashboardUiState(val monthTotalMinor: Long = 0, val recent: List<Expense> = emptyList(), val preferences: AppPreferences = AppPreferences())
