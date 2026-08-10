package com.ledgerleaf.feature.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
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
import com.ledgerleaf.core.ui.theme.LedgerMarginLine
import com.ledgerleaf.core.ui.theme.LedgerRed
import com.ledgerleaf.core.ui.theme.LedgerSpiralMetal
import com.ledgerleaf.core.utils.CurrencyFormatter
import com.ledgerleaf.domain.model.Expense
import com.ledgerleaf.feature.search.SearchScreen
import com.ledgerleaf.feature.reports.ReportsScreen
import com.ledgerleaf.feature.history.HistoryScreen
import com.ledgerleaf.feature.budget.BudgetsScreen
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun DashboardScreen(
    section: NotebookSection,
    onSectionChange: (NotebookSection) -> Unit,
    onSettingsClick: () -> Unit,
    onExpenseClick: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = MaterialTheme.colorScheme.background.red < 0.15f

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF050605) else Color(0xFF8A6B45))
    ) {
        val showSideTabs = maxWidth > 420.dp
        val frameEnd = if (showSideTabs) 56.dp else 8.dp

        LedgerPaperFrame(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 8.dp, end = frameEnd, top = 48.dp, bottom = 8.dp),
            isDark = isDark
        ) {
            if (section == NotebookSection.Ledger) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 54.dp, end = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item { Spacer(Modifier.height(16.dp)) }
                    item {
                        LedgerHeader(
                            isDark = isDark,
                            onCurrencyClick = onSettingsClick,
                            onSettingsClick = onSettingsClick
                        )
                    }
                    item {
                        MonthHeader(
                            title = state.periodTitle,
                            range = state.periodLabel,
                            onPrevious = viewModel::previousMonth,
                            onNext = viewModel::nextMonth
                        )
                    }
                    item { StatsCard(state) }
                    item { BudgetProgressCard(state) }
                    item {
                        RecentEntriesCard(
                            expenses = state.recent,
                            currency = state.preferences.currencyCode,
                            onViewAll = { onSectionChange(NotebookSection.History) },
                            onExpenseClick = onExpenseClick
                        )
                    }
                    item { MandatoryNotesFooter() }
                    item { Spacer(Modifier.height(22.dp)) }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 54.dp, end = 12.dp, top = 16.dp, bottom = 8.dp)
                ) {
                    Text(
                        section.label,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                    )
                    Box(Modifier.weight(1f).fillMaxWidth()) {
                        when (section) {
                            NotebookSection.History -> HistoryScreen(
                                onExpenseClick = onExpenseClick,
                                embedded = true
                            )
                            NotebookSection.Search -> SearchScreen(
                                onExpenseClick = onExpenseClick,
                                embedded = true
                            )
                            NotebookSection.Reports -> ReportsScreen(embedded = true)
                            NotebookSection.Budgets -> BudgetsScreen(embedded = true)
                            NotebookSection.Ledger -> Unit
                        }
                    }
                }
            }
        }

        if (showSideTabs) {
            SideTabs(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 112.dp, end = 6.dp),
                isDark = isDark,
                selected = section,
                onSelect = onSectionChange
            )
        }
    }
}

@Composable
private fun LedgerPaperFrame(modifier: Modifier, isDark: Boolean, content: @Composable () -> Unit) {
    val ruleColor = if (isDark) Color(0x0DC8C8AA) else Color(0x125A4628)
    val marginColor = if (isDark) DarkMarginLine else LedgerMarginLine
    val metal = if (isDark) DarkSpiralMetal else LedgerSpiralMetal
    val hole = if (isDark) Color(0xFF050605) else Color(0xFF8A6B45)

    Box(
        modifier
            .shadow(18.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    if (isDark) listOf(Color(0xFF12140F), Color(0xFF14170F))
                    else listOf(Color(0xFFF4EFE1), Color(0xFFF7F2E6))
                )
            )
            .drawBehind {
                val step = 28.dp.toPx()
                var y = step
                while (y < size.height) {
                    drawLine(ruleColor, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
                    y += step
                }
                drawLine(marginColor, Offset(52.dp.toPx(), 0f), Offset(52.dp.toPx(), size.height), 1.dp.toPx())
            }
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .width(34.dp)
                .background(Color.Black.copy(alpha = if (isDark) .14f else .07f))
        )
        Column(
            modifier = Modifier.padding(start = 8.dp, top = 26.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            repeat(13) {
                Box(
                    Modifier
                        .size(17.dp)
                        .background(hole, CircleShape)
                        .border(3.dp, metal, CircleShape)
                )
            }
        }
        content()
    }
}

@Composable
private fun LedgerHeader(
    isDark: Boolean,
    onCurrencyClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Image(
            painter = painterResource(if (isDark) R.drawable.ledgerleaf_logo_dark else R.drawable.ledgerleaf_logo_light),
            contentDescription = "LedgerLeaf logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
        )
        Column(Modifier.weight(1f).padding(start = 9.dp)) {
            Text(
                "LedgerLeaf",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                letterSpacing = .3.sp
            )
            Text(
                "My Personal Ledger",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = FontStyle.Italic
            )
        }
        LedgerGlyphButton("▤", "Currency", onCurrencyClick)
        Spacer(Modifier.width(8.dp))
        LedgerIconButton(Icons.Default.Settings, "Settings", onSettingsClick)
    }
}

@Composable
private fun LedgerGlyphButton(glyph: String, description: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .size(38.dp)
            .semantics { contentDescription = description; role = Role.Button }
            .clickable(onClick = onClick),
        color = Color.Transparent,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(glyph, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 19.sp)
        }
    }
}

@Composable
private fun LedgerIconButton(image: androidx.compose.ui.graphics.vector.ImageVector, description: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .size(38.dp)
            .semantics { contentDescription = description; role = Role.Button }
            .clickable(onClick = onClick),
        color = Color.Transparent,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(image, description, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun MonthHeader(title: String, range: String, onPrevious: () -> Unit, onNext: () -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(
                Icons.Default.KeyboardArrowLeft,
                "Previous month",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(25.dp).clickable(onClick = onPrevious).padding(2.dp)
            )
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Icon(
                Icons.Default.KeyboardArrowRight,
                "Next month",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(25.dp).clickable(onClick = onNext).padding(2.dp)
            )
        }
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
    LedgerCard {
        Row(Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 4.dp)) {
            StatCell("BUDGET", "▣", state.preferences.monthlyBudgetMinor?.let { money(it, currency) } ?: "—", Modifier.weight(1f))
            DottedDivider()
            StatCell("SPENT", "⌑", money(state.monthTotalMinor, currency), Modifier.weight(1f), MaterialTheme.colorScheme.error)
            DottedDivider()
            StatCell("REMAINING", "▤", state.remainingBudgetMinor?.let { signedMoney(it, currency) } ?: "—", Modifier.weight(1f))
            DottedDivider()
            StatCell("ENTRIES", "▥", state.transactionCount.toString(), Modifier.weight(1f))
        }
    }
}

@Composable
private fun DottedDivider() {
    val color = MaterialTheme.colorScheme.outline.copy(alpha = .45f)
    Canvas(Modifier.width(1.dp).height(70.dp)) {
        var y = 0f
        while (y < size.height) {
            drawLine(color, Offset(0f, y), Offset(0f, (y + 4.dp.toPx()).coerceAtMost(size.height)), 1.dp.toPx())
            y += 8.dp.toPx()
        }
    }
}

@Composable
private fun StatCell(label: String, icon: String, value: String, modifier: Modifier, valueColor: Color? = null) {
    Column(modifier.padding(horizontal = 3.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.5.sp, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.SemiBold)
        Text(icon, fontSize = 19.sp, color = valueColor ?: MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(top = 7.dp, bottom = 5.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleSmall,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = valueColor ?: MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BudgetProgressCard(state: DashboardUiState) {
    val progress = state.budgetProgress
    val budget = state.preferences.monthlyBudgetMinor
    LedgerCard {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Text(
                    "MONTH PROGRESS",
                    modifier = Modifier.weight(1f),
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    if (progress == null) "—" else "${String.format("%.1f", progress * 100f)}%",
                    style = MaterialTheme.typography.titleSmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 17.sp
                )
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { (progress ?: 0f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(9.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .65f),
                strokeCap = StrokeCap.Round
            )
            Spacer(Modifier.height(8.dp))
            Text(
                if (budget == null) "Set a monthly budget in Settings"
                else "${money(state.monthTotalMinor, state.preferences.currencyCode)} of ${money(budget, state.preferences.currencyCode)}",
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
    onViewAll: () -> Unit,
    onExpenseClick: (String) -> Unit
) {
    LedgerCard {
        Column(Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 6.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "RECENT ENTRIES",
                    modifier = Modifier.weight(1f),
                    fontSize = 12.5.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(Modifier.clickable(onClick = onViewAll), verticalAlignment = Alignment.CenterVertically) {
                    Text("View all", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontStyle = FontStyle.Italic)
                    Icon(Icons.Default.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                }
            }
            LedgerHairline(Modifier.padding(top = 10.dp))
            EntryColumnLabels()
            if (expenses.isEmpty()) {
                Text(
                    "No entries in this ledger period.",
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic
                )
            } else {
                expenses.forEach { expense ->
                    LedgerHairline()
                    EntryRow(expense, currency, onExpenseClick)
                }
            }
        }
    }
}

@Composable
private fun EntryColumnLabels() {
    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("Date", Modifier.width(38.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Text("Category", Modifier.weight(1.15f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Text("Note", Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Text("★", Modifier.width(20.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Text("Amount", Modifier.width(68.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.width(14.dp))
    }
}

@Composable
private fun EntryRow(expense: Expense, currency: String, onExpenseClick: (String) -> Unit) {
    val dt = Instant.ofEpochMilli(expense.occurredAtEpochMillis).atZone(ZoneId.systemDefault())
    val amountColor = when {
        expense.amountMinor <= 70_000L -> Color(0xFF3F7A2E).takeIf { MaterialTheme.colorScheme.background.red > .15f } ?: Color(0xFF8FB35C)
        expense.amountMinor <= 100_000L -> LedgerAmber.takeIf { MaterialTheme.colorScheme.background.red > .15f } ?: Color(0xFFD99A4E)
        else -> MaterialTheme.colorScheme.error
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpenseClick(expense.id) }
            .padding(horizontal = 8.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.width(38.dp)) {
            Text(dt.dayOfMonth.toString().padStart(2, '0'), fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(dt.format(DateTimeFormatter.ofPattern("MMM")), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(Modifier.weight(1.15f), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(24.dp),
                shape = CircleShape,
                color = Color.Transparent,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(categoryGlyph(expense.category.name), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(
                expense.category.name,
                modifier = Modifier.padding(start = 7.dp),
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            expense.notes,
            Modifier.weight(1f),
            fontSize = 12.sp,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Box(Modifier.width(20.dp), contentAlignment = Alignment.Center) {
            Icon(
                Icons.Default.Star,
                null,
                tint = if (expense.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = .65f),
                modifier = Modifier.size(15.dp)
            )
        }
        Text(
            money(expense.amountMinor, currency),
            Modifier.width(68.dp),
            textAlign = TextAlign.End,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = amountColor,
            maxLines = 1
        )
        Icon(Icons.Default.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.width(14.dp).size(14.dp))
    }
}

@Composable
private fun LedgerHairline(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = .7f)))
}

@Composable
private fun MandatoryNotesFooter() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .45f))
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("❧", color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
            Spacer(Modifier.width(9.dp))
            Text(
                "Notes are mandatory for every entry.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
private fun LedgerCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = .96f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        content = content
    )
}

@Composable
private fun SideTabs(
    modifier: Modifier,
    isDark: Boolean,
    selected: NotebookSection,
    onSelect: (NotebookSection) -> Unit
) {
    val inactiveBackground = if (isDark) Color(0xFF1C1F16) else Color(0xFFEFE7D3)
    val inactiveInk = if (isDark) Color(0xFFA79F83) else Color(0xFF6B6142)
    val activeBackground = if (isDark) Color(0xFF35431F) else Color(0xFF2D5A1E)
    val activeInk = if (isDark) Color(0xFFEEF2E2) else Color(0xFFF3F0E4)
    val edge = if (isDark) Color(0x334E5C3D) else Color(0x335A4628)

    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        NotebookSection.entries.forEach { tab ->
            val active = tab == selected
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .height(82.dp)
                    .clip(RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 12.dp, bottomEnd = 12.dp))
                    .background(if (active) activeBackground else inactiveBackground)
                    .border(
                        1.dp,
                        edge,
                        RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 12.dp, bottomEnd = 12.dp)
                    )
                    .semantics {
                        contentDescription = "${tab.label} ledger tab"
                        role = Role.Tab
                    }
                    .clickable { onSelect(tab) },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        tab.glyph,
                        modifier = Modifier.height(18.dp),
                        fontSize = 14.sp,
                        lineHeight = 14.sp,
                        color = if (active) activeInk else inactiveInk,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.label.uppercase(),
                            modifier = Modifier
                                .rotate(90f)
                                .requiredWidth(56.dp),
                            fontSize = 9.5.sp,
                            lineHeight = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center,
                            color = if (active) activeInk else inactiveInk,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }
        }
    }
}


private fun categoryGlyph(name: String): String = when {
    name.contains("grocer", true) -> "♧"
    name.contains("util", true) -> "ϟ"
    name.contains("pet", true) -> "♙"
    name.contains("travel", true) -> "▰"
    name.contains("food", true) || name.contains("dining", true) -> "☕"
    name.contains("entertain", true) -> "▶"
    name.contains("medical", true) -> "+"
    name.contains("maint", true) -> "⌁"
    else -> "•"
}

private fun money(amountMinor: Long, currency: String): String =
    CurrencyFormatter.format(BigDecimal.valueOf(amountMinor, 2), currency)

private fun signedMoney(amountMinor: Long, currency: String): String =
    if (amountMinor < 0L) "-${money(-amountMinor, currency)}" else money(amountMinor, currency)
