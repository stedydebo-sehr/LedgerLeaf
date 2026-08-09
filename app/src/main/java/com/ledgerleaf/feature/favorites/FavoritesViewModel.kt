package com.ledgerleaf.feature.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledgerleaf.domain.model.Expense
import com.ledgerleaf.domain.repository.ExpenseRepository
import com.ledgerleaf.feature.shared.patternKey
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class FrequentExpensePattern(
    val representative: Expense,
    val useCount: Int
)

data class FavoritesUiState(
    val favorites: List<Expense> = emptyList(),
    val frequent: List<FrequentExpensePattern> = emptyList()
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    repository: ExpenseRepository
) : ViewModel() {
    val uiState: StateFlow<FavoritesUiState> = repository.observeActiveExpenses()
        .map { expenses ->
            val frequent = expenses
                .groupBy { it.patternKey() }
                .values
                .mapNotNull { matches ->
                    if (matches.size < 2) null else FrequentExpensePattern(
                        representative = matches.maxBy { it.occurredAtEpochMillis },
                        useCount = matches.size
                    )
                }
                .sortedWith(
                    compareByDescending<FrequentExpensePattern> { it.useCount }
                        .thenByDescending { it.representative.occurredAtEpochMillis }
                )
                .take(12)

            FavoritesUiState(
                favorites = expenses.filter { it.isFavorite },
                frequent = frequent
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FavoritesUiState())
}
