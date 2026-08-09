package com.ledgerleaf.feature.recurring

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ledgerleaf.core.ui.components.LedgerLeafTopBar
import com.ledgerleaf.core.utils.CurrencyFormatter
import com.ledgerleaf.core.utils.DateFormatter
import java.math.BigDecimal

@Composable
fun RecurringScreen(
    onExpenseClick: (String) -> Unit,
    onReviewExpense: (String) -> Unit,
    viewModel: RecurringViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(Modifier.fillMaxSize()) {
        item { LedgerLeafTopBar("Recurring Expenses") }
        item {
            Text(
                "Recurring entries are never created automatically. Review and edit every occurrence before saving.",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item { SectionHeader("Due now", state.due.size) }
        if (state.due.isEmpty()) {
            item { EmptySection("Nothing due", "Your recurring ledger is up to date.") }
        } else {
            items(state.due, key = { "due-${it.expense.id}" }) { item ->
                RecurringRow(item, onExpenseClick, onReviewExpense)
            }
        }

        item { SectionHeader("Upcoming", state.upcoming.size) }
        if (state.upcoming.isEmpty()) {
            item { EmptySection("No upcoming recurring expenses", "Turn on Recurring for an expense and choose Weekly or Monthly.") }
        } else {
            items(state.upcoming, key = { "upcoming-${it.expense.id}" }) { item ->
                RecurringRow(item, onExpenseClick, onReviewExpense)
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text(count.toString(), style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun RecurringRow(
    item: RecurringExpenseItem,
    onExpenseClick: (String) -> Unit,
    onReviewExpense: (String) -> Unit
) {
    val expense = item.expense
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Column {
            Row(
                Modifier.fillMaxWidth().clickable { onExpenseClick(expense.id) }.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    Text(expense.category.name, fontWeight = FontWeight.SemiBold)
                    Text(expense.notes, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
                    Text(
                        "${expense.recurringFrequency?.lowercase()?.replaceFirstChar { it.uppercase() }} · ${if (item.isDue) "Due" else "Next"} ${DateFormatter.formatDate(item.nextDueDate)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (item.isDue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(CurrencyFormatter.format(BigDecimal.valueOf(expense.amountMinor, 2), expense.currencyCode), fontWeight = FontWeight.Bold)
            }
            HorizontalDivider()
            Button(
                onClick = { onReviewExpense(expense.id) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)
            ) { Text(if (item.isDue) "Review & add occurrence" else "Review next occurrence") }
        }
    }
}

@Composable
private fun EmptySection(title: String, message: String) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold)
        Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
