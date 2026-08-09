package com.ledgerleaf.feature.transaction

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ledgerleaf.core.ui.components.LedgerLeafTopBar
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun EditExpenseScreen(
    onSaved: () -> Unit,
    viewModel: EditExpenseViewModel = hiltViewModel()
) {
    val expense by viewModel.expense.collectAsStateWithLifecycle()
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

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        LedgerLeafTopBar("Edit Expense")
        val current = expense
        if (current == null) {
            if (saveState !is SaveState.Error) CircularProgressIndicator()
        } else {
            val localDateTime = Instant.ofEpochMilli(current.occurredAtEpochMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()

            ExpenseEditorForm(
                initialValues = ExpenseEditorInitialValues(
                    amount = BigDecimal.valueOf(current.amountMinor, 2).stripTrailingZeros().toPlainString(),
                    categoryId = current.category.id,
                    subcategoryIds = current.subcategories.map { it.id },
                    paymentMethodId = current.paymentMethod.id,
                    notes = current.notes,
                    dateText = localDateTime.toLocalDate().toString(),
                    timeText = localDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                    favorite = current.isFavorite,
                    recurring = current.isRecurring,
                    recurringFrequency = current.recurringFrequency ?: "MONTHLY"
                ),
                categories = categories,
                subcategories = subcategories,
                paymentMethods = paymentMethods,
                errorMessage = (saveState as? SaveState.Error)?.message,
                actionLabel = "Update Expense",
                onCategorySelected = viewModel::selectCategory,
                onAddCustomCategory = viewModel::addCustomCategory,
                onSubmit = viewModel::save,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
