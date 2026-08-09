package com.ledgerleaf.domain.repository

import com.ledgerleaf.domain.model.Expense
import kotlinx.coroutines.flow.Flow

data class NewExpense(
    val amountMinor: Long,
    val currencyCode: String,
    val categoryId: String,
    val paymentMethodId: String,
    val subcategoryIds: List<String>,
    val notes: String,
    val occurredAtEpochMillis: Long,
    val isFavorite: Boolean,
    val isRecurring: Boolean,
    val recurringFrequency: String?
)

interface ExpenseRepository {
    fun observeActiveExpenses(): Flow<List<Expense>>
    fun observeActiveExpensesInRange(fromEpochMillis: Long, toEpochMillis: Long): Flow<List<Expense>>
    suspend fun getExpense(id: String): Expense?
    suspend fun addExpense(input: NewExpense): String
    suspend fun updateExpense(id: String, input: NewExpense)
    suspend fun softDeleteExpense(id: String)
    suspend fun restoreExpense(id: String)
    suspend fun getActiveTotalMinor(fromEpochMillis: Long, toEpochMillis: Long): Long
}
