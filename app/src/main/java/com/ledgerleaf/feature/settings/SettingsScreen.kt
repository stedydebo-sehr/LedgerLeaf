package com.ledgerleaf.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.ledgerleaf.core.datastore.ThemeMode
import com.ledgerleaf.core.ui.components.LedgerLeafTopBar

@Composable
fun SettingsScreen(
    onSearchClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onRecycleBinClick: () -> Unit,
    onBudgetsClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    var currency by remember(preferences.currencyCode) { mutableStateOf(preferences.currencyCode) }
    var budget by remember(preferences.monthlyBudgetMinor) {
        mutableStateOf(preferences.monthlyBudgetMinor?.let { "%.2f".format(it / 100.0) } ?: "")
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        LedgerLeafTopBar(title = "More")
        SectionTitle("Appearance")
        ThemeMode.entries.forEach { mode ->
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = preferences.themeMode == mode, onClick = { viewModel.setThemeMode(mode) })
                Text(mode.name.lowercase().replaceFirstChar { it.uppercase() })
            }
        }
        HorizontalDivider()
        SectionTitle("Ledger preferences")
        OutlinedTextField(
            value = currency,
            onValueChange = { currency = it.uppercase().take(3) },
            label = { Text("Currency code") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
        )
        Button(onClick = { viewModel.setCurrencyCode(currency) }, modifier = Modifier.padding(horizontal = 16.dp)) { Text("Save currency") }
        OutlinedTextField(
            value = budget,
            onValueChange = { budget = it.filter { ch -> ch.isDigit() || ch == '.' } },
            label = { Text("Monthly budget (optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
        )
        Row(Modifier.padding(horizontal = 16.dp)) {
            Button(onClick = { viewModel.setMonthlyBudget(budget) }) { Text("Save budget") }
            TextButton(onClick = { budget = ""; viewModel.clearMonthlyBudget() }) { Text("Clear") }
        }
        Text("Monthly period starts on day ${preferences.monthStartDay}", modifier = Modifier.padding(16.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            listOf(1, 5, 10, 15, 20, 25, 28).forEach { day ->
                TextButton(onClick = { viewModel.setMonthStartDay(day) }) { Text(day.toString()) }
            }
        }
        HorizontalDivider()
        SectionTitle("Data & tools")
        SettingsEntry("Search", onSearchClick)
        SettingsEntry("Budgets", onBudgetsClick)
        SettingsEntry("Archive", onArchiveClick)
        SettingsEntry("Recycle Bin", onRecycleBinClick)
    }
}

@Composable private fun SectionTitle(title: String) { Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp)) }
@Composable private fun SettingsEntry(title: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, modifier = Modifier.fillMaxWidth())
    }
    HorizontalDivider()
}
