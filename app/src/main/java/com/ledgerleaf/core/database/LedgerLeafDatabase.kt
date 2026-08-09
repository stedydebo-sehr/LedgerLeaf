package com.ledgerleaf.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ledgerleaf.data.local.dao.CategoryDao
import com.ledgerleaf.data.local.dao.ExpenseDao
import com.ledgerleaf.data.local.dao.PaymentMethodDao
import com.ledgerleaf.data.local.entity.CategoryEntity
import com.ledgerleaf.data.local.entity.ExpenseEntity
import com.ledgerleaf.data.local.entity.ExpenseSubcategoryCrossRef
import com.ledgerleaf.data.local.entity.PaymentMethodEntity
import com.ledgerleaf.data.local.entity.SubcategoryEntity

@Database(
    entities = [
        FoundationEntity::class,
        CategoryEntity::class,
        SubcategoryEntity::class,
        PaymentMethodEntity::class,
        ExpenseEntity::class,
        ExpenseSubcategoryCrossRef::class
    ],
    version = 3,
    exportSchema = false
)
abstract class LedgerLeafDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun paymentMethodDao(): PaymentMethodDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        const val DATABASE_NAME = "ledgerleaf.db"
    }
}
