package com.ledgerleaf.domain.usecase

import com.ledgerleaf.domain.repository.ExpenseRepository
import javax.inject.Inject

class RestoreExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> = runCatching {
        require(id.isNotBlank()) { "Expense id is required." }
        repository.restoreExpense(id)
    }
}
