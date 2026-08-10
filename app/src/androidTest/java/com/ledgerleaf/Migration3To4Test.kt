package com.ledgerleaf

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ledgerleaf.core.database.DatabaseMigrations
import com.ledgerleaf.core.database.LedgerLeafDatabase
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration3To4Test {
    @Test fun migration3To4PreservesDatabaseAndAddsArchiveIndex() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "migration-3-4-test.db"
        context.deleteDatabase(name)
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(name)
                .callback(object : SupportSQLiteOpenHelper.Callback(3) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL("CREATE TABLE IF NOT EXISTS `_foundation` (`id` TEXT NOT NULL, `createdAtEpochMillis` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                        DatabaseMigrations.MIGRATION_1_2.migrate(db)
                        DatabaseMigrations.MIGRATION_2_3.migrate(db)
                    }
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
                }).build()
        )
        helper.writableDatabase
        helper.close()

        val room = Room.databaseBuilder(context, LedgerLeafDatabase::class.java, name)
            .addMigrations(DatabaseMigrations.MIGRATION_3_4)
            .allowMainThreadQueries()
            .build()
        room.openHelper.writableDatabase.query("PRAGMA index_list(`expenses`)").use { cursor ->
            val nameColumn = cursor.getColumnIndex("name")
            var found = false
            while (cursor.moveToNext()) if (cursor.getString(nameColumn) == "index_expenses_archivedAtEpochMillis") found = true
            assertTrue(found)
        }
        room.close()
        context.deleteDatabase(name)
    }
}
