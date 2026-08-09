package com.ledgerleaf.feature.transaction

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ledgerleaf.core.ui.components.LedgerLeafTopBar
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun AddExpenseScreen(
    onSaved: () -> Unit,
    viewModel: AddExpenseViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val subcategories by viewModel.subcategories.collectAsStateWithLifecycle()
    val paymentMethods by viewModel.paymentMethods.collectAsStateWithLifecycle()
    val saveState by viewModel.saveState.collectAsStateWithLifecycle()

    LaunchedEffect(saveState) {
        if (saveState is SaveState.Saved) {
            viewModel.consumeSaved()
            onSaved()
        }
    }

    Column(Modifier.fillMaxSize()) {
        LedgerLeafTopBar("Add Expense")
        ExpenseEditorForm(
            initialValues = ExpenseEditorInitialValues(
                dateText = LocalDate.now().toString(),
                timeText = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            ),
            categories = categories,
            subcategories = subcategories,
            paymentMethods = paymentMethods,
            errorMessage = (saveState as? SaveState.Error)?.message,
            actionLabel = "Save Expense",
            onCategorySelected = viewModel::selectCategory,
            onAddCustomCategory = viewModel::addCustomCategory,
            onSubmit = viewModel::save,
            modifier = Modifier.fillMaxSize()
        )
    }
}
