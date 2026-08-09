package com.ledgerleaf.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payment_methods",
    indices = [Index(value = ["name"], unique = true), Index("isActive")]
)
data class PaymentMethodEntity(
    @PrimaryKey val id: String,
    val name: String,
    val isSystem: Boolean,
    val isActive: Boolean,
    val sortOrder: Int,
    val createdAtEpochMillis: Long
)
