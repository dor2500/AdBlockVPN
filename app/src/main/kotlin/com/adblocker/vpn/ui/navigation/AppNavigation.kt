package com.adblocker.vpn.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.adblocker.vpn.ui.dashboard.DashboardScreen
import com.adblocker.vpn.ui.excluded.ExcludedNetworksScreen
import com.adblocker.vpn.ui.monitor.MonitorScreen
import com.adblocker.vpn.ui.settings.SettingsScreen
import com.adblocker.vpn.ui.theme.NeonGreen
import com.adblocker.vpn.ui.theme.cyberBackground
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale

private object Routes {
    const val DASHBOARD = "dashboard"
    const val EXCLUDED_NETWORKS = "excluded_networks"
    const val SETTINGS = "settings"
    const val MONITOR = "monitor"
    const val LOCATION = "location"
    const val APP_BYPASS = "app_bypass"
    const val APP_FIREWALL = "app_firewall"
    const val CHANGELOG = "changelog"
}

private data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Routes.DASHBOARD, "Home", Icons.Filled.Home),
    BottomNavItem(Routes.LOCATION, "Locations", androidx.compose.material.icons.Icons.Filled.Public),
    BottomNavItem(Routes.MONITOR, "Traffic", Icons.Filled.Timeline),
    BottomNavItem(Routes.SETTINGS, "Settings", Icons.Filled.Settings)
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController, 
        startDestination = Routes.DASHBOARD,
        modifier = Modifier
            .fillMaxSize()
            .cyberBackground()
    ) {
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToLocation = { navController.navigate(Routes.LOCATION) },
                onNavigateToMonitor = { navController.navigate(Routes.MONITOR) }
            )
        }
        composable(Routes.LOCATION) {
            com.adblocker.vpn.ui.location.LocationScreen()
        }
        composable(Routes.MONITOR) {
            MonitorScreen()
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateToExcluded = { navController.navigate(Routes.EXCLUDED_NETWORKS) },
                onNavigateToAppBypass = { navController.navigate(Routes.APP_BYPASS) },
                onNavigateToAppFirewall = { navController.navigate(Routes.APP_FIREWALL) },
                onNavigateToChangelog = { navController.navigate(Routes.CHANGELOG) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.EXCLUDED_NETWORKS) {
            ExcludedNetworksScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.APP_BYPASS) {
            com.adblocker.vpn.ui.bypass.AppBypassScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.APP_FIREWALL) {
            com.adblocker.vpn.ui.firewall.AppFirewallScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.CHANGELOG) {
            com.adblocker.vpn.ui.settings.ChangelogScreen(onBack = { navController.popBackStack() })
        }
    }
}
