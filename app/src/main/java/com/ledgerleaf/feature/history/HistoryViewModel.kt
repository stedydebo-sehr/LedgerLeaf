package com.ledgerleaf.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledgerleaf.domain.model.Expense
import com.ledgerleaf.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HistoryViewModel @Inject constructor(repository: ExpenseRepository) : ViewModel() {
    val expenses: StateFlow<List<Expense>> = repository.observeActiveExpenses().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
