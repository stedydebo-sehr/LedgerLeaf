package com.ledgerleaf.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledgerleaf.core.datastore.ThemeMode
import com.ledgerleaf.domain.model.AppPreferences
import com.ledgerleaf.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val message: String? = null,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {
    val preferences: StateFlow<AppPreferences> = repository.preferences.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), AppPreferences()
    )

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { repository.setThemeMode(mode) }

    fun setCurrencyCode(code: String) = viewModelScope.launch {
        val normalized = code.trim().uppercase()
        if (normalized.length != 3 || normalized.any { !it.isLetter() }) {
            _uiState.value = SettingsUiState(error = "Currency must be a 3-letter code such as INR or USD.")
            return@launch
        }
        repository.setCurrencyCode(normalized)
        _uiState.value = SettingsUiState(message = "Currency saved.")
    }

    fun setMonthlyBudget(text: String) = viewModelScope.launch {
        val minor = parseMoney(text)
        if (minor == null || minor < 0) {
            _uiState.value = SettingsUiState(error = "Enter a valid budget amount.")
            return@launch
        }
        repository.setMonthlyBudgetMinor(minor)
        _uiState.value = SettingsUiState(message = "Monthly budget saved.")
    }

    fun clearMonthlyBudget() = viewModelScope.launch {
        repository.setMonthlyBudgetMinor(null)
        _uiState.value = SettingsUiState(message = "Monthly budget cleared.")
    }

    fun setMonthlyIncome(text: String) = viewModelScope.launch {
        val minor = parseMoney(text)
        if (minor == null || minor < 0) {
            _uiState.value = SettingsUiState(error = "Enter a valid income amount.")
            return@launch
        }
        repository.setMonthlyIncomeMinor(minor)
        _uiState.value = SettingsUiState(message = "Monthly income saved.")
    }

    fun clearMonthlyIncome() = viewModelScope.launch {
        repository.setMonthlyIncomeMinor(null)
        _uiState.value = SettingsUiState(message = "Monthly income cleared.")
    }

    fun setMonthStartDay(day: Int) = viewModelScope.launch { repository.setMonthStartDay(day) }
    fun setPdfIncludeTransactions(include: Boolean) = viewModelScope.launch { repository.setPdfIncludeTransactions(include) }
    fun setPdfIncludeNotes(include: Boolean) = viewModelScope.launch { repository.setPdfIncludeNotes(include) }

    fun clearFeedback() {
        _uiState.value = SettingsUiState()
    }

    private fun parseMoney(text: String): Long? {
        if (text.isBlank()) return null
        return text.toBigDecimalOrNull()?.movePointRight(2)?.longValueExactOrNull()
    }
}

private fun java.math.BigDecimal.longValueExactOrNull(): Long? = try { longValueExact() } catch (_: ArithmeticException) { null }
