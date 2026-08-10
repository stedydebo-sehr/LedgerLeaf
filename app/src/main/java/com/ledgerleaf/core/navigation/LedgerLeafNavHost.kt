package com.ledgerleaf.core.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ledgerleaf.feature.archive.ArchiveScreen
import com.ledgerleaf.feature.backup.BackupScreen
import com.ledgerleaf.feature.budget.BudgetsScreen
import com.ledgerleaf.feature.dashboard.DashboardScreen
import com.ledgerleaf.feature.dashboard.NotebookSection
import com.ledgerleaf.feature.favorites.FavoritesScreen
import com.ledgerleaf.feature.history.HistoryScreen
import com.ledgerleaf.feature.monthlyclosing.MonthlyClosingScreen
import com.ledgerleaf.feature.recurring.RecurringScreen
import com.ledgerleaf.feature.recyclebin.RecycleBinScreen
import com.ledgerleaf.feature.reports.ReportsScreen
import com.ledgerleaf.feature.search.SearchScreen
import com.ledgerleaf.feature.settings.SettingsScreen
import com.ledgerleaf.feature.transaction.AddExpenseScreen
import com.ledgerleaf.feature.transaction.EditExpenseScreen
import com.ledgerleaf.feature.transaction.ExpenseDetailsScreen

@Composable
fun LedgerLeafNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    var notebookSection by rememberSaveable { mutableStateOf(NotebookSection.Ledger) }

    val openNotebook: (NotebookSection) -> Unit = { section ->
        notebookSection = section
        if (currentRoute != LedgerLeafDestination.Dashboard.route) {
            navController.navigate(LedgerLeafDestination.Dashboard.route) {
                popUpTo(LedgerLeafDestination.Dashboard.route) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            LedgerBottomNavigation(
                selectedSection = notebookSection,
                notebookVisible = currentRoute == LedgerLeafDestination.Dashboard.route,
                moreSelected = currentRoute == LedgerLeafDestination.Settings.route,
                onSelectSection = openNotebook,
                onMore = { navController.navigate(LedgerLeafDestination.Settings.route) { launchSingleTop = true } },
                onAdd = { navController.navigate(LedgerLeafDestination.AddExpense.route) }
            )
        }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            NavHost(navController, startDestination = LedgerLeafDestination.Dashboard.route) {
                composable(LedgerLeafDestination.Dashboard.route) {
                    DashboardScreen(
                        section = notebookSection,
                        onSectionChange = { notebookSection = it },
                        onSettingsClick = { navController.navigate(LedgerLeafDestination.Settings.route) },
                        onExpenseClick = { expenseId ->
                            navController.navigate(LedgerLeafDestination.ExpenseDetails.createRoute(expenseId))
                        }
                    )
                }
                composable(LedgerLeafDestination.History.route) {
                    HistoryScreen(onExpenseClick = { navController.navigate(LedgerLeafDestination.ExpenseDetails.createRoute(it)) })
                }
                composable(LedgerLeafDestination.AddExpense.route) {
                    AddExpenseScreen(onSaved = { navController.popBackStack() })
                }
                composable(LedgerLeafDestination.AddExpenseFromTemplate.route) { backStackEntry ->
                    AddExpenseScreen(
                        onSaved = { navController.popBackStack() },
                        templateExpenseId = backStackEntry.arguments?.getString("expenseId")
                    )
                }
                composable(LedgerLeafDestination.ExpenseDetails.route) {
                    ExpenseDetailsScreen(
                        onEdit = { navController.navigate(LedgerLeafDestination.EditExpense.createRoute(it)) },
                        onDeleted = { openNotebook(NotebookSection.History) },
                        onArchived = { openNotebook(NotebookSection.History) }
                    )
                }
                composable(LedgerLeafDestination.EditExpense.route) {
                    EditExpenseScreen(onSaved = { navController.popBackStack() })
                }
                composable(LedgerLeafDestination.Reports.route) { ReportsScreen() }
                composable(LedgerLeafDestination.Settings.route) {
                    SettingsScreen(
                        onSearchClick = { openNotebook(NotebookSection.Search) },
                        onArchiveClick = { navController.navigate(LedgerLeafDestination.Archive.route) },
                        onRecycleBinClick = { navController.navigate(LedgerLeafDestination.RecycleBin.route) },
                        onBudgetsClick = { openNotebook(NotebookSection.Budgets) },
                        onFavoritesClick = { navController.navigate(LedgerLeafDestination.Favorites.route) },
                        onRecurringClick = { navController.navigate(LedgerLeafDestination.Recurring.route) },
                        onMonthlyClosingClick = { navController.navigate(LedgerLeafDestination.MonthlyClosing.route) },
                        onBackupRestoreClick = { navController.navigate(LedgerLeafDestination.BackupRestore.route) }
                    )
                }
                composable(LedgerLeafDestination.Search.route) {
                    SearchScreen(onExpenseClick = { navController.navigate(LedgerLeafDestination.ExpenseDetails.createRoute(it)) })
                }
                composable(LedgerLeafDestination.Archive.route) { ArchiveScreen() }
                composable(LedgerLeafDestination.RecycleBin.route) { RecycleBinScreen() }
                composable(LedgerLeafDestination.Budgets.route) { BudgetsScreen() }
                composable(LedgerLeafDestination.Favorites.route) {
                    FavoritesScreen(
                        onExpenseClick = { navController.navigate(LedgerLeafDestination.ExpenseDetails.createRoute(it)) },
                        onReuseExpense = { navController.navigate(LedgerLeafDestination.AddExpenseFromTemplate.createRoute(it)) }
                    )
                }
                composable(LedgerLeafDestination.MonthlyClosing.route) {
                    MonthlyClosingScreen(onExpenseClick = { navController.navigate(LedgerLeafDestination.ExpenseDetails.createRoute(it)) })
                }
                composable(LedgerLeafDestination.BackupRestore.route) { BackupScreen() }
                composable(LedgerLeafDestination.Recurring.route) {
                    RecurringScreen(
                        onExpenseClick = { navController.navigate(LedgerLeafDestination.ExpenseDetails.createRoute(it)) },
                        onReviewExpense = { navController.navigate(LedgerLeafDestination.AddExpenseFromTemplate.createRoute(it)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LedgerBottomNavigation(
    selectedSection: NotebookSection,
    notebookVisible: Boolean,
    moreSelected: Boolean,
    onSelectSection: (NotebookSection) -> Unit,
    onMore: () -> Unit,
    onAdd: () -> Unit
) {
    Surface(
        modifier = Modifier.padding(horizontal = 8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 5.dp
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            LedgerNavItem("⌂", "Home", notebookVisible && selectedSection == NotebookSection.Ledger) {
                onSelectSection(NotebookSection.Ledger)
            }
            LedgerNavItem("▣", "History", notebookVisible && selectedSection == NotebookSection.History) {
                onSelectSection(NotebookSection.History)
            }
            Surface(
                modifier = Modifier
                    .offset(y = (-16).dp)
                    .size(58.dp)
                    .semantics { contentDescription = "Add expense"; role = Role.Button }
                    .clickable(onClick = onAdd),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 9.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = .55f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("+", color = MaterialTheme.colorScheme.onPrimary, fontSize = 32.sp)
                }
            }
            LedgerNavItem("◫", "Reports", notebookVisible && selectedSection == NotebookSection.Reports) {
                onSelectSection(NotebookSection.Reports)
            }
            LedgerNavItem("•••", "More", moreSelected, onMore)
        }
    }
}

@Composable
private fun LedgerNavItem(
    glyph: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .semantics {
                contentDescription = if (selected) "$label, selected" else label
                role = Role.Tab
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            glyph,
            fontSize = if (label == "More") 15.sp else 21.sp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
        Box(
            Modifier
                .padding(top = 2.dp)
                .width(16.dp)
                .height(2.dp)
                .background(if (selected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent, CircleShape)
        )
    }
}
