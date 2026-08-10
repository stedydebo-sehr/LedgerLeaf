package com.ledgerleaf.feature.reports

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.ledgerleaf.core.utils.DateFormatter
import com.ledgerleaf.domain.model.ExpenseReport
import com.ledgerleaf.domain.model.ReportBreakdownItem
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun ReportsScreen(viewModel: ReportsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var startText by remember(state.startDate) { mutableStateOf(state.startDate.toString()) }
    var endText by remember(state.endDate) { mutableStateOf(state.endDate.toString()) }
    var inputError by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val createPdf = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri -> uri?.let(viewModel::exportPdf) }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Column(Modifier.fillMaxSize()) {
        LedgerLeafTopBar("Reports")
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                startText,
                { startText = it },
                label = { Text("Start YYYY-MM-DD") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                endText,
                { endText = it },
                label = { Text("End YYYY-MM-DD") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
        (inputError ?: state.error)?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val start = runCatching { LocalDate.parse(startText.trim()) }.getOrNull()
                    val end = runCatching { LocalDate.parse(endText.trim()) }.getOrNull()
                    inputError = if (start == null || end == null) "Use dates in YYYY-MM-DD format." else null
                    if (start != null && end != null) viewModel.generate(start, end)
                },
                enabled = !state.isGenerating && !state.isExporting,
                modifier = Modifier.weight(1f)
            ) { Text(if (state.isGenerating) "Generating…" else "Generate report") }

            OutlinedButton(
                onClick = {
                    val report = state.report ?: return@OutlinedButton
                    createPdf.launch(defaultPdfName(report))
                },
                enabled = state.report != null && !state.isGenerating && !state.isExporting,
                modifier = Modifier.weight(1f)
            ) { Text(if (state.isExporting) "Exporting…" else "Export PDF") }
        }

        val report = state.report
        if (report == null) {
            Text(
                "Generate an offline report for any date range. Archived records are included; Recycle Bin records are excluded.",
                modifier = Modifier.padding(24.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            ReportContent(report)
        }
        SnackbarHost(snackbarHostState)
    }
}

@Composable
private fun ReportContent(report: ExpenseReport) {
    LazyColumn(Modifier.fillMaxSize().imePadding()) {
        item {
            Surface(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val start = Instant.ofEpochMilli(report.fromEpochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                    val end = Instant.ofEpochMilli(report.toEpochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                    Text("Report summary", fontWeight = FontWeight.Bold)
                    Text("${DateFormatter.formatDate(start)} – ${DateFormatter.formatDate(end)}")
                    Text(money(report.totalMinor, report.currencyCode), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("${report.transactionCount} transactions • ${report.archivedTransactionCount} archived")
                    Text("Average ${money(report.averageMinor, report.currencyCode)}")
                    report.largestExpense?.let { Text("Largest ${money(it.amountMinor, it.currencyCode)} • ${it.category.name}") }
                }
            }
        }
        item { BreakdownHeader("By category") }
        items(report.categoryBreakdown, key = { "category-${it.label}" }) { BreakdownRow(it, report.currencyCode) }
        item { BreakdownHeader("By payment method") }
        items(report.paymentMethodBreakdown, key = { "payment-${it.label}" }) { BreakdownRow(it, report.currencyCode) }
        if (report.expenses.isEmpty()) {
            item { Text("No reportable expenses in this period.", modifier = Modifier.padding(24.dp)) }
        }
    }
}

@Composable
private fun BreakdownHeader(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
}

@Composable
private fun BreakdownRow(item: ReportBreakdownItem, currencyCode: String) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text(item.label, fontWeight = FontWeight.SemiBold)
            Text("${item.transactionCount} transaction${if (item.transactionCount == 1) "" else "s"}", style = MaterialTheme.typography.labelSmall)
        }
        Text(money(item.amountMinor, currencyCode), fontWeight = FontWeight.Bold)
    }
    HorizontalDivider()
}

private fun defaultPdfName(report: ExpenseReport): String {
    val zone = ZoneId.systemDefault()
    val start = Instant.ofEpochMilli(report.fromEpochMillis).atZone(zone).toLocalDate()
    val end = Instant.ofEpochMilli(report.toEpochMillis).atZone(zone).toLocalDate()
    return "LedgerLeaf_${start}_to_${end}.pdf"
}

private fun money(amountMinor: Long, currencyCode: String): String =
    CurrencyFormatter.format(BigDecimal.valueOf(amountMinor, 2), currencyCode)
