package com.ledgerleaf.feature.transaction

import androidx.compose.runtime.Composable
import com.ledgerleaf.core.ui.components.FoundationPlaceholderScreen

@Composable
fun AddExpensePlaceholderScreen() {
    FoundationPlaceholderScreen(
        title = "Add Expense",
        message = "Expense entry will be implemented in the Expense module. Notes will be mandatory."
    )
}
