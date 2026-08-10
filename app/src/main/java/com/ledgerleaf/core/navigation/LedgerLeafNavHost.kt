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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
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
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ledgerleaf.feature.archive.ArchiveScreen
import com.ledgerleaf.feature.backup.BackupScreen
import com.ledgerleaf.feature.budget.BudgetsScreen
import com.ledgerleaf.feature.dashboard.DashboardScreen
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

private val primaryDestinations = listOf(
    LedgerLeafDestination.Dashboard,
    LedgerLeafDestination.History,
    LedgerLeafDestination.Reports,
    LedgerLeafDestination.Settings
)

@Composable
fun LedgerLeafNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            LedgerBottomNavigation(
                currentRouteSelected = { route ->
                    currentDestination?.hierarchy?.any { it.route == route } == true
                },
                onNavigate = { destination ->
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onAdd = { navController.navigate(LedgerLeafDestination.AddExpense.route) }
            )
        }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            NavHost(navController, startDestination = LedgerLeafDestination.Dashboard.route) {
                composable(LedgerLeafDestination.Dashboard.route) {
                    DashboardScreen(
                        onReportsClick = { navController.navigate(LedgerLeafDestination.Reports.route) },
                        onSettingsClick = { navController.navigate(LedgerLeafDestination.Settings.route) },
                        onSearchClick = { navController.navigate(LedgerLeafDestination.Search.route) },
                        onBudgetsClick = { navController.navigate(LedgerLeafDestination.Budgets.route) },
                        onExpenseClick = { expenseId -> navController.navigate(LedgerLeafDestination.ExpenseDetails.createRoute(expenseId)) },
                        onHistoryClick = { navController.navigate(LedgerLeafDestination.History.route) },
                        onFavoritesClick = { navController.navigate(LedgerLeafDestination.Favorites.route) },
                        onRecurringClick = { navController.navigate(LedgerLeafDestination.Recurring.route) }
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
                        onDeleted = { navController.popBackStack(LedgerLeafDestination.History.route, inclusive = false) },
                        onArchived = { navController.popBackStack(LedgerLeafDestination.History.route, inclusive = false) }
                    )
                }
                composable(LedgerLeafDestination.EditExpense.route) {
                    EditExpenseScreen(onSaved = { navController.popBackStack() })
                }
                composable(LedgerLeafDestination.Reports.route) { ReportsScreen() }
                composable(LedgerLeafDestination.Settings.route) {
                    SettingsScreen(
                        { navController.navigate(LedgerLeafDestination.Search.route) },
                        { navController.navigate(LedgerLeafDestination.Archive.route) },
                        { navController.navigate(LedgerLeafDestination.RecycleBin.route) },
                        { navController.navigate(LedgerLeafDestination.Budgets.route) },
                        { navController.navigate(LedgerLeafDestination.Favorites.route) },
                        { navController.navigate(LedgerLeafDestination.Recurring.route) },
                        { navController.navigate(LedgerLeafDestination.MonthlyClosing.route) },
                        { navController.navigate(LedgerLeafDestination.BackupRestore.route) }
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
    currentRouteSelected: (String) -> Boolean,
    onNavigate: (LedgerLeafDestination) -> Unit,
    onAdd: () -> Unit
) {
    Surface(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 0.dp),
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
            LedgerNavItem(primaryDestinations[0], "⌂", "Home", currentRouteSelected(primaryDestinations[0].route)) { onNavigate(primaryDestinations[0]) }
            LedgerNavItem(primaryDestinations[1], "▣", "History", currentRouteSelected(primaryDestinations[1].route)) { onNavigate(primaryDestinations[1]) }
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
                    Text("+", color = MaterialTheme.colorScheme.onPrimary, fontSize = 32.sp, fontWeight = FontWeight.Normal)
                }
            }
            LedgerNavItem(primaryDestinations[2], "◫", "Reports", currentRouteSelected(primaryDestinations[2].route)) { onNavigate(primaryDestinations[2]) }
            LedgerNavItem(primaryDestinations[3], "•••", "More", currentRouteSelected(primaryDestinations[3].route)) { onNavigate(primaryDestinations[3]) }
        }
    }
}

@Composable
private fun LedgerNavItem(
    destination: LedgerLeafDestination,
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
