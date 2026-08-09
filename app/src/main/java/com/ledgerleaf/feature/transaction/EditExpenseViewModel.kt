package com.ledgerleaf.feature.transaction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledgerleaf.domain.model.Category
import com.ledgerleaf.domain.model.Expense
import com.ledgerleaf.domain.model.PaymentMethod
import com.ledgerleaf.domain.model.Subcategory
import com.ledgerleaf.domain.repository.CategoryRepository
import com.ledgerleaf.domain.repository.NewExpense
import com.ledgerleaf.domain.repository.PaymentMethodRepository
import com.ledgerleaf.domain.usecase.GetExpenseUseCase
import com.ledgerleaf.domain.usecase.UpdateExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
class EditExpenseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val getExpense: GetExpenseUseCase,
    private val updateExpense: UpdateExpenseUseCase
) : ViewModel() {
    private val expenseId: String = checkNotNull(savedStateHandle["expenseId"])

    val categories: StateFlow<List<Category>> =
        categoryRepository.observeCategories()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val paymentMethods: StateFlow<List<PaymentMethod>> =
        paymentMethodRepository.observeActivePaymentMethods()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val selectedCategoryId = MutableStateFlow<String?>(null)

    val subcategories: StateFlow<List<Subcategory>> =
        selectedCategoryId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else categoryRepository.observeSubcategories(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _expense = MutableStateFlow<Expense?>(null)
    val expense: StateFlow<Expense?> = _expense

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState

    init {
        viewModelScope.launch {
            categoryRepository.ensureSystemDefaults()
            paymentMethodRepository.ensureSystemDefaults()
            val loaded = getExpense(expenseId)
            _expense.value = loaded
            selectedCategoryId.value = loaded?.category?.id
            if (loaded == null) {
                _saveState.value = SaveState.Error("Expense not found.")
            }
        }
    }

    fun selectCategory(id: String) {
        selectedCategoryId.value = id
    }

    fun addCustomCategory(name: String) = viewModelScope.launch {
        runCatching { categoryRepository.addCustomCategory(name) }
            .onSuccess { selectedCategoryId.value = it }
            .onFailure { _saveState.value = SaveState.Error(it.message ?: "Unable to add category.") }
    }

    fun save(submission: ExpenseEditorSubmission) {
        val current = _expense.value ?: return
        viewModelScope.launch {
            val input = submission.toNewExpense(current.currencyCode) ?: return@launch
            _saveState.value = updateExpense(expenseId, input).fold(
                onSuccess = { SaveState.Saved },
                onFailure = { SaveState.Error(it.message ?: "Unable to update expense.") }
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
