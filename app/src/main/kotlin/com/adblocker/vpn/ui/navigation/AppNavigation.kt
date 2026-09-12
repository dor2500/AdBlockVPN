package com.adblocker.vpn.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.border
import com.adblocker.vpn.R
import com.adblocker.vpn.ui.block.BlockedScreen
import com.adblocker.vpn.ui.bypass.AppBypassScreen
import com.adblocker.vpn.ui.dashboard.DashboardScreen
import com.adblocker.vpn.ui.excluded.ExcludedNetworksScreen
import com.adblocker.vpn.ui.firewall.AppFirewallScreen
import com.adblocker.vpn.ui.location.LocationScreen
import com.adblocker.vpn.ui.monitor.MonitorScreen
import com.adblocker.vpn.ui.settings.ChangelogScreen
import com.adblocker.vpn.ui.settings.SettingsScreen

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
    val titleResId: Int,
    val icon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Routes.DASHBOARD, R.string.nav_home, Icons.Filled.Home),
    BottomNavItem(Routes.LOCATION, R.string.nav_servers, Icons.Filled.Public),
    BottomNavItem(Routes.MONITOR, R.string.nav_traffic, Icons.Filled.Timeline),
    BottomNavItem(Routes.SETTINGS, R.string.nav_settings, Icons.Filled.Settings)
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val showBottomBar = bottomNavItems.any { it.route == currentDestination?.route }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
                ) {
                    NavigationBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                            ),
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        tonalElevation = 0.dp
                    ) {
                        bottomNavItems.forEach { item ->
                            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                            NavigationBarItem(
                                icon = { Icon(item.icon, contentDescription = stringResource(item.titleResId)) },
                                label = { Text(stringResource(item.titleResId), style = MaterialTheme.typography.labelSmall) },
                                selected = selected,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(bottom = if (showBottomBar) 90.dp else 0.dp)) {
            NavHost(
                navController = navController, 
                startDestination = Routes.DASHBOARD
            ) {
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
                composable(
                    route = "blocked_screen/{domain}/{reason}",
                    deepLinks = listOf(androidx.navigation.navDeepLink { uriPattern = "adblockvpn://blocked/{domain}/{reason}" })
                ) { backStackEntry ->
                    val domain = backStackEntry.arguments?.getString("domain") ?: ""
                    // Handle URL encoded reasons (e.g. spaces)
                    val encodedReason = backStackEntry.arguments?.getString("reason") ?: ""
                    val reason = java.net.URLDecoder.decode(encodedReason, "UTF-8")
                    com.adblocker.vpn.ui.block.BlockedScreen(
                        domain = domain,
                        reason = reason,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

