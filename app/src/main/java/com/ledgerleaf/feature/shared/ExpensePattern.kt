package com.ledgerleaf.feature.shared

import com.ledgerleaf.domain.model.Expense

internal data class ExpensePatternKey(
    val categoryId: String,
    val subcategoryIds: List<String>,
    val paymentMethodId: String,
    val normalizedNotes: String
)

internal fun Expense.patternKey(): ExpensePatternKey = ExpensePatternKey(
    categoryId = category.id,
    subcategoryIds = subcategories.map { it.id }.sorted(),
    paymentMethodId = paymentMethod.id,
    normalizedNotes = notes.trim().lowercase()
)
