package com.ledgerleaf.feature.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledgerleaf.domain.repository.ArchivedExpense
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
class ArchiveViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {
    val archivedExpenses: StateFlow<List<ArchivedExpense>> = repository.observeArchivedExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init { purgeExpired() }

    fun restore(id: String) = viewModelScope.launch { repository.restoreArchivedExpense(id) }

    private fun purgeExpired() = viewModelScope.launch {
        val cutoff = Instant.now().atZone(ZoneId.systemDefault()).minusMonths(18).toInstant().toEpochMilli()
        repository.purgeArchivedBefore(cutoff)
    }
}
