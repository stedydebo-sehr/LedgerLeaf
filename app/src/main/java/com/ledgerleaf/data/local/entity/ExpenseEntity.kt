package com.ledgerleaf.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = PaymentMethodEntity::class,
            parentColumns = ["id"],
            childColumns = ["paymentMethodId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("categoryId"),
        Index("paymentMethodId"),
        Index("occurredAtEpochMillis"),
        Index("deletedAtEpochMillis"),
        Index("archivedAtEpochMillis")
    ]
)
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val amountMinor: Long,
    val currencyCode: String,
    val categoryId: String,
    val paymentMethodId: String,
    val notes: String,
    val occurredAtEpochMillis: Long,
    val isFavorite: Boolean,
    val isRecurring: Boolean,
    val recurringFrequency: String?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val deletedAtEpochMillis: Long? = null,
    val archivedAtEpochMillis: Long? = null
)
