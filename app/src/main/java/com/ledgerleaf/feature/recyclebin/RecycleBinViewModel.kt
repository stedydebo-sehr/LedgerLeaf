package com.ledgerleaf.feature.recyclebin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledgerleaf.domain.repository.DeletedExpense
import com.ledgerleaf.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class RecycleBinViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {
    val deletedExpenses: StateFlow<List<DeletedExpense>> = repository.observeDeletedExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init { purgeExpired() }

    fun restore(id: String) = viewModelScope.launch { repository.restoreExpense(id) }

    private fun purgeExpired() = viewModelScope.launch {
        val cutoff = Instant.now().atZone(ZoneId.systemDefault()).minusMonths(6).toInstant().toEpochMilli()
        repository.purgeDeletedBefore(cutoff)
    }
}
