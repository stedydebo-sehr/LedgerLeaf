package com.ledgerleaf.feature.monthlyclosing

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.ledgerleaf.domain.model.Expense
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun MonthlyClosingScreen(
    onExpenseClick: (String) -> Unit = {},
    viewModel: MonthlyClosingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var startText by remember(state.startDate) { mutableStateOf(state.startDate.toString()) }
    var endText by remember(state.endDate) { mutableStateOf(state.endDate.toString()) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize()) {
        LedgerLeafTopBar("Monthly Closing")
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = viewModel::previousMonth) { Text("Previous") }
            Text(DateFormatter.formatDate(state.startDate), modifier = Modifier.padding(top = 12.dp), fontWeight = FontWeight.SemiBold)
            TextButton(onClick = viewModel::nextMonth) { Text("Next") }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(startText, { startText = it }, label = { Text("Start YYYY-MM-DD") }, singleLine = true, modifier = Modifier.weight(1f))
            OutlinedTextField(endText, { endText = it }, label = { Text("End YYYY-MM-DD") }, singleLine = true, modifier = Modifier.weight(1f))
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) }
        Button(
            onClick = {
                val start = runCatching { LocalDate.parse(startText.trim()) }.getOrNull()
                val end = runCatching { LocalDate.parse(endText.trim()) }.getOrNull()
                error = when {
                    start == null || end == null -> "Use dates in YYYY-MM-DD format."
                    !viewModel.setPeriod(start, end) -> "End date cannot be before start date."
                    else -> null
                }
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) { Text("Review period") }

        Surface(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
            Column(Modifier.padding(14.dp)) {
                Text("Closing review", fontWeight = FontWeight.Bold)
                Text("${DateFormatter.formatDate(state.startDate)} – ${DateFormatter.formatDate(state.endDate)}")
                Text("${state.expenses.size} transaction${if (state.expenses.size == 1) "" else "s"}")
                Text(money(state.totalMinor, state.currencyCode), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }

        if (state.expenses.isEmpty()) {
            Text("No active expenses in this closing period.", modifier = Modifier.padding(24.dp))
        } else {
            LazyColumn(Modifier.fillMaxSize().imePadding()) {
                items(state.expenses, key = { it.id }) { expense -> ClosingExpenseRow(expense, onExpenseClick) }
            }
        }
    }
}

@Composable
private fun ClosingExpenseRow(expense: Expense, onExpenseClick: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onExpenseClick(expense.id) }.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text(expense.category.name, fontWeight = FontWeight.SemiBold)
            Text(expense.notes, maxLines = 1, overflow = TextOverflow.Ellipsis)
            val date = Instant.ofEpochMilli(expense.occurredAtEpochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
            Text(DateFormatter.formatDate(date), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(money(expense.amountMinor, expense.currencyCode), fontWeight = FontWeight.Bold)
    }
    HorizontalDivider()
}

private fun money(amountMinor: Long, currencyCode: String): String =
    CurrencyFormatter.format(BigDecimal.valueOf(amountMinor, 2), currencyCode)
