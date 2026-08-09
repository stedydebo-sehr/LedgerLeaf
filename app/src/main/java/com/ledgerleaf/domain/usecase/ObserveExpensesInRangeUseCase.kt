package com.ledgerleaf.domain.usecase

import com.ledgerleaf.domain.model.Expense
import com.ledgerleaf.domain.repository.ExpenseRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveExpensesInRangeUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    operator fun invoke(fromEpochMillis: Long, toEpochMillis: Long): Flow<List<Expense>> {
        require(toEpochMillis >= fromEpochMillis) { "End date must not be before start date." }
        return repository.observeActiveExpensesInRange(fromEpochMillis, toEpochMillis)
    }
}
