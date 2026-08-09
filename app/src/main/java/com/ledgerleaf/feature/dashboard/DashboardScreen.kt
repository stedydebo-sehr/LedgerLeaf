package com.ledgerleaf.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ledgerleaf.core.utils.CurrencyFormatter
import java.math.BigDecimal

@Composable
fun DashboardScreen(onReportsClick: () -> Unit, onSettingsClick: () -> Unit, viewModel: DashboardViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp)) {
        DashboardHeader(onReportsClick, onSettingsClick)
        Spacer(Modifier.height(22.dp))
        Text("This Month", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(10.dp))
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(18.dp)) {
                Text(CurrencyFormatter.format(BigDecimal.valueOf(state.monthTotalMinor, 2), state.preferences.currencyCode), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                state.preferences.monthlyBudgetMinor?.let { budget -> Text("Budget: ${CurrencyFormatter.format(BigDecimal.valueOf(budget, 2), state.preferences.currencyCode)}") }
            }
        }
        Spacer(Modifier.height(22.dp)); Text("Recent Entries", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(10.dp)); HorizontalDivider()
        if (state.recent.isEmpty()) Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) { Text("Your ledger is empty.") }
        else state.recent.forEach { expense ->
            Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text(expense.category.name, fontWeight = FontWeight.SemiBold); Text(expense.notes, style = MaterialTheme.typography.bodySmall, maxLines = 1) }
                Text(CurrencyFormatter.format(BigDecimal.valueOf(expense.amountMinor, 2), expense.currencyCode), fontWeight = FontWeight.Bold)
            }
            HorizontalDivider()
        }
    }
}

@Composable private fun DashboardHeader(onReportsClick: () -> Unit, onSettingsClick: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primary).padding(horizontal = 10.dp, vertical = 7.dp)) { Text("LL", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Black) }
        Column(Modifier.weight(1f).padding(start = 12.dp)) { Text("LedgerLeaf", fontSize = 24.sp, fontWeight = FontWeight.Bold); Text("My Personal Ledger", style = MaterialTheme.typography.bodySmall) }
        IconButton(onClick = onReportsClick) { Text("▧", fontSize = 22.sp, fontWeight = FontWeight.Bold) }
        IconButton(onClick = onSettingsClick) { Text("⚙", fontSize = 21.sp) }
    }
}
