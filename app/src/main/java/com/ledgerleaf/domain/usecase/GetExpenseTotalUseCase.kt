package com.ledgerleaf.domain.usecase

import com.ledgerleaf.domain.repository.ExpenseRepository
import javax.inject.Inject

class GetExpenseTotalUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(fromEpochMillis: Long, toEpochMillis: Long): Long {
        require(toEpochMillis >= fromEpochMillis) { "End date must not be before start date." }
        return repository.getActiveTotalMinor(fromEpochMillis, toEpochMillis)
    }
}
