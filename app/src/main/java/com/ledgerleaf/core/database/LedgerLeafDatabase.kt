package com.ledgerleaf.core.database
import androidx.room.Database
import androidx.room.RoomDatabase
@Database(entities = [FoundationEntity::class], version = 1, exportSchema = false)
abstract class LedgerLeafDatabase : RoomDatabase() {
    companion object { const val DATABASE_NAME = "ledgerleaf.db" }
}
