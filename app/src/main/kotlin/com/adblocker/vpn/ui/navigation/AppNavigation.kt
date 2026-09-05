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

private object Routes {
    const val DASHBOARD = "dashboard"
    const val EXCLUDED_NETWORKS = "excluded_networks"
    const val SETTINGS = "settings"
    const val MONITOR = "monitor"
    const val LOCATION = "location"
    const val APP_BYPASS = "app_bypass"
    const val APP_FIREWALL = "app_firewall"
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

    Scaffold(
        modifier = Modifier.cyberBackground(),
        containerColor = Color.Transparent,
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            
            if (bottomNavItems.any { it.route == currentDestination?.route }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    NavigationBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(36.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), androidx.compose.foundation.shape.RoundedCornerShape(36.dp)),
                        containerColor = Color.Transparent,
                        contentColor = Color.LightGray,
                        tonalElevation = 0.dp
                    ) {
                        bottomNavItems.forEach { item ->
                            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                            NavigationBarItem(
                                icon = { Icon(item.icon, contentDescription = item.title) },
                                label = { Text(item.title) },
                                selected = selected,
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = NeonGreen,
                                    selectedTextColor = NeonGreen,
                                    indicatorColor = Color.Transparent, // Removes the ugly pill
                                    unselectedIconColor = Color.White.copy(alpha = 0.5f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.5f)
                                ),
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = Routes.DASHBOARD, modifier = Modifier.padding(innerPadding)) {
            composable(Routes.DASHBOARD) {
                DashboardScreen()
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
        }
    }
}
