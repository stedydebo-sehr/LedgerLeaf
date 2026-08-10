package com.ledgerleaf.feature.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ledgerleaf.R
import com.ledgerleaf.core.ui.theme.DarkMarginLine
import com.ledgerleaf.core.ui.theme.DarkSpiralMetal
import com.ledgerleaf.core.ui.theme.LedgerAmber
import com.ledgerleaf.core.ui.theme.LedgerDeepGreen
import com.ledgerleaf.core.ui.theme.LedgerMarginLine
import com.ledgerleaf.core.ui.theme.LedgerRed
import com.ledgerleaf.core.ui.theme.LedgerSpiralMetal
import com.ledgerleaf.core.utils.CurrencyFormatter
import com.ledgerleaf.domain.model.Expense
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
    val isDark = MaterialTheme.colorScheme.background.red < 0.15f
    val ruleColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.20f else 0.34f)
    val marginColor = if (isDark) DarkMarginLine else LedgerMarginLine

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .drawBehind {
                    val step = 28.dp.toPx()
                    var y = step
                    while (y < size.height) {
                        drawLine(ruleColor, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
                        y += step
                    }
                    val x = 42.dp.toPx()
                    drawLine(marginColor, Offset(x, 0f), Offset(x, size.height), 1.dp.toPx())
                }
        )

        SpiralBinding(isDark)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 50.dp, end = 22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(Modifier.height(18.dp)) }
            item { LedgerHeader(onReportsClick, onSettingsClick) }
            item { MonthHeader(state.periodTitle, state.periodLabel) }
            item { StatsCard(state) }
            item { BudgetProgressCard(state) }
            if (state.showMonthlyClosingBanner) {
                item { MonthlyClosingBanner(onHistoryClick) }
            }
            item { RecentEntriesCard(state.recent, state.preferences.currencyCode, onHistoryClick, onExpenseClick) }
            item { MandatoryNotesFooter() }
            item { Spacer(Modifier.height(18.dp)) }
        }
    }
}

@Composable
private fun SpiralBinding(isDark: Boolean) {
    val metal = if (isDark) DarkSpiralMetal else LedgerSpiralMetal
    val hole = MaterialTheme.colorScheme.background
    Column(
        modifier = Modifier.padding(start = 13.dp, top = 30.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        repeat(12) {
            Box(
                Modifier
                    .size(18.dp)
                    .background(hole, CircleShape)
                    .border(3.dp, metal, CircleShape)
            )
        }
    }
}

@Composable
private fun LedgerHeader(onReportsClick: () -> Unit, onSettingsClick: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        LeafMark()
        Column(Modifier.weight(1f).padding(start = 8.dp)) {
            Text(
                "LedgerLeaf",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                "My Personal Ledger",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = FontStyle.Italic
            )
        }
        LedgerIconButton("▤", "Reports", onReportsClick)
        Spacer(Modifier.width(7.dp))
        LedgerIconButton("⚙", "Settings", onSettingsClick)
    }
}

@Composable
private fun LeafMark() {
    val isDark = MaterialTheme.colorScheme.background.red < 0.15f
    Image(
        painter = painterResource(
            if (isDark) R.drawable.ledgerleaf_logo_dark else R.drawable.ledgerleaf_logo_light
        ),
        contentDescription = "LedgerLeaf logo",
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(9.dp))
    )
}

@Composable
private fun LedgerIconButton(glyph: String, description: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .size(38.dp)
            .semantics { contentDescription = description }
            .clickable(onClick = onClick),
        color = Color.Transparent,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(glyph, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MonthHeader(title: String, range: String) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Text(
            range,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontStyle = FontStyle.Italic
        )
    }
}

@Composable
private fun StatsCard(state: DashboardUiState) {
    val currency = state.preferences.currencyCode
    val budget = state.preferences.monthlyBudgetMinor
    val remaining = state.remainingBudgetMinor
    LedgerCard {
        Row(Modifier.fillMaxWidth().padding(vertical = 15.dp)) {
            StatCell("BUDGET", "▣", budget?.let { money(it, currency) } ?: "—", Modifier.weight(1f))
            DottedDivider()
            StatCell("SPENT", "⌑", money(state.monthTotalMinor, currency), Modifier.weight(1f), LedgerRed)
            DottedDivider()
            StatCell("REMAINING", "▤", remaining?.let { signedMoney(it, currency) } ?: "—", Modifier.weight(1f))
            DottedDivider()
            StatCell("ENTRIES", "▥", state.transactionCount.toString(), Modifier.weight(1f))
        }
    }
}

@Composable
private fun DottedDivider() {
    val color = MaterialTheme.colorScheme.outline.copy(alpha = .42f)
    Canvas(Modifier.width(1.dp).height(68.dp)) {
        var y = 0f
        while (y < size.height) {
            drawLine(color, Offset(0f, y), Offset(0f, (y + 4.dp.toPx()).coerceAtMost(size.height)), 1.dp.toPx())
            y += 8.dp.toPx()
        }
    }
}

@Composable
private fun StatCell(label: String, icon: String, value: String, modifier: Modifier, valueColor: Color? = null) {
    val resolvedValueColor = valueColor ?: MaterialTheme.colorScheme.onSurface
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold)
        Text(icon, fontSize = 18.sp, color = if (resolvedValueColor == LedgerRed) LedgerRed else MaterialTheme.colorScheme.secondary)
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = resolvedValueColor, maxLines = 1)
    }
}

@Composable
private fun BudgetProgressCard(state: DashboardUiState) {
    val budget = state.preferences.monthlyBudgetMinor
    LedgerCard {
        Column(Modifier.padding(15.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Text("MONTH PROGRESS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline, letterSpacing = 1.sp)
                Text(
                    if (budget != null && budget > 0) "${((state.budgetProgress ?: 0f) * 100).coerceAtLeast(0f).toInt()}%" else "—",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontStyle = FontStyle.Italic
                )
            }
            Spacer(Modifier.height(9.dp))
            val p = (state.budgetProgress ?: 0f).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { p },
                modifier = Modifier.fillMaxWidth().height(9.dp).clip(CircleShape),
                color = when {
                    p < .65f -> MaterialTheme.colorScheme.primary
                    p < .90f -> LedgerAmber
                    else -> LedgerRed
                },
                trackColor = MaterialTheme.colorScheme.outlineVariant
            )
            Spacer(Modifier.height(7.dp))
            Text(
                if (budget != null) "${money(state.monthTotalMinor, state.preferences.currencyCode)} of ${money(budget, state.preferences.currencyCode)}" else "Set a monthly budget in Settings",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
private fun RecentEntriesCard(
    expenses: List<Expense>,
    currency: String,
    onHistoryClick: () -> Unit,
    onExpenseClick: (String) -> Unit
) {
    LedgerCard {
        Column {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("RECENT ENTRIES", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(
                    "View all ›",
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onHistoryClick).padding(4.dp)
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            if (expenses.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(vertical = 42.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("▱", fontSize = 32.sp, color = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.height(8.dp))
                        Text("Your ledger is empty.", style = MaterialTheme.typography.bodyLarge, fontStyle = FontStyle.Italic)
                    }
                }
            } else {
                EntryColumnLabels()
                expenses.forEach { expense ->
                    ExpenseTableRow(expense, currency, onExpenseClick)
                }
            }
        }
    }
}

@Composable
private fun EntryColumnLabels() {
    Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 7.dp)) {
        Text("Date", Modifier.width(42.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Text("Category", Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Text("Note", Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Text("Amount", Modifier.width(76.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun ExpenseTableRow(expense: Expense, currency: String, onExpenseClick: (String) -> Unit) {
    val dt = Instant.ofEpochMilli(expense.occurredAtEpochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    val amountMajor = expense.amountMinor / 100.0
    val amountColor = when {
        amountMajor <= 700 -> LedgerDeepGreen
        amountMajor <= 1000 -> LedgerAmber
        else -> LedgerRed
    }
    Column(Modifier.clickable { onExpenseClick(expense.id) }) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.width(42.dp)) {
                Text(dt.dayOfMonth.toString().padStart(2, '0'), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(dt.format(DateTimeFormatter.ofPattern("MMM")), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(expense.category.name, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(expense.notes, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(money(expense.amountMinor, currency), Modifier.width(76.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = amountColor)
        }
    }
}

@Composable
private fun MonthlyClosingBanner(onClick: () -> Unit) {
    LedgerCard(modifier = Modifier.clickable(onClick = onClick)) {
        Column(Modifier.padding(14.dp)) {
            Text("MONTHLY CLOSING", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Last month's ledger is ready for review.", style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
        }
    }
}

@Composable
private fun MandatoryNotesFooter() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .45f))
    ) {
        Row(Modifier.padding(horizontal = 13.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("❧", color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
            Spacer(Modifier.width(8.dp))
            Text("Notes are mandatory for every entry.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontStyle = FontStyle.Italic)
        }
    }
}

@Composable
private fun LedgerCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = .96f),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        content = content
    )
}

private fun money(amountMinor: Long, currency: String): String =
    CurrencyFormatter.format(BigDecimal.valueOf(amountMinor, 2), currency)

private fun signedMoney(amountMinor: Long, currency: String): String =
    if (amountMinor < 0L) "-${money(-amountMinor, currency)}" else money(amountMinor, currency)
