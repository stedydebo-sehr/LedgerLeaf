package com.ledgerleaf.core.navigation
sealed class LedgerLeafDestination(val route: String) { data object Dashboard : LedgerLeafDestination("dashboard") }
