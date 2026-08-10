package com.ledgerleaf

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ledgerleaf.core.database.LedgerLeafDatabase
import com.ledgerleaf.data.local.entity.CategoryEntity
import com.ledgerleaf.data.local.entity.PaymentMethodEntity
import com.ledgerleaf.data.repository.RoomExpenseRepository
import com.ledgerleaf.domain.repository.NewExpense
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseAndRepositoryTest {
    private lateinit var db: LedgerLeafDatabase
    private lateinit var repository: RoomExpenseRepository
    private val categoryId = "11111111-1111-1111-1111-111111111111"
    private val paymentId = "system-payment-cash"

    @Before fun setUp() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, LedgerLeafDatabase::class.java).allowMainThreadQueries().build()
        db.backupDao().insertCategories(listOf(CategoryEntity(categoryId, "Food", false, "NONE", 0, 0)))
        db.backupDao().insertPaymentMethods(listOf(PaymentMethodEntity(paymentId, "Cash", true, true, 0, 0)))
        repository = RoomExpenseRepository(db.expenseDao())
    }

    @After fun tearDown() = db.close()

    private fun input(amount: Long = 1000) = NewExpense(
        amount, "INR", categoryId, paymentId, emptyList(), "Test note", 1000,
        isFavorite = false, isRecurring = false, recurringFrequency = null
    )

    @Test fun repositoryRoundTripPreservesExpense() = runBlocking {
        val id = repository.addExpense(input())
        val stored = repository.getExpense(id)
        assertNotNull(stored)
        assertEquals(1000L, stored?.amountMinor)
        assertEquals("Test note", stored?.notes)
    }

    @Test fun deleteRestoreArchiveRestoreKeepSameUuid() = runBlocking {
        val id = repository.addExpense(input())
        repository.softDeleteExpense(id)
        assertEquals(id, repository.observeDeletedExpenses().first().single().expense.id)
        assertEquals(0, repository.observeActiveExpenses().first().size)

        repository.restoreExpense(id)
        assertEquals(id, repository.observeActiveExpenses().first().single().id)

        repository.archiveExpense(id)
        assertEquals(id, repository.observeArchivedExpenses().first().single().expense.id)
        assertEquals(0, repository.observeActiveExpenses().first().size)

        repository.restoreArchivedExpense(id)
        assertEquals(id, repository.observeActiveExpenses().first().single().id)
    }

    @Test fun activeTotalExcludesDeletedAndArchivedRows() = runBlocking {
        val active = repository.addExpense(input(1000))
        val deleted = repository.addExpense(input(2000))
        val archived = repository.addExpense(input(3000))
        repository.softDeleteExpense(deleted)
        repository.archiveExpense(archived)
        assertEquals(1000L, repository.getActiveTotalMinor(0, 5000))
        assertNotNull(repository.getExpense(active))
    }
}
