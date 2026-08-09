package com.ledgerleaf.domain.model

data class Expense(
    val id: String,
    val amountMinor: Long,
    val currencyCode: String,
    val category: Category,
    val paymentMethod: PaymentMethod,
    val subcategories: List<Subcategory>,
    val notes: String,
    val occurredAtEpochMillis: Long,
    val isFavorite: Boolean,
    val isRecurring: Boolean,
    val recurringFrequency: String?
)
