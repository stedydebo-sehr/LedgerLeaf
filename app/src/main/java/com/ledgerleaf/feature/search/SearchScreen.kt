package com.ledgerleaf.feature.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
fun SearchScreen(
    onExpenseClick: (String) -> Unit = {},
    embedded: Boolean = false,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        if (!embedded) LedgerLeafTopBar("Search & Filters")
        LazyColumn(if (embedded) Modifier.fillMaxSize() else Modifier.fillMaxSize().imePadding()) {
            item {
                SearchControls(state, viewModel)
            }

            if (!state.filters.canSearch) {
                item {
                    SearchMessage("Search notes, or select at least one filter to view matching expenses.")
                }
            } else {
                val amountError = state.amountError
                if (amountError != null) {
                    item {
                        SearchMessage(amountError, isError = true)
                    }
                } else {
                item {
                    ResultsSummary(state)
                }
                if (state.results.isEmpty()) {
                    item { SearchMessage("No expenses match the current search and filters.") }
                } else {
                    items(state.results, key = { it.id }) { expense ->
                        SearchResultRow(expense, onExpenseClick)
                    }
                }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun SearchControls(state: SearchUiState, viewModel: SearchViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = state.filters.notesQuery,
            onValueChange = viewModel::setNotesQuery,
            label = { Text("Search notes") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        FilterSectionTitle("Date")
        ScrollableChoices {
            SearchDateFilter.entries.forEach { option ->
                FilterChip(
                    selected = state.filters.dateFilter == option,
                    onClick = { viewModel.setDateFilter(option) },
                    label = { Text(option.label) }
                )
            }
        }

        FilterSectionTitle("Category")
        ScrollableChoices {
            FilterChip(
                selected = state.filters.categoryId == null,
                onClick = { viewModel.setCategory(null) },
                label = { Text("Any") }
            )
            state.categories.forEach { choice ->
                FilterChip(
                    selected = state.filters.categoryId == choice.id,
                    onClick = { viewModel.setCategory(choice.id) },
                    label = { Text(choice.label) }
                )
            }
        }

        FilterSectionTitle("Payment method")
        ScrollableChoices {
            FilterChip(
                selected = state.filters.paymentMethodId == null,
                onClick = { viewModel.setPaymentMethod(null) },
                label = { Text("Any") }
            )
            state.paymentMethods.forEach { choice ->
                FilterChip(
                    selected = state.filters.paymentMethodId == choice.id,
                    onClick = { viewModel.setPaymentMethod(choice.id) },
                    label = { Text(choice.label) }
                )
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = state.filters.minimumAmount,
                onValueChange = viewModel::setMinimumAmount,
                label = { Text("Min amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = state.filters.maximumAmount,
                onValueChange = viewModel::setMaximumAmount,
                label = { Text("Max amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
        state.amountError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CheckboxFilter(
                label = "Favorites only",
                checked = state.filters.favoritesOnly,
                onCheckedChange = viewModel::setFavoritesOnly,
                modifier = Modifier.weight(1f)
            )
            CheckboxFilter(
                label = "Recurring only",
                checked = state.filters.recurringOnly,
                onCheckedChange = viewModel::setRecurringOnly,
                modifier = Modifier.weight(1f)
            )
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = viewModel::clearFilters) { Text("Clear all") }
        }
        HorizontalDivider()
    }
}

@Composable
private fun FilterSectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun ScrollableChoices(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

@Composable
private fun CheckboxFilter(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ResultsSummary(state: SearchUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("${state.results.size} result${if (state.results.size == 1) "" else "s"}", fontWeight = FontWeight.SemiBold)
            Text(money(state.resultTotalMinor, state.currencyCode), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SearchMessage(message: String, isError: Boolean = false) {
    Text(
        text = message,
        modifier = Modifier.padding(24.dp),
        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun SearchResultRow(expense: Expense, onExpenseClick: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onExpenseClick(expense.id) }.padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(expense.category.name, fontWeight = FontWeight.SemiBold)
            Text(expense.notes, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                "${expense.paymentMethod.name} · ${DateFormatter.formatDateTime(Instant.ofEpochMilli(expense.occurredAtEpochMillis).atZone(ZoneId.systemDefault()).toLocalDateTime())}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(money(expense.amountMinor, expense.currencyCode), fontWeight = FontWeight.Bold)
    }
    HorizontalDivider()
}

private fun money(amountMinor: Long, currencyCode: String): String =
    CurrencyFormatter.format(BigDecimal.valueOf(amountMinor, 2), currencyCode)
