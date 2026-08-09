package com.ledgerleaf.domain.model

import com.ledgerleaf.core.datastore.ThemeMode

data class AppPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val currencyCode: String = "INR",
    val monthlyBudgetMinor: Long? = null,
    val monthStartDay: Int = 1
)
