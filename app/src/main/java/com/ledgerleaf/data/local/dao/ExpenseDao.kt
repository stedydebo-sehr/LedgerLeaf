package com.ledgerleaf.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ledgerleaf.data.local.entity.ExpenseEntity
import com.ledgerleaf.data.local.entity.ExpenseSubcategoryCrossRef
import com.ledgerleaf.data.local.model.ExpenseWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Transaction
    @Query("SELECT * FROM expenses WHERE deletedAtEpochMillis IS NULL AND archivedAtEpochMillis IS NULL ORDER BY occurredAtEpochMillis DESC")
    fun observeActiveExpenses(): Flow<List<ExpenseWithDetails>>

    @Transaction
    @Query("SELECT * FROM expenses WHERE deletedAtEpochMillis IS NULL AND archivedAtEpochMillis IS NULL AND occurredAtEpochMillis BETWEEN :from AND :to ORDER BY occurredAtEpochMillis DESC")
    fun observeActiveExpensesInRange(from: Long, to: Long): Flow<List<ExpenseWithDetails>>

    @Transaction
    @Query("SELECT * FROM expenses WHERE deletedAtEpochMillis IS NOT NULL ORDER BY deletedAtEpochMillis DESC")
    fun observeDeletedExpenses(): Flow<List<ExpenseWithDetails>>

    @Transaction
    @Query("SELECT * FROM expenses WHERE deletedAtEpochMillis IS NULL AND archivedAtEpochMillis IS NOT NULL ORDER BY archivedAtEpochMillis DESC")
    fun observeArchivedExpenses(): Flow<List<ExpenseWithDetails>>

    @Transaction
    @Query("SELECT * FROM expenses WHERE deletedAtEpochMillis IS NULL AND occurredAtEpochMillis BETWEEN :from AND :to ORDER BY occurredAtEpochMillis DESC")
    fun observeReportableExpensesInRange(from: Long, to: Long): Flow<List<ExpenseWithDetails>>

    @Transaction
    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    suspend fun getExpense(id: String): ExpenseWithDetails?

    @Query("SELECT COALESCE(SUM(amountMinor), 0) FROM expenses WHERE deletedAtEpochMillis IS NULL AND archivedAtEpochMillis IS NULL AND occurredAtEpochMillis BETWEEN :from AND :to")
    suspend fun getActiveTotalMinor(from: Long, to: Long): Long

    @Insert
    suspend fun insertExpense(expense: ExpenseEntity)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Insert
    suspend fun insertSubcategoryRefs(refs: List<ExpenseSubcategoryCrossRef>)

    @Query("DELETE FROM expense_subcategories WHERE expenseId = :expenseId")
    suspend fun deleteSubcategoryRefs(expenseId: String)

    @Query("UPDATE expenses SET deletedAtEpochMillis = :deletedAt, updatedAtEpochMillis = :updatedAt WHERE id = :id AND deletedAtEpochMillis IS NULL")
    suspend fun softDelete(id: String, deletedAt: Long, updatedAt: Long)

    @Query("UPDATE expenses SET deletedAtEpochMillis = NULL, updatedAtEpochMillis = :updatedAt WHERE id = :id AND deletedAtEpochMillis IS NOT NULL")
    suspend fun restore(id: String, updatedAt: Long)

    @Query("DELETE FROM expenses WHERE deletedAtEpochMillis IS NOT NULL AND deletedAtEpochMillis < :cutoff")
    suspend fun purgeDeletedBefore(cutoff: Long)

    @Query("UPDATE expenses SET archivedAtEpochMillis = :archivedAt, updatedAtEpochMillis = :updatedAt WHERE id = :id AND deletedAtEpochMillis IS NULL AND archivedAtEpochMillis IS NULL")
    suspend fun archive(id: String, archivedAt: Long, updatedAt: Long)

    @Query("UPDATE expenses SET archivedAtEpochMillis = NULL, updatedAtEpochMillis = :updatedAt WHERE id = :id AND deletedAtEpochMillis IS NULL AND archivedAtEpochMillis IS NOT NULL")
    suspend fun restoreArchived(id: String, updatedAt: Long)

    @Query("DELETE FROM expenses WHERE deletedAtEpochMillis IS NULL AND archivedAtEpochMillis IS NOT NULL AND archivedAtEpochMillis < :cutoff")
    suspend fun purgeArchivedBefore(cutoff: Long)

    @Transaction
    suspend fun insertExpenseWithSubcategories(expense: ExpenseEntity, subcategoryIds: List<String>) {
        insertExpense(expense)
        replaceSubcategoryRefs(expense.id, subcategoryIds)
    }

    @Transaction
    suspend fun updateExpenseWithSubcategories(expense: ExpenseEntity, subcategoryIds: List<String>) {
        updateExpense(expense)
        replaceSubcategoryRefs(expense.id, subcategoryIds)
    }

    suspend fun replaceSubcategoryRefs(expenseId: String, subcategoryIds: List<String>) {
        deleteSubcategoryRefs(expenseId)
        val refs = subcategoryIds.distinct().map { ExpenseSubcategoryCrossRef(expenseId, it) }
        if (refs.isNotEmpty()) insertSubcategoryRefs(refs)
    }
}
