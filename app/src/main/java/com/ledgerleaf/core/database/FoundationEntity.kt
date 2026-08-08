package com.ledgerleaf.core.database
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID
@Entity(tableName = "_foundation")
data class FoundationEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)
