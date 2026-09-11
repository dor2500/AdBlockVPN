package com.adblocker.vpn.ui.dashboard

import android.content.Intent
import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adblocker.vpn.util.Constants
import com.adblocker.vpn.vpn.AdBlockVpnService
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToLocation: () -> Unit = {},
    onNavigateToMonitor: () -> Unit = {}
) {
    val context = LocalContext.current
    val state by viewModel.engineState.collectAsState()
    val view = LocalView.current

    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            startVpnService(context)
        }
    }

    fun requestStart() {
        val intent = VpnService.prepare(context)
        if (intent != null) {
            vpnPermissionLauncher.launch(intent)
        } else {
            startVpnService(context)
        }
    }

    val isRunning = state.isRunning

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = if (isRunning) listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background
                    ) else listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 48.dp, bottom = 100.dp, start = 24.dp, end = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(), 
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isRunning) "PROTECTED" else "UNPROTECTED",
                            style = MaterialTheme.typography.headlineMedium,
                            color = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (isRunning) "Traffic is secured via V2 Core" else "V2 Core Standby",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            // Massive Toggle Button with Radar Animation
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val infiniteTransition = rememberInfiniteTransition()
                    
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = if (isRunning) 1.05f else 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "button_pulse"
                    )

                    val wave1Scale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = if (isRunning) 1.8f else 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "wave1_scale"
                    )
                    val wave1Alpha by infiniteTransition.animateFloat(
                        initialValue = 0.5f,
                        targetValue = 0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "wave1_alpha"
                    )

                    if (isRunning) {
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .scale(wave1Scale)
                                .clip(RoundedCornerShape(100.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = wave1Alpha))
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .scale(scale)
                            .clip(RoundedCornerShape(100.dp))
                            .background(
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = if (isRunning) listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    ) else listOf(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                                    )
                                )
                            )
                            .border(
                                width = 2.dp,
                                color = if (isRunning) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(100.dp)
                            )
                            .shadow(
                                elevation = if (isRunning) 40.dp else 16.dp,
                                shape = RoundedCornerShape(100.dp),
                                ambientColor = if (isRunning) MaterialTheme.colorScheme.secondary else Color.Black,
                                spotColor = if (isRunning) MaterialTheme.colorScheme.primary else Color.Black
                            )
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                if (isRunning) stopVpnService(context) else requestStart()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PowerSettingsNew,
                            contentDescription = "Toggle VPN",
                            modifier = Modifier.size(60.dp),
                            tint = if (isRunning) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            
            // Server Location & Monitor Cards
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ActionCard(
                        title = "Location", 
                        subtitle = "Select Server", 
                        icon = Icons.Filled.Public, 
                        onClick = onNavigateToLocation, 
                        modifier = Modifier.weight(1f)
                    )
                    ActionCard(
                        title = "Traffic", 
                        subtitle = "View Logs", 
                        icon = Icons.Filled.Timeline, 
                        onClick = onNavigateToMonitor, 
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Stats Card
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                        .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val format = java.text.NumberFormat.getNumberInstance(java.util.Locale.US)
                    StatCard("Scanned", format.format(state.queriesTotal))
                    StatCard("Blocked", format.format(state.queriesBlocked))
                }
            }
        }
    }
}

@Composable
private fun ActionCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
            .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StatCard(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun startVpnService(context: android.content.Context) {
    val intent = Intent(context, AdBlockVpnService::class.java).setAction(Constants.ACTION_START)
    ContextCompat.startForegroundService(context, intent)
}

private fun stopVpnService(context: android.content.Context) {
    val intent = Intent(context, AdBlockVpnService::class.java).setAction(Constants.ACTION_STOP)
    context.startService(intent)
}
