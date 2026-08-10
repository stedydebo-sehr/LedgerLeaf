package com.ledgerleaf.domain.usecase

import com.ledgerleaf.domain.repository.ExpenseRepository
import javax.inject.Inject

class RestoreArchivedExpenseUseCase @Inject constructor(private val repository: ExpenseRepository) {
    suspend operator fun invoke(id: String): Result<Unit> = runCatching { repository.restoreArchivedExpense(id) }
}
