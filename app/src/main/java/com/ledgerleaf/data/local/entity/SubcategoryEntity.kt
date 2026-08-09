package com.ledgerleaf.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "subcategories",
    foreignKeys = [ForeignKey(entity = CategoryEntity::class, parentColumns = ["id"], childColumns = ["categoryId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("categoryId")]
)
data class SubcategoryEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val name: String,
    val isSystem: Boolean,
    val sortOrder: Int,
    val createdAtEpochMillis: Long
)
