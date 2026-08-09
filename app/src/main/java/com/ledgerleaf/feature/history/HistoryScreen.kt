package com.ledgerleaf.feature.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
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
import com.ledgerleaf.domain.model.Expense
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId

@Composable
fun HistoryScreen(
    onExpenseClick: (String) -> Unit = {},
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        LedgerLeafTopBar("History")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HistoryGroup.entries.forEach { item ->
                FilterChip(
                    selected = state.group == item,
                    onClick = { viewModel.setGroup(item) },
                    label = { Text(item.label) }
                )
            }
        }

        HistorySummary(
            count = state.expenseCount,
            totalMinor = state.totalMinor,
            currencyCode = state.currencyCode
        )

        if (state.sections.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No expenses yet.", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Saved expenses will appear here automatically.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                state.sections.forEach { section ->
                    item(key = "header-${section.key}") {
                        SectionHeader(section, state.currencyCode)
                    }
                    items(section.expenses, key = { it.id }) { expense ->
                        ExpenseRow(expense, onExpenseClick)
                    }
                }
                item { Spacer(Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
private fun HistorySummary(count: Int, totalMinor: Long, currencyCode: String) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("All active expenses", style = MaterialTheme.typography.labelLarge)
                Text("$count transaction${if (count == 1) "" else "s"}", style = MaterialTheme.typography.bodySmall)
            }
            Text(money(totalMinor, currencyCode), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SectionHeader(section: HistorySection, currencyCode: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(Modifier.weight(1f)) {
            Text(section.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "${section.expenses.size} transaction${if (section.expenses.size == 1) "" else "s"}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(money(section.totalMinor, currencyCode), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ExpenseRow(expense: Expense, onExpenseClick: (String) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onExpenseClick(expense.id) }
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(expense.category.name, fontWeight = FontWeight.SemiBold)
            Text(
                expense.notes,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                DateFormatter.formatDateTime(
                    Instant.ofEpochMilli(expense.occurredAtEpochMillis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(money(expense.amountMinor, expense.currencyCode), fontWeight = FontWeight.Bold)
    }
    HorizontalDivider()
}

private fun money(amountMinor: Long, currencyCode: String): String =
    CurrencyFormatter.format(BigDecimal.valueOf(amountMinor, 2), currencyCode)
