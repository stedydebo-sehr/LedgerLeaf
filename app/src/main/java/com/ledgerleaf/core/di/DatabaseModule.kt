package com.ledgerleaf.core.di

import android.content.Context
import androidx.room.Room
import com.ledgerleaf.core.database.LedgerLeafDatabase
import com.ledgerleaf.core.database.DatabaseMigrations
import com.ledgerleaf.data.local.dao.BackupDao
import com.ledgerleaf.data.local.dao.CategoryDao
import com.ledgerleaf.data.local.dao.ExpenseDao
import com.ledgerleaf.data.local.dao.PaymentMethodDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideLedgerLeafDatabase(@ApplicationContext context: Context): LedgerLeafDatabase =
        Room.databaseBuilder(context, LedgerLeafDatabase::class.java, LedgerLeafDatabase.DATABASE_NAME)
            .addMigrations(DatabaseMigrations.MIGRATION_1_2, DatabaseMigrations.MIGRATION_2_3, DatabaseMigrations.MIGRATION_3_4)
            .build()

    @Provides
    fun provideBackupDao(database: LedgerLeafDatabase): BackupDao = database.backupDao()

    @Provides
    fun provideCategoryDao(database: LedgerLeafDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun providePaymentMethodDao(database: LedgerLeafDatabase): PaymentMethodDao = database.paymentMethodDao()

    @Provides
    fun provideExpenseDao(database: LedgerLeafDatabase): ExpenseDao = database.expenseDao()
}
