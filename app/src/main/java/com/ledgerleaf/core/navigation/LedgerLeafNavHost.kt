package com.ledgerleaf.core.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ledgerleaf.feature.archive.ArchiveScreen
import com.ledgerleaf.feature.budget.BudgetsScreen
import com.ledgerleaf.feature.dashboard.DashboardScreen
import com.ledgerleaf.feature.history.HistoryScreen
import com.ledgerleaf.feature.monthlyclosing.MonthlyClosingScreen
import com.ledgerleaf.feature.favorites.FavoritesScreen
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
            Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 3.dp) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    primaryDestinations.take(2).forEach { destination ->
                        LedgerLeafNavigationButton(
                            destination,
                            currentDestination?.hierarchy?.any { it.route == destination.route } == true
                        ) {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }

                    FloatingActionButton(
                        onClick = { navController.navigate(LedgerLeafDestination.AddExpense.route) },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text("+", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }

                    primaryDestinations.drop(2).forEach { destination ->
                        LedgerLeafNavigationButton(
                            destination,
                            currentDestination?.hierarchy?.any { it.route == destination.route } == true
                        ) {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            NavHost(navController, startDestination = LedgerLeafDestination.Dashboard.route) {
                composable(LedgerLeafDestination.Dashboard.route) {
                    DashboardScreen(
                        onReportsClick = { navController.navigate(LedgerLeafDestination.Reports.route) },
                        onSettingsClick = { navController.navigate(LedgerLeafDestination.Settings.route) },
                        onExpenseClick = { expenseId ->
                            navController.navigate(LedgerLeafDestination.ExpenseDetails.createRoute(expenseId))
                        },
                        onHistoryClick = { navController.navigate(LedgerLeafDestination.History.route) },
                        onFavoritesClick = { navController.navigate(LedgerLeafDestination.Favorites.route) },
                        onRecurringClick = { navController.navigate(LedgerLeafDestination.Recurring.route) }
                    )
                }
                composable(LedgerLeafDestination.History.route) {
                    HistoryScreen(
                        onExpenseClick = {
                            navController.navigate(LedgerLeafDestination.ExpenseDetails.createRoute(it))
                        }
                    )
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
                        onEdit = {
                            navController.navigate(LedgerLeafDestination.EditExpense.createRoute(it))
                        },
                        onDeleted = {
                            navController.popBackStack(LedgerLeafDestination.History.route, inclusive = false)
                        }
                    )
                }
                composable(LedgerLeafDestination.EditExpense.route) {
                    EditExpenseScreen(
                        onSaved = {
                            navController.popBackStack()
                        }
                    )
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
                        { navController.navigate(LedgerLeafDestination.MonthlyClosing.route) }
                    )
                }
                composable(LedgerLeafDestination.Search.route) {
                    SearchScreen(
                        onExpenseClick = { expenseId ->
                            navController.navigate(LedgerLeafDestination.ExpenseDetails.createRoute(expenseId))
                        }
                    )
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
                    MonthlyClosingScreen(
                        onExpenseClick = { navController.navigate(LedgerLeafDestination.ExpenseDetails.createRoute(it)) }
                    )
                }
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
private fun LedgerLeafNavigationButton(
    destination: LedgerLeafDestination,
    selected: Boolean,
    onClick: () -> Unit
) {
    TextButton(onClick = onClick) {
        Text(
            destination.label,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
