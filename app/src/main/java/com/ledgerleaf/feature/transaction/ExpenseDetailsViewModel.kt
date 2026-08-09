package com.ledgerleaf.feature.transaction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledgerleaf.domain.model.Expense
import com.ledgerleaf.domain.repository.ExpenseRepository
import com.ledgerleaf.domain.usecase.DeleteExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ExpenseDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: ExpenseRepository,
    private val deleteExpense: DeleteExpenseUseCase
) : ViewModel() {
    val expenseId: String = checkNotNull(savedStateHandle["expenseId"])

    private val _uiState = MutableStateFlow<ExpenseDetailsUiState>(ExpenseDetailsUiState.Loading)
    val uiState: StateFlow<ExpenseDetailsUiState> = _uiState

    private var deletionRequested = false

    init {
        viewModelScope.launch {
            repository.observeActiveExpenses().collect { expenses ->
                val expense = expenses.firstOrNull { it.id == expenseId }
                _uiState.value = when {
                    deletionRequested && expense == null -> ExpenseDetailsUiState.Deleted
                    expense != null -> ExpenseDetailsUiState.Ready(expense)
                    else -> ExpenseDetailsUiState.NotFound
                }
            }
        }
    }

    fun delete() {
        viewModelScope.launch {
            deletionRequested = true
            deleteExpense(expenseId)
                .onFailure {
                    deletionRequested = false
                    _uiState.value = ExpenseDetailsUiState.Error(it.message ?: "Unable to delete expense.")
                }
        }
    }
}

sealed interface ExpenseDetailsUiState {
    data object Loading : ExpenseDetailsUiState
    data class Ready(val expense: Expense) : ExpenseDetailsUiState
    data object NotFound : ExpenseDetailsUiState
    data object Deleted : ExpenseDetailsUiState
    data class Error(val message: String) : ExpenseDetailsUiState
}
