package com.ledgerleaf.feature.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledgerleaf.domain.model.AppPreferences
import com.ledgerleaf.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class BudgetsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {
    val preferences: StateFlow<AppPreferences> = repository.preferences.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AppPreferences()
    )

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun saveBudget(text: String) = viewModelScope.launch {
        val value = parseMoney(text)
        if (value == null || value < 0L) {
            _message.value = "Enter a valid monthly budget."
            return@launch
        }
        repository.setMonthlyBudgetMinor(value)
        _message.value = "Monthly budget saved."
    }

    fun clearBudget() = viewModelScope.launch {
        repository.setMonthlyBudgetMinor(null)
        _message.value = "Monthly budget cleared."
    }

    fun saveIncome(text: String) = viewModelScope.launch {
        val value = parseMoney(text)
        if (value == null || value < 0L) {
            _message.value = "Enter a valid monthly income."
            return@launch
        }
        repository.setMonthlyIncomeMinor(value)
        _message.value = "Monthly income saved."
    }

    fun clearIncome() = viewModelScope.launch {
        repository.setMonthlyIncomeMinor(null)
        _message.value = "Monthly income cleared."
    }

    fun setMonthStartDay(day: Int) = viewModelScope.launch {
        repository.setMonthStartDay(day)
        _message.value = "Ledger start day saved."
    }

    fun clearMessage() {
        _message.value = null
    }

    private fun parseMoney(text: String): Long? = try {
        text.trim().toBigDecimal().movePointRight(2).longValueExact()
    } catch (_: Exception) {
        null
    }
}
