package com.ledgerleaf.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ledgerleaf.core.ui.theme.LedgerAmber
import com.ledgerleaf.core.ui.theme.LedgerDeepGreen
import com.ledgerleaf.core.ui.theme.LedgerRed
import com.ledgerleaf.core.utils.CurrencyFormatter
import com.ledgerleaf.core.utils.DateFormatter
import com.ledgerleaf.domain.model.Expense
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId

@Composable
fun DashboardScreen(
    onReportsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onExpenseClick: (String) -> Unit,
    onHistoryClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onRecurringClick: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(Modifier.height(12.dp))
            DashboardHeader(onReportsClick, onSettingsClick)
        }
        item { MonthlyOverviewCard(state) }
        item { QuickAccessSection(state, onFavoritesClick, onRecurringClick) }
        if (state.showMonthlyClosingBanner) {
            item { MonthlyClosingBanner(onHistoryClick) }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Recent Expenses",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "View history",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.clickable(onClick = onHistoryClick).padding(8.dp)
                )
            }
        }
        if (state.recent.isEmpty()) {
            item { EmptyRecentCard() }
        } else {
            items(state.recent, key = { it.id }) { expense ->
                RecentExpenseRow(expense, onExpenseClick)
            }
        }
        item { Spacer(Modifier.height(18.dp)) }
    }
}

@Composable
private fun MonthlyOverviewCard(state: DashboardUiState) {
    val currency = state.preferences.currencyCode
    val budget = state.preferences.monthlyBudgetMinor
    val income = state.preferences.monthlyIncomeMinor
    val progress = state.budgetProgress
    val progressColor = when {
        progress == null -> MaterialTheme.colorScheme.primary
        progress < 0.65f -> LedgerDeepGreen
        progress < 0.9f -> LedgerAmber
        else -> LedgerRed
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("This Month", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(state.periodLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                money(state.monthTotalMinor, currency),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text("${state.transactionCount} transaction${if (state.transactionCount == 1) "" else "s"}", style = MaterialTheme.typography.bodyMedium)

            if (budget != null) {
                HorizontalDivider()
                SummaryRow("Budget", money(budget, currency))
                SummaryRow("Remaining", signedMoney(state.remainingBudgetMinor ?: 0L, currency), valueColor = if ((state.remainingBudgetMinor ?: 0L) >= 0L) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                LinearProgressIndicator(
                    progress = { (progress ?: 0f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(99.dp)),
                    color = progressColor
                )
                if ((progress ?: 0f) > 1f) {
                    Text("Budget exceeded", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
                }
            }

            if (income != null) {
                HorizontalDivider()
                SummaryRow("Income", money(income, currency))
                SummaryRow(
                    "Savings",
                    signedMoney(state.savingsMinor ?: 0L, currency),
                    valueColor = if ((state.savingsMinor ?: 0L) >= 0L) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

@Composable
private fun QuickAccessSection(state: DashboardUiState, onFavoritesClick: () -> Unit, onRecurringClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Quick Access", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickAccessCard("Favorites", state.favoriteCount.toString(), Modifier.weight(1f), onFavoritesClick)
            QuickAccessCard("Recurring Due", state.recurringDueCount.toString(), Modifier.weight(1f), onRecurringClick)
        }
        Surface(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onFavoritesClick),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(Modifier.padding(14.dp)) {
                Text("Frequently Used", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(
                    state.frequentLabel ?: "No repeated expense pattern yet",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun QuickAccessCard(title: String, value: String, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MonthlyClosingBanner(onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Monthly Closing", fontWeight = FontWeight.Bold)
            Text("Last month's ledger is ready for review.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun RecentExpenseRow(expense: Expense, onExpenseClick: (String) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onExpenseClick(expense.id) },
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(expense.category.name, fontWeight = FontWeight.SemiBold)
                    Text(expense.notes, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(DateFormatter.formatDateTime(Instant.ofEpochMilli(expense.occurredAtEpochMillis).atZone(ZoneId.systemDefault()).toLocalDateTime()), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(money(expense.amountMinor, expense.currencyCode), fontWeight = FontWeight.Bold)
            }
            HorizontalDivider()
        }
    }
}

@Composable
private fun EmptyRecentCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(Modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
            Text("Your ledger is empty.")
        }
    }
}

@Composable
private fun DashboardHeader(onReportsClick: () -> Unit, onSettingsClick: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 10.dp, vertical = 7.dp)
        ) {
            Text("LL", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Black)
        }
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text("LedgerLeaf", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("My Personal Ledger", style = MaterialTheme.typography.bodySmall)
        }
        IconButton(onClick = onReportsClick) { Text("▧", fontSize = 22.sp, fontWeight = FontWeight.Bold) }
        IconButton(onClick = onSettingsClick) { Text("⚙", fontSize = 21.sp) }
    }
}

private fun money(amountMinor: Long, currency: String): String =
    CurrencyFormatter.format(BigDecimal.valueOf(amountMinor, 2), currency)

private fun signedMoney(amountMinor: Long, currency: String): String = when {
    amountMinor < 0L -> "-${money(-amountMinor, currency)}"
    else -> money(amountMinor, currency)
}
