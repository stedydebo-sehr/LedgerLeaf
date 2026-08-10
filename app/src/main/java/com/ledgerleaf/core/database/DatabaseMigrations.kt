package com.ledgerleaf.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
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

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `payment_methods` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `isSystem` INTEGER NOT NULL, `isActive` INTEGER NOT NULL, `sortOrder` INTEGER NOT NULL, `createdAtEpochMillis` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_payment_methods_name` ON `payment_methods` (`name`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_payment_methods_isActive` ON `payment_methods` (`isActive`)")
            db.execSQL("INSERT OR IGNORE INTO `payment_methods` (`id`,`name`,`isSystem`,`isActive`,`sortOrder`,`createdAtEpochMillis`) VALUES ('system-payment-other','Other',1,1,50,0),('system-payment-cash','Cash',1,1,0,0),('system-payment-upi','UPI',1,1,10,0),('system-payment-debit-card','Debit Card',1,1,20,0),('system-payment-credit-card','Credit Card',1,1,30,0),('system-payment-bank-transfer','Bank Transfer',1,1,40,0)")

            // SQLite cannot add a foreign key with ALTER TABLE ADD COLUMN. Preserve the
            // relationship rows, rebuild expenses with the final v3 foreign keys, then
            // recreate the join table. This keeps v2 data intact and lets Room validate.
            db.execSQL("CREATE TEMP TABLE `expense_subcategories_backup` AS SELECT `expenseId`,`subcategoryId` FROM `expense_subcategories`")
            db.execSQL("DROP TABLE `expense_subcategories`")
            db.execSQL("CREATE TABLE `expenses_new` (`id` TEXT NOT NULL, `amountMinor` INTEGER NOT NULL, `currencyCode` TEXT NOT NULL, `categoryId` TEXT NOT NULL, `paymentMethodId` TEXT NOT NULL, `notes` TEXT NOT NULL, `occurredAtEpochMillis` INTEGER NOT NULL, `isFavorite` INTEGER NOT NULL, `isRecurring` INTEGER NOT NULL, `recurringFrequency` TEXT, `createdAtEpochMillis` INTEGER NOT NULL, `updatedAtEpochMillis` INTEGER NOT NULL, `deletedAtEpochMillis` INTEGER, `archivedAtEpochMillis` INTEGER, PRIMARY KEY(`id`), FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT, FOREIGN KEY(`paymentMethodId`) REFERENCES `payment_methods`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT)")
            db.execSQL("INSERT INTO `expenses_new` (`id`,`amountMinor`,`currencyCode`,`categoryId`,`paymentMethodId`,`notes`,`occurredAtEpochMillis`,`isFavorite`,`isRecurring`,`recurringFrequency`,`createdAtEpochMillis`,`updatedAtEpochMillis`,`deletedAtEpochMillis`,`archivedAtEpochMillis`) SELECT `id`,`amountMinor`,`currencyCode`,`categoryId`,'system-payment-other',`notes`,`occurredAtEpochMillis`,`isFavorite`,`isRecurring`,`recurringFrequency`,`createdAtEpochMillis`,`updatedAtEpochMillis`,`deletedAtEpochMillis`,`archivedAtEpochMillis` FROM `expenses`")
            db.execSQL("DROP TABLE `expenses`")
            db.execSQL("ALTER TABLE `expenses_new` RENAME TO `expenses`")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_categoryId` ON `expenses` (`categoryId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_paymentMethodId` ON `expenses` (`paymentMethodId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_occurredAtEpochMillis` ON `expenses` (`occurredAtEpochMillis`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_deletedAtEpochMillis` ON `expenses` (`deletedAtEpochMillis`)")
            db.execSQL("CREATE TABLE `expense_subcategories` (`expenseId` TEXT NOT NULL, `subcategoryId` TEXT NOT NULL, PRIMARY KEY(`expenseId`, `subcategoryId`), FOREIGN KEY(`expenseId`) REFERENCES `expenses`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(`subcategoryId`) REFERENCES `subcategories`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT)")
            db.execSQL("CREATE INDEX `index_expense_subcategories_expenseId` ON `expense_subcategories` (`expenseId`)")
            db.execSQL("CREATE INDEX `index_expense_subcategories_subcategoryId` ON `expense_subcategories` (`subcategoryId`)")
            db.execSQL("INSERT INTO `expense_subcategories` (`expenseId`,`subcategoryId`) SELECT `expenseId`,`subcategoryId` FROM `expense_subcategories_backup`")
            db.execSQL("DROP TABLE `expense_subcategories_backup`")
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_archivedAtEpochMillis` ON `expenses` (`archivedAtEpochMillis`)")
        }
    }
}
