package com.ledgerleaf.domain.usecase

import com.ledgerleaf.domain.repository.ExpenseRepository
import com.ledgerleaf.domain.repository.NewExpense
import javax.inject.Inject

class AddExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(input: NewExpense): Result<String> = runCatching {
        validateExpense(input)
        repository.addExpense(input)
    }
}

internal fun validateExpense(input: NewExpense) {
    require(input.amountMinor > 0) { "Enter an amount greater than zero." }
    require(input.categoryId.isNotBlank()) { "Select a category." }
    require(input.paymentMethodId.isNotBlank()) { "Select a payment method." }
    require(input.notes.isNotBlank()) { "Detailed notes are required." }
}
