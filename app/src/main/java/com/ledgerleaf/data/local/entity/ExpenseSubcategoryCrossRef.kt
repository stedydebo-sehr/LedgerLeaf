package com.ledgerleaf.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "expense_subcategories",
    primaryKeys = ["expenseId", "subcategoryId"],
    foreignKeys = [
        ForeignKey(entity = ExpenseEntity::class, parentColumns = ["id"], childColumns = ["expenseId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = SubcategoryEntity::class, parentColumns = ["id"], childColumns = ["subcategoryId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("expenseId"), Index("subcategoryId")]
)
data class ExpenseSubcategoryCrossRef(val expenseId: String, val subcategoryId: String)
