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
    data object Favorites : LedgerLeafDestination("favorites", "Favorites")
    data object Recurring : LedgerLeafDestination("recurring", "Recurring")
    data object MonthlyClosing : LedgerLeafDestination("monthly_closing", "Monthly Closing")
    data object BackupRestore : LedgerLeafDestination("backup_restore", "Backup & Restore")

    data object AddExpenseFromTemplate : LedgerLeafDestination("add_expense/from/{expenseId}", "Add from expense") {
        fun createRoute(expenseId: String) = "add_expense/from/$expenseId"
    }

    data object ExpenseDetails : LedgerLeafDestination("expense/{expenseId}", "Expense") {
        fun createRoute(expenseId: String) = "expense/$expenseId"
    }

    data object EditExpense : LedgerLeafDestination("expense/{expenseId}/edit", "Edit Expense") {
        fun createRoute(expenseId: String) = "expense/$expenseId/edit"
    }
}
