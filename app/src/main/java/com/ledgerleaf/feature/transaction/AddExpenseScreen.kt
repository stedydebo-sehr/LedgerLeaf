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

@Composable
fun AddExpenseScreen(
    onSaved: () -> Unit,
    templateExpenseId: String? = null,
    viewModel: AddExpenseViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val subcategories by viewModel.subcategories.collectAsStateWithLifecycle()
    val paymentMethods by viewModel.paymentMethods.collectAsStateWithLifecycle()
    val saveState by viewModel.saveState.collectAsStateWithLifecycle()
    val initialValues by viewModel.initialValues.collectAsStateWithLifecycle()

    LaunchedEffect(templateExpenseId) {
        viewModel.loadTemplate(templateExpenseId)
    }

    LaunchedEffect(saveState) {
        if (saveState is SaveState.Saved) {
            viewModel.consumeSaved()
            onSaved()
        }
    }

    Column(Modifier.fillMaxSize()) {
        LedgerLeafTopBar("Add Expense")
        ExpenseEditorForm(
            initialValues = initialValues,
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
