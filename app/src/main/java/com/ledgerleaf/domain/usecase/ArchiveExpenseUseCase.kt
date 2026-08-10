package com.ledgerleaf.domain.usecase

import com.ledgerleaf.domain.repository.ExpenseRepository
import javax.inject.Inject

class ArchiveExpenseUseCase @Inject constructor(private val repository: ExpenseRepository) {
    suspend operator fun invoke(id: String): Result<Unit> = runCatching { repository.archiveExpense(id) }
}
