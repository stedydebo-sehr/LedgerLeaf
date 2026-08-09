package com.ledgerleaf.feature.recyclebin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ledgerleaf.core.ui.components.LedgerLeafTopBar
import com.ledgerleaf.core.utils.CurrencyFormatter
import com.ledgerleaf.core.utils.DateFormatter
import com.ledgerleaf.domain.repository.DeletedExpense
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId

@Composable
fun RecycleBinScreen(viewModel: RecycleBinViewModel = hiltViewModel()) {
    val items by viewModel.deletedExpenses.collectAsStateWithLifecycle()
    var pendingRestore by remember { mutableStateOf<DeletedExpense?>(null) }

    Column(Modifier.fillMaxSize()) {
        LedgerLeafTopBar("Recycle Bin")
        Text(
            "Deleted expenses are recoverable for 6 months, then removed automatically.",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (items.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
                Text("Recycle Bin is empty.", style = MaterialTheme.typography.titleMedium)
                Text("Deleted expenses will appear here without mixing into active history or search.")
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(items, key = { it.expense.id }) { item ->
                    RecycleBinRow(item) { pendingRestore = item }
                }
            }
        }
    }

    pendingRestore?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingRestore = null },
            title = { Text("Restore expense?") },
            text = { Text("This expense will return to active LedgerLeaf records.") },
            confirmButton = {
                Button(onClick = { viewModel.restore(item.expense.id); pendingRestore = null }) { Text("Restore") }
            },
            dismissButton = { TextButton(onClick = { pendingRestore = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun RecycleBinRow(item: DeletedExpense, onRestore: () -> Unit) {
    val expense = item.expense
    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f)) {
            Text(expense.category.name, fontWeight = FontWeight.SemiBold)
            Text(expense.notes, maxLines = 1, overflow = TextOverflow.Ellipsis)
            val deleted = Instant.ofEpochMilli(item.deletedAtEpochMillis).atZone(ZoneId.systemDefault()).toLocalDateTime()
            Text("Deleted ${DateFormatter.formatDateTime(deleted)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(CurrencyFormatter.format(BigDecimal.valueOf(expense.amountMinor, 2), expense.currencyCode), fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = onRestore) { Text("Restore") }
    }
    HorizontalDivider()
}
