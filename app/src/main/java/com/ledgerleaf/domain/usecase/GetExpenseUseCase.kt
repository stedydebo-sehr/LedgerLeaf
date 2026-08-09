package com.ledgerleaf.domain.usecase

import com.ledgerleaf.domain.model.Expense
import com.ledgerleaf.domain.repository.ExpenseRepository
import javax.inject.Inject

class GetExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(id: String): Expense? = repository.getExpense(id)
}
