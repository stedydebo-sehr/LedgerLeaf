package com.ledgerleaf.core.navigation

sealed class LedgerLeafDestination(
    val route: String,
    val label: String
) {
    data object Dashboard : LedgerLeafDestination("dashboard", "Home")
    data object History : LedgerLeafDestination("history", "History")
    data object AddExpense : LedgerLeafDestination("add_expense", "Add")
    data object Reports : LedgerLeafDestination("reports", "Reports")
    data object Settings : LedgerLeafDestination("settings", "More")
    data object Search : LedgerLeafDestination("search", "Search")
    data object Archive : LedgerLeafDestination("archive", "Archive")
    data object RecycleBin : LedgerLeafDestination("recycle_bin", "Recycle Bin")
    data object Budgets : LedgerLeafDestination("budgets", "Budgets")

    data object ExpenseDetails : LedgerLeafDestination("expense/{expenseId}", "Expense") {
        fun createRoute(expenseId: String) = "expense/$expenseId"
    }

    data object EditExpense : LedgerLeafDestination("expense/{expenseId}/edit", "Edit Expense") {
        fun createRoute(expenseId: String) = "expense/$expenseId/edit"
    }
}
