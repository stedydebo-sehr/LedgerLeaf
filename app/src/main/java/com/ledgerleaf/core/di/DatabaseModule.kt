package com.ledgerleaf.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ledgerleaf.core.database.LedgerLeafDatabase
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

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `categories` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `isSystem` INTEGER NOT NULL, `selectionMode` TEXT NOT NULL, `sortOrder` INTEGER NOT NULL, `createdAtEpochMillis` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        db.execSQL("CREATE TABLE IF NOT EXISTS `subcategories` (`id` TEXT NOT NULL, `categoryId` TEXT NOT NULL, `name` TEXT NOT NULL, `isSystem` INTEGER NOT NULL, `sortOrder` INTEGER NOT NULL, `createdAtEpochMillis` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_subcategories_categoryId` ON `subcategories` (`categoryId`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `expenses` (`id` TEXT NOT NULL, `amountMinor` INTEGER NOT NULL, `currencyCode` TEXT NOT NULL, `categoryId` TEXT NOT NULL, `notes` TEXT NOT NULL, `occurredAtEpochMillis` INTEGER NOT NULL, `isFavorite` INTEGER NOT NULL, `isRecurring` INTEGER NOT NULL, `recurringFrequency` TEXT, `createdAtEpochMillis` INTEGER NOT NULL, `updatedAtEpochMillis` INTEGER NOT NULL, `deletedAtEpochMillis` INTEGER, `archivedAtEpochMillis` INTEGER, PRIMARY KEY(`id`), FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_categoryId` ON `expenses` (`categoryId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_occurredAtEpochMillis` ON `expenses` (`occurredAtEpochMillis`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_deletedAtEpochMillis` ON `expenses` (`deletedAtEpochMillis`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `expense_subcategories` (`expenseId` TEXT NOT NULL, `subcategoryId` TEXT NOT NULL, PRIMARY KEY(`expenseId`, `subcategoryId`), FOREIGN KEY(`expenseId`) REFERENCES `expenses`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(`subcategoryId`) REFERENCES `subcategories`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_expense_subcategories_expenseId` ON `expense_subcategories` (`expenseId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_expense_subcategories_subcategoryId` ON `expense_subcategories` (`subcategoryId`)")
    }
}

private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `payment_methods` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `isSystem` INTEGER NOT NULL, `isActive` INTEGER NOT NULL, `sortOrder` INTEGER NOT NULL, `createdAtEpochMillis` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_payment_methods_name` ON `payment_methods` (`name`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_payment_methods_isActive` ON `payment_methods` (`isActive`)")
        db.execSQL("INSERT OR IGNORE INTO `payment_methods` (`id`,`name`,`isSystem`,`isActive`,`sortOrder`,`createdAtEpochMillis`) VALUES ('system-payment-other','Other',1,1,50,0)")
        db.execSQL("ALTER TABLE `expenses` ADD COLUMN `paymentMethodId` TEXT NOT NULL DEFAULT 'system-payment-other'")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_paymentMethodId` ON `expenses` (`paymentMethodId`)")
        db.execSQL("INSERT OR IGNORE INTO `payment_methods` (`id`,`name`,`isSystem`,`isActive`,`sortOrder`,`createdAtEpochMillis`) VALUES ('system-payment-cash','Cash',1,1,0,0),('system-payment-upi','UPI',1,1,10,0),('system-payment-debit-card','Debit Card',1,1,20,0),('system-payment-credit-card','Credit Card',1,1,30,0),('system-payment-bank-transfer','Bank Transfer',1,1,40,0)")
    }
}

private val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_archivedAtEpochMillis` ON `expenses` (`archivedAtEpochMillis`)")
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideLedgerLeafDatabase(@ApplicationContext context: Context): LedgerLeafDatabase =
        Room.databaseBuilder(context, LedgerLeafDatabase::class.java, LedgerLeafDatabase.DATABASE_NAME)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
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
