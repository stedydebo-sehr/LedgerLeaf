package com.ledgerleaf.feature.favorites

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
import com.ledgerleaf.domain.model.Expense
import java.math.BigDecimal

@Composable
fun FavoritesScreen(
    onExpenseClick: (String) -> Unit,
    onReuseExpense: (String) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(Modifier.fillMaxSize()) {
        item { LedgerLeafTopBar("Favorites & Frequently Used") }
        item { SectionHeader("Favorites", "Saved shortcuts from your expense history") }
        if (state.favorites.isEmpty()) {
            item { EmptySection("No favorites yet", "Mark an expense as Favorite while adding or editing it.") }
        } else {
            items(state.favorites, key = { "favorite-${it.id}" }) { expense ->
                ExpenseShortcutRow(expense, "Favorite", onExpenseClick, onReuseExpense)
            }
        }

        item { SectionHeader("Frequently Used", "Repeated patterns detected locally from your ledger") }
        if (state.frequent.isEmpty()) {
            item { EmptySection("No repeated patterns yet", "Frequently used shortcuts appear after the same expense pattern is recorded more than once.") }
        } else {
            items(state.frequent, key = { "frequent-${it.representative.id}" }) { pattern ->
                ExpenseShortcutRow(
                    expense = pattern.representative,
                    supporting = "Used ${pattern.useCount} times",
                    onExpenseClick = onExpenseClick,
                    onReuseExpense = onReuseExpense
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ExpenseShortcutRow(
    expense: Expense,
    supporting: String,
    onExpenseClick: (String) -> Unit,
    onReuseExpense: (String) -> Unit
) {
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
                    Text(supporting, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    CurrencyFormatter.format(BigDecimal.valueOf(expense.amountMinor, 2), expense.currencyCode),
                    fontWeight = FontWeight.Bold
                )
            }
            HorizontalDivider()
            Button(
                onClick = { onReuseExpense(expense.id) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)
            ) { Text("Use as new expense") }
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
