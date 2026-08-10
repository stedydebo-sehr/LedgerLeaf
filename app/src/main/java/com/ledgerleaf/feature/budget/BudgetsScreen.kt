package com.ledgerleaf.feature.budget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import java.math.BigDecimal

@Composable
fun BudgetsScreen(
    embedded: Boolean = false,
    viewModel: BudgetsViewModel = hiltViewModel()
) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var budget by remember(preferences.monthlyBudgetMinor) {
        mutableStateOf(preferences.monthlyBudgetMinor?.let(::minorToInput) ?: "")
    }
    var income by remember(preferences.monthlyIncomeMinor) {
        mutableStateOf(preferences.monthlyIncomeMinor?.let(::minorToInput) ?: "")
    }

    LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Column(Modifier.fillMaxSize()) {
        if (!embedded) LedgerLeafTopBar("Budgets")
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = if (embedded) 4.dp else 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Current monthly plan", fontWeight = FontWeight.Bold)
                    Text(
                        "Budget: ${preferences.monthlyBudgetMinor?.let { money(it, preferences.currencyCode) } ?: "Not set"}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "Income: ${preferences.monthlyIncomeMinor?.let { money(it, preferences.currencyCode) } ?: "Not set"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Ledger period starts on day ${preferences.monthStartDay}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedTextField(
                value = budget,
                onValueChange = { budget = moneyInput(it) },
                label = { Text("Monthly budget") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.saveBudget(budget) }) { Text("Save budget") }
                TextButton(onClick = { budget = ""; viewModel.clearBudget() }) { Text("Clear") }
            }

            OutlinedTextField(
                value = income,
                onValueChange = { income = moneyInput(it) },
                label = { Text("Monthly income (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.saveIncome(income) }) { Text("Save income") }
                TextButton(onClick = { income = ""; viewModel.clearIncome() }) { Text("Clear") }
            }

            Text("Monthly period start", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf(1, 5, 10, 15, 20, 25, 28).forEach { day ->
                    TextButton(onClick = { viewModel.setMonthStartDay(day) }) {
                        Text(if (preferences.monthStartDay == day) "[$day]" else day.toString())
                    }
                }
            }
        }
        SnackbarHost(snackbar)
    }
}

private fun moneyInput(value: String): String {
    val filtered = value.filter { it.isDigit() || it == '.' }
    val firstDot = filtered.indexOf('.')
    if (firstDot < 0) return filtered
    val whole = filtered.substring(0, firstDot)
    val decimals = filtered.substring(firstDot + 1).replace(".", "").take(2)
    return "$whole.$decimals"
}

private fun minorToInput(minor: Long): String =
    BigDecimal.valueOf(minor, 2).stripTrailingZeros().toPlainString()

private fun money(minor: Long, currency: String): String =
    CurrencyFormatter.format(BigDecimal.valueOf(minor, 2), currency)
