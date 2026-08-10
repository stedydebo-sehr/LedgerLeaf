package com.ledgerleaf.domain.repository

import com.ledgerleaf.core.datastore.ThemeMode
import com.ledgerleaf.domain.model.AppPreferences
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val preferences: Flow<AppPreferences>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setCurrencyCode(code: String)
    suspend fun setMonthlyBudgetMinor(amountMinor: Long?)
    suspend fun setMonthlyIncomeMinor(amountMinor: Long?)
    suspend fun setMonthStartDay(day: Int)
    suspend fun setPdfIncludeTransactions(include: Boolean)
    suspend fun setPdfIncludeNotes(include: Boolean)
    suspend fun setDisplayName(name: String)
    suspend fun setProfileImagePath(path: String?)
    suspend fun restorePreferences(preferences: AppPreferences)
}
