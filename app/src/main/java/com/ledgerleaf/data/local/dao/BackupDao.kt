package com.ledgerleaf.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ledgerleaf.data.local.entity.CategoryEntity
import com.ledgerleaf.data.local.entity.ExpenseEntity
import com.ledgerleaf.data.local.entity.ExpenseSubcategoryCrossRef
import com.ledgerleaf.data.local.entity.PaymentMethodEntity
import com.ledgerleaf.data.local.entity.SubcategoryEntity

@Dao
interface BackupDao {
    @Query("SELECT * FROM categories ORDER BY sortOrder, name")
    suspend fun getCategories(): List<CategoryEntity>

    @Query("SELECT * FROM subcategories ORDER BY categoryId, sortOrder, name")
    suspend fun getSubcategories(): List<SubcategoryEntity>

    @Query("SELECT * FROM payment_methods ORDER BY sortOrder, name")
    suspend fun getPaymentMethods(): List<PaymentMethodEntity>

    @Query("SELECT * FROM expenses ORDER BY occurredAtEpochMillis, id")
    suspend fun getExpenses(): List<ExpenseEntity>

    @Query("SELECT * FROM expense_subcategories ORDER BY expenseId, subcategoryId")
    suspend fun getExpenseSubcategoryRefs(): List<ExpenseSubcategoryCrossRef>

    @Query("DELETE FROM expense_subcategories")
    suspend fun clearExpenseSubcategories()

    @Query("DELETE FROM expenses")
    suspend fun clearExpenses()

    @Query("DELETE FROM subcategories")
    suspend fun clearSubcategories()

    @Query("DELETE FROM categories")
    suspend fun clearCategories()

    @Query("DELETE FROM payment_methods")
    suspend fun clearPaymentMethods()

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCategories(items: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSubcategories(items: List<SubcategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPaymentMethods(items: List<PaymentMethodEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertExpenses(items: List<ExpenseEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertExpenseSubcategoryRefs(items: List<ExpenseSubcategoryCrossRef>)

    @Transaction
    suspend fun replaceAll(
        categories: List<CategoryEntity>,
        subcategories: List<SubcategoryEntity>,
        paymentMethods: List<PaymentMethodEntity>,
        expenses: List<ExpenseEntity>,
        refs: List<ExpenseSubcategoryCrossRef>
    ) {
        clearExpenseSubcategories()
        clearExpenses()
        clearSubcategories()
        clearCategories()
        clearPaymentMethods()

        if (categories.isNotEmpty()) insertCategories(categories)
        if (subcategories.isNotEmpty()) insertSubcategories(subcategories)
        if (paymentMethods.isNotEmpty()) insertPaymentMethods(paymentMethods)
        if (expenses.isNotEmpty()) insertExpenses(expenses)
        if (refs.isNotEmpty()) insertExpenseSubcategoryRefs(refs)
    }
}
