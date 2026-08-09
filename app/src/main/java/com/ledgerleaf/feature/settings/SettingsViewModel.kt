package com.ledgerleaf.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledgerleaf.core.datastore.ThemeMode
import com.ledgerleaf.domain.model.AppPreferences
import com.ledgerleaf.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {
    val preferences: StateFlow<AppPreferences> = repository.preferences.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), AppPreferences()
    )

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { repository.setThemeMode(mode) }
    fun setCurrencyCode(code: String) = viewModelScope.launch { repository.setCurrencyCode(code) }
    fun setMonthlyBudget(text: String) = viewModelScope.launch {
        val minor = text.toBigDecimalOrNull()?.movePointRight(2)?.longValueExactOrNull()
        repository.setMonthlyBudgetMinor(minor)
    }
    fun clearMonthlyBudget() = viewModelScope.launch { repository.setMonthlyBudgetMinor(null) }
    fun setMonthlyIncome(text: String) = viewModelScope.launch {
        val minor = text.toBigDecimalOrNull()?.movePointRight(2)?.longValueExactOrNull()
        repository.setMonthlyIncomeMinor(minor)
    }
    fun clearMonthlyIncome() = viewModelScope.launch { repository.setMonthlyIncomeMinor(null) }
    fun setMonthStartDay(day: Int) = viewModelScope.launch { repository.setMonthStartDay(day) }
}

private fun java.math.BigDecimal.longValueExactOrNull(): Long? = try { longValueExact() } catch (_: ArithmeticException) { null }
