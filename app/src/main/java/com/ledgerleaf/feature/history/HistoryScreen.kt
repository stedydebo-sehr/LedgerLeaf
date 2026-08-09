package com.ledgerleaf.feature.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ledgerleaf.core.ui.components.LedgerLeafTopBar
import com.ledgerleaf.core.utils.CurrencyFormatter
import com.ledgerleaf.domain.model.Expense
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

private enum class HistoryGroup { DAY, WEEK, MONTH, YEAR }

@Composable
fun HistoryScreen(
    onExpenseClick: (String) -> Unit = {},
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    var group by remember { mutableStateOf(HistoryGroup.DAY) }

    Column(Modifier.fillMaxSize()) {
        LedgerLeafTopBar("History")
        Row(Modifier.fillMaxWidth().padding(8.dp)) {
            HistoryGroup.entries.forEach { item ->
                FilterChip(
                    selected = group == item,
                    onClick = { group = item },
                    label = { Text(item.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    modifier = Modifier.padding(horizontal = 3.dp)
                )
            }
        }

        val grouped = expenses.groupBy { groupKey(it, group) }.toList()
        LazyColumn(Modifier.fillMaxSize()) {
            grouped.forEach { (header, entries) ->
                item {
                    Text(
                        header,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                items(entries, key = { it.id }) { expense ->
                    ExpenseRow(expense, onExpenseClick)
                }
            }
            if (expenses.isEmpty()) {
                item { Text("No expenses yet.", modifier = Modifier.padding(24.dp)) }
            }
        }
    }
}

@Composable
private fun ExpenseRow(expense: Expense, onExpenseClick: (String) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onExpenseClick(expense.id) }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(expense.category.name, fontWeight = FontWeight.SemiBold)
            Text(expense.notes, style = MaterialTheme.typography.bodySmall)
        }
        Text(
            CurrencyFormatter.format(BigDecimal.valueOf(expense.amountMinor, 2), expense.currencyCode),
            fontWeight = FontWeight.Bold
        )
    }
    HorizontalDivider()
}

private fun groupKey(expense: Expense, group: HistoryGroup): String {
    val date = Instant.ofEpochMilli(expense.occurredAtEpochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

    return when (group) {
        HistoryGroup.DAY -> date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
        HistoryGroup.WEEK -> "Week ${date.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear())}, ${date.year}"
        HistoryGroup.MONTH -> date.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        HistoryGroup.YEAR -> date.year.toString()
    }
}
