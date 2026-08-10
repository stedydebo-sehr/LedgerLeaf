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

data class DeletedExpense(val expense: Expense, val deletedAtEpochMillis: Long)
data class ArchivedExpense(val expense: Expense, val archivedAtEpochMillis: Long)
data class ReportableExpense(val expense: Expense, val archivedAtEpochMillis: Long?)

interface ExpenseRepository {
    fun observeActiveExpenses(): Flow<List<Expense>>
    fun observeActiveExpensesInRange(fromEpochMillis: Long, toEpochMillis: Long): Flow<List<Expense>>
    fun observeDeletedExpenses(): Flow<List<DeletedExpense>>
    fun observeArchivedExpenses(): Flow<List<ArchivedExpense>>
    fun observeReportableExpensesInRange(fromEpochMillis: Long, toEpochMillis: Long): Flow<List<ReportableExpense>>
    suspend fun getExpense(id: String): Expense?
    suspend fun addExpense(input: NewExpense): String
    suspend fun updateExpense(id: String, input: NewExpense)
    suspend fun softDeleteExpense(id: String)
    suspend fun restoreExpense(id: String)
    suspend fun purgeDeletedBefore(cutoffEpochMillis: Long)
    suspend fun archiveExpense(id: String)
    suspend fun restoreArchivedExpense(id: String)
    suspend fun purgeArchivedBefore(cutoffEpochMillis: Long)
    suspend fun getActiveTotalMinor(fromEpochMillis: Long, toEpochMillis: Long): Long
}
