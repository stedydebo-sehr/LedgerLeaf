package com.ledgerleaf.core.navigation
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ledgerleaf.feature.dashboard.DashboardScreen
@Composable
fun LedgerLeafNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = LedgerLeafDestination.Dashboard.route) {
        composable(LedgerLeafDestination.Dashboard.route) { DashboardScreen() }
    }
}
