package com.ledgerleaf.feature.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledgerleaf.domain.model.AppPreferences
import com.ledgerleaf.domain.model.Category
import com.ledgerleaf.domain.model.PaymentMethod
import com.ledgerleaf.domain.model.Subcategory
import com.ledgerleaf.domain.repository.CategoryRepository
import com.ledgerleaf.domain.repository.NewExpense
import com.ledgerleaf.domain.repository.PaymentMethodRepository
import com.ledgerleaf.domain.repository.SettingsRepository
import com.ledgerleaf.domain.usecase.AddExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val categoriesRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository,
    private val paymentMethodsRepository: PaymentMethodRepository,
    private val addExpense: AddExpenseUseCase
) : ViewModel() {
    val categories: StateFlow<List<Category>> =
        categoriesRepository.observeCategories()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val paymentMethods: StateFlow<List<PaymentMethod>> =
        paymentMethodsRepository.observeActivePaymentMethods()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val preferences: StateFlow<AppPreferences> =
        settingsRepository.preferences
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppPreferences())

    private val selectedCategoryId = MutableStateFlow<String?>(null)

    val subcategories: StateFlow<List<Subcategory>> =
        selectedCategoryId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else categoriesRepository.observeSubcategories(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState

    init {
        viewModelScope.launch {
            categoriesRepository.ensureSystemDefaults()
            paymentMethodsRepository.ensureSystemDefaults()
        }
    }

    fun selectCategory(id: String) {
        selectedCategoryId.value = id
    }

    fun addCustomCategory(name: String) = viewModelScope.launch {
        runCatching { categoriesRepository.addCustomCategory(name) }
            .onSuccess { selectedCategoryId.value = it }
            .onFailure { _saveState.value = SaveState.Error(it.message ?: "Unable to add category.") }
    }

    fun save(submission: ExpenseEditorSubmission) {
        viewModelScope.launch {
            val input = submission.toNewExpense(preferences.value.currencyCode)
            if (input == null) return@launch
            val result = addExpense(input)
            _saveState.value = result.fold(
                onSuccess = { SaveState.Saved },
                onFailure = { SaveState.Error(it.message ?: "Unable to save expense.") }
            )
        }
    }

    private fun ExpenseEditorSubmission.toNewExpense(currencyCode: String): NewExpense? {
        val amountMinor = amount.toBigDecimalOrNull()
            ?.movePointRight(2)
            ?.let { runCatching { it.longValueExact() }.getOrNull() }

        if (amountMinor == null || amountMinor <= 0) {
            _saveState.value = SaveState.Error("Enter a valid amount greater than zero.")
            return null
        }
        if (categoryId.isNullOrBlank()) {
            _saveState.value = SaveState.Error("Select a category.")
            return null
        }
        if (paymentMethodId.isNullOrBlank()) {
            _saveState.value = SaveState.Error("Select a payment method.")
            return null
        }
        if (notes.isBlank()) {
            _saveState.value = SaveState.Error("Detailed notes are mandatory.")
            return null
        }

        val occurredAt = parseOccurredAt(dateText, timeText)
        if (occurredAt == null) {
            _saveState.value = SaveState.Error("Use date YYYY-MM-DD and time HH:MM.")
            return null
        }

        return NewExpense(
            amountMinor = amountMinor,
            currencyCode = currencyCode,
            categoryId = categoryId,
            paymentMethodId = paymentMethodId,
            subcategoryIds = subcategoryIds,
            notes = notes,
            occurredAtEpochMillis = occurredAt,
            isFavorite = favorite,
            isRecurring = recurring,
            recurringFrequency = recurringFrequency
        )
    }

    fun consumeSaved() {
        _saveState.value = SaveState.Idle
    }
}

internal fun parseOccurredAt(dateText: String, timeText: String): Long? =
    runCatching {
        val date = LocalDate.parse(dateText)
        val time = LocalTime.parse(timeText)
        LocalDateTime.of(date, time)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }.getOrNull()

sealed interface SaveState {
    data object Idle : SaveState
    data object Saved : SaveState
    data class Error(val message: String) : SaveState
}
