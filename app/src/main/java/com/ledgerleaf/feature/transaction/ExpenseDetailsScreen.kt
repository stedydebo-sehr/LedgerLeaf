package com.ledgerleaf.feature.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ledgerleaf.core.ui.components.DoubleConfirmationDialog
import com.ledgerleaf.core.ui.components.LedgerLeafTopBar
import com.ledgerleaf.core.utils.CurrencyFormatter
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ExpenseDetailsScreen(
    onEdit: (String) -> Unit,
    onDeleted: () -> Unit,
    onArchived: () -> Unit,
    viewModel: ExpenseDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showArchiveDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        if (state is ExpenseDetailsUiState.Deleted) onDeleted()
        if (state is ExpenseDetailsUiState.Archived) onArchived()
    }

    Column(Modifier.fillMaxSize()) {
        LedgerLeafTopBar("Expense Details")
        when (val current = state) {
            ExpenseDetailsUiState.Loading -> {
                Row(Modifier.fillMaxWidth().padding(32.dp), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator()
                }
            }
            ExpenseDetailsUiState.NotFound -> {
                Text("Expense not found.", modifier = Modifier.padding(24.dp))
            }
            ExpenseDetailsUiState.Deleted -> Unit
            ExpenseDetailsUiState.Archived -> Unit
            is ExpenseDetailsUiState.Error -> {
                Text(current.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(24.dp))
            }
            is ExpenseDetailsUiState.Ready -> {
                val expense = current.expense
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)
                ) {
                    Text(
                        CurrencyFormatter.format(BigDecimal.valueOf(expense.amountMinor, 2), expense.currencyCode),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(20.dp))
                    DetailLine("Category", expense.category.name)
                    DetailLine(
                        "Details",
                        expense.subcategories.joinToString { it.name }.ifBlank { "—" }
                    )
                    DetailLine("Payment method", expense.paymentMethod.name)
                    DetailLine("Date & time", formatExpenseDateTime(expense.occurredAtEpochMillis))
                    DetailLine("Notes", expense.notes)
                    DetailLine("Favorite", if (expense.isFavorite) "Yes" else "No")
                    DetailLine(
                        "Recurring",
                        if (expense.isRecurring) {
                            expense.recurringFrequency?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Yes"
                        } else "No"
                    )

                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { onEdit(expense.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Edit Expense") }

                    OutlinedButton(
                        onClick = { showArchiveDialog = true },
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                    ) { Text("Archive Expense") }

                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                    ) { Text("Delete Expense") }
                }

                if (showArchiveDialog) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showArchiveDialog = false },
                        title = { Text("Archive expense?") },
                        text = { Text("This expense will leave active ledger views and remain available in Archive for 18 months by default.") },
                        confirmButton = {
                            Button(onClick = { showArchiveDialog = false; viewModel.archive() }) { Text("Archive") }
                        },
                        dismissButton = { androidx.compose.material3.TextButton(onClick = { showArchiveDialog = false }) { Text("Cancel") } }
                    )
                }

                DoubleConfirmationDialog(
                    visible = showDeleteDialog,
                    itemDescription = "this expense. It will be moved to the Recycle Bin",
                    onDismiss = { showDeleteDialog = false },
                    onConfirmed = {
                        showDeleteDialog = false
                        viewModel.delete()
                    }
                )
            }
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    Text(value, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 3.dp, bottom = 14.dp))
}

private fun formatExpenseDateTime(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
        .format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))
