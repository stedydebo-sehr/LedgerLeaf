package com.ledgerleaf.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ledgerleaf.core.datastore.ThemeMode
import com.ledgerleaf.domain.model.AppPreferences
import com.ledgerleaf.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.ledgerLeafDataStore by preferencesDataStore(name = "ledgerleaf_preferences")

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {
    private object Keys {
        val themeMode = stringPreferencesKey("theme_mode")
        val currencyCode = stringPreferencesKey("currency_code")
        val monthlyBudgetMinor = longPreferencesKey("monthly_budget_minor")
        val monthStartDay = intPreferencesKey("month_start_day")
    }

    override val preferences: Flow<AppPreferences> = context.ledgerLeafDataStore.data.map { values ->
        AppPreferences(
            themeMode = runCatching { ThemeMode.valueOf(values[Keys.themeMode] ?: ThemeMode.SYSTEM.name) }
                .getOrDefault(ThemeMode.SYSTEM),
            currencyCode = values[Keys.currencyCode] ?: "INR",
            monthlyBudgetMinor = values[Keys.monthlyBudgetMinor],
            monthStartDay = (values[Keys.monthStartDay] ?: 1).coerceIn(1, 28)
        )
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        context.ledgerLeafDataStore.edit { it[Keys.themeMode] = mode.name }
    }

    override suspend fun setCurrencyCode(code: String) {
        val normalized = code.trim().uppercase().take(3)
        if (normalized.length == 3) context.ledgerLeafDataStore.edit { it[Keys.currencyCode] = normalized }
    }

    override suspend fun setMonthlyBudgetMinor(amountMinor: Long?) {
        context.ledgerLeafDataStore.edit {
            if (amountMinor == null) it.remove(Keys.monthlyBudgetMinor) else it[Keys.monthlyBudgetMinor] = amountMinor.coerceAtLeast(0)
        }
    }

    override suspend fun setMonthStartDay(day: Int) {
        context.ledgerLeafDataStore.edit { it[Keys.monthStartDay] = day.coerceIn(1, 28) }
    }
}
