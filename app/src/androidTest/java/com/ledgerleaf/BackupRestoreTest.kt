package com.ledgerleaf

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ledgerleaf.core.database.LedgerLeafDatabase
import com.ledgerleaf.core.datastore.ThemeMode
import com.ledgerleaf.data.local.entity.CategoryEntity
import com.ledgerleaf.data.local.entity.PaymentMethodEntity
import com.ledgerleaf.data.repository.RoomBackupRepository
import com.ledgerleaf.data.repository.RoomExpenseRepository
import com.ledgerleaf.domain.model.AppPreferences
import com.ledgerleaf.domain.repository.SettingsRepository
import com.ledgerleaf.domain.repository.NewExpense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackupRestoreTest {
    private lateinit var db: LedgerLeafDatabase
    private lateinit var settings: FakeSettingsRepository
    private lateinit var backup: RoomBackupRepository
    private val categoryId = "22222222-2222-2222-2222-222222222222"

    @Before fun setUp() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), LedgerLeafDatabase::class.java)
            .allowMainThreadQueries().build()
        db.backupDao().insertCategories(listOf(CategoryEntity(categoryId, "Travel", false, "NONE", 0, 0)))
        db.backupDao().insertPaymentMethods(listOf(PaymentMethodEntity("system-payment-cash", "Cash", true, true, 0, 0)))
        settings = FakeSettingsRepository()
        backup = RoomBackupRepository(db.backupDao(), settings)
        RoomExpenseRepository(db.expenseDao()).addExpense(
            NewExpense(12345, "INR", categoryId, "system-payment-cash", emptyList(), "Train", 1000, false, false, null)
        )
    }

    @After fun tearDown() = db.close()

    @Test fun exportedBackupRestoresAtomically() = runBlocking {
        val (json, created) = backup.createBackup()
        assertEquals(1, created.expenseCount)
        db.backupDao().clearExpenseSubcategories(); db.backupDao().clearExpenses()
        val restored = backup.restoreBackup(json)
        assertEquals(1, restored.expenseCount)
        assertEquals(1, db.backupDao().getExpenses().size)
    }

    @Test fun invalidBackupDoesNotReplaceDatabase() = runBlocking {
        val before = db.backupDao().getExpenses().size
        assertThrows(IllegalArgumentException::class.java) { runBlocking { backup.restoreBackup("{\"format\":\"wrong\"}") } }
        assertEquals(before, db.backupDao().getExpenses().size)
    }
}

private class FakeSettingsRepository : SettingsRepository {
    private val state = MutableStateFlow(AppPreferences())
    override val preferences: Flow<AppPreferences> = state
    override suspend fun setThemeMode(mode: ThemeMode) { state.value = state.value.copy(themeMode = mode) }
    override suspend fun setCurrencyCode(code: String) { state.value = state.value.copy(currencyCode = code) }
    override suspend fun setMonthlyBudgetMinor(amountMinor: Long?) { state.value = state.value.copy(monthlyBudgetMinor = amountMinor) }
    override suspend fun setMonthlyIncomeMinor(amountMinor: Long?) { state.value = state.value.copy(monthlyIncomeMinor = amountMinor) }
    override suspend fun setMonthStartDay(day: Int) { state.value = state.value.copy(monthStartDay = day) }
    override suspend fun setPdfIncludeTransactions(include: Boolean) { state.value = state.value.copy(pdfIncludeTransactions = include) }
    override suspend fun setPdfIncludeNotes(include: Boolean) { state.value = state.value.copy(pdfIncludeNotes = include) }
    override suspend fun restorePreferences(preferences: AppPreferences) { state.value = preferences }
}
