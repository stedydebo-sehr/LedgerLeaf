package com.ledgerleaf.data.repository

import com.ledgerleaf.core.utils.UuidGenerator
import com.ledgerleaf.data.local.dao.ExpenseDao
import com.ledgerleaf.data.local.entity.ExpenseEntity
import com.ledgerleaf.data.local.model.ExpenseWithDetails
import com.ledgerleaf.domain.model.Category
import com.ledgerleaf.domain.model.CategorySelectionMode
import com.ledgerleaf.domain.model.Expense
import com.ledgerleaf.domain.model.PaymentMethod
import com.ledgerleaf.domain.model.Subcategory
import com.ledgerleaf.domain.repository.DeletedExpense
import com.ledgerleaf.domain.repository.ExpenseRepository
import com.ledgerleaf.domain.repository.NewExpense
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RoomExpenseRepository @Inject constructor(
    private val dao: ExpenseDao
) : ExpenseRepository {

    override fun observeActiveExpenses(): Flow<List<Expense>> =
        dao.observeActiveExpenses().map { list -> list.map { it.toDomain() } }

    override fun observeActiveExpensesInRange(fromEpochMillis: Long, toEpochMillis: Long): Flow<List<Expense>> =
        dao.observeActiveExpensesInRange(fromEpochMillis, toEpochMillis).map { list -> list.map { it.toDomain() } }

    override fun observeDeletedExpenses(): Flow<List<DeletedExpense>> =
        dao.observeDeletedExpenses().map { list ->
            list.mapNotNull { item ->
                item.expense.deletedAtEpochMillis?.let { deletedAt -> DeletedExpense(item.toDomain(), deletedAt) }
            }
        }

    override suspend fun getExpense(id: String): Expense? = dao.getExpense(id)?.toDomain()

    override suspend fun addExpense(input: NewExpense): String {
        validate(input)
        val now = System.currentTimeMillis()
        val id = UuidGenerator.newId()
        dao.insertExpenseWithSubcategories(
            ExpenseEntity(
                id = id,
                amountMinor = input.amountMinor,
                currencyCode = input.currencyCode,
                categoryId = input.categoryId,
                paymentMethodId = input.paymentMethodId,
                notes = input.notes.trim(),
                occurredAtEpochMillis = input.occurredAtEpochMillis,
                isFavorite = input.isFavorite,
                isRecurring = input.isRecurring,
                recurringFrequency = input.recurringFrequency,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now
            ),
            input.subcategoryIds
        )
        return id
    }

    override suspend fun updateExpense(id: String, input: NewExpense) {
        validate(input)
        val current = dao.getExpense(id)?.expense ?: error("Expense not found.")
        dao.updateExpenseWithSubcategories(
            current.copy(
                amountMinor = input.amountMinor,
                currencyCode = input.currencyCode,
                categoryId = input.categoryId,
                paymentMethodId = input.paymentMethodId,
                notes = input.notes.trim(),
                occurredAtEpochMillis = input.occurredAtEpochMillis,
                isFavorite = input.isFavorite,
                isRecurring = input.isRecurring,
                recurringFrequency = input.recurringFrequency,
                updatedAtEpochMillis = System.currentTimeMillis()
            ),
            input.subcategoryIds
        )
    }

    override suspend fun softDeleteExpense(id: String) {
        val now = System.currentTimeMillis()
        dao.softDelete(id, now, now)
    }

    override suspend fun restoreExpense(id: String) {
        dao.restore(id, System.currentTimeMillis())
    }

    override suspend fun purgeDeletedBefore(cutoffEpochMillis: Long) {
        dao.purgeDeletedBefore(cutoffEpochMillis)
    }

    override suspend fun getActiveTotalMinor(fromEpochMillis: Long, toEpochMillis: Long): Long =
        dao.getActiveTotalMinor(fromEpochMillis, toEpochMillis)

    private fun validate(input: NewExpense) {
        require(input.amountMinor > 0) { "Amount must be greater than zero." }
        require(input.categoryId.isNotBlank()) { "Category is required." }
        require(input.paymentMethodId.isNotBlank()) { "Payment method is required." }
        require(input.notes.isNotBlank()) { "Notes are mandatory." }
    }

    private fun ExpenseWithDetails.toDomain() = Expense(
        id = expense.id,
        amountMinor = expense.amountMinor,
        currencyCode = expense.currencyCode,
        category = Category(
            category.id,
            category.name,
            category.isSystem,
            runCatching { CategorySelectionMode.valueOf(category.selectionMode) }
                .getOrDefault(CategorySelectionMode.NONE)
        ),
        paymentMethod = PaymentMethod(
            paymentMethod.id,
            paymentMethod.name,
            paymentMethod.isSystem,
            paymentMethod.isActive
        ),
        subcategories = subcategories.map { Subcategory(it.id, it.categoryId, it.name, it.isSystem) },
        notes = expense.notes,
        occurredAtEpochMillis = expense.occurredAtEpochMillis,
        isFavorite = expense.isFavorite,
        isRecurring = expense.isRecurring,
        recurringFrequency = expense.recurringFrequency
    )
}
