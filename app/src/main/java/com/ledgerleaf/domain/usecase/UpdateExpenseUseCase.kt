package com.ledgerleaf.domain.usecase

import com.ledgerleaf.domain.repository.ExpenseRepository
import com.ledgerleaf.domain.repository.NewExpense
import javax.inject.Inject

class UpdateExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(id: String, input: NewExpense): Result<Unit> = runCatching {
        require(id.isNotBlank()) { "Expense id is required." }
        validateExpense(input)
        repository.updateExpense(id, input)
    }
}
