package com.ledgerleaf.data.local.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.ledgerleaf.data.local.entity.CategoryEntity
import com.ledgerleaf.data.local.entity.ExpenseEntity
import com.ledgerleaf.data.local.entity.ExpenseSubcategoryCrossRef
import com.ledgerleaf.data.local.entity.PaymentMethodEntity
import com.ledgerleaf.data.local.entity.SubcategoryEntity

data class ExpenseWithDetails(
    @Embedded val expense: ExpenseEntity,
    @Relation(parentColumn = "categoryId", entityColumn = "id")
    val category: CategoryEntity,
    @Relation(parentColumn = "paymentMethodId", entityColumn = "id")
    val paymentMethod: PaymentMethodEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            ExpenseSubcategoryCrossRef::class,
            parentColumn = "expenseId",
            entityColumn = "subcategoryId"
        )
    )
    val subcategories: List<SubcategoryEntity>
)
