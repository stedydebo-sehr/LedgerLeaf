package com.ledgerleaf.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val isSystem: Boolean,
    val selectionMode: String,
    val sortOrder: Int,
    val createdAtEpochMillis: Long
)
