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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
    
    // Liquid Glass Animated Background
    val infiniteTransition = rememberInfiniteTransition()
    val bgOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bg_flow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = if (isRunning) listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background
                    ) else listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background
                    ),
                    center = androidx.compose.ui.geometry.Offset(bgOffset, bgOffset),
                    radius = 1500f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 48.dp, bottom = 48.dp, start = 24.dp, end = 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isRunning) "SECURED" else "STANDBY",
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (isRunning) "V2 Quantum Core Active" else "Ready to Connect",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            // Massive Toggle Button with Liquid Radar Animation
            Box(
                modifier = Modifier.fillMaxWidth().height(350.dp),
                contentAlignment = Alignment.Center
            ) {
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = if (isRunning) 1.08f else 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "button_pulse"
                )

                val wave1Scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = if (isRunning) 2.2f else 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2500, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "wave1_scale"
                )
                val wave1Alpha by infiniteTransition.animateFloat(
                    initialValue = 0.6f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2500, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "wave1_alpha"
                )

                if (isRunning) {
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .scale(wave1Scale)
                            .clip(RoundedCornerShape(110.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = wave1Alpha))
                    )
                }

                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .scale(scale)
                        .clip(RoundedCornerShape(110.dp))
                        .background(
                            Brush.linearGradient(
                                colors = if (isRunning) listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                ) else listOf(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                )
                            )
                        )
                        .border(
                            width = 3.dp,
                            color = if (isRunning) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(110.dp)
                        )
                        .shadow(
                            elevation = if (isRunning) 60.dp else 20.dp,
                            shape = RoundedCornerShape(110.dp),
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
                        modifier = Modifier.size(80.dp),
                        tint = if (isRunning) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            // Server Location & Monitor Cards
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ActionCard(
                    title = "Location", 
                    subtitle = "Tap to change", 
                    icon = Icons.Filled.Public, 
                    onClick = onNavigateToLocation, 
                    modifier = Modifier.weight(1f)
                )
                ActionCard(
                    title = "Traffic logs", 
                    subtitle = "Live view", 
                    icon = Icons.Filled.Timeline, 
                    onClick = onNavigateToMonitor, 
                    modifier = Modifier.weight(1f)
                )
            }

            // Stats Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
                    .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val format = java.text.NumberFormat.getNumberInstance(java.util.Locale.US)
                StatCard("SCANNED", format.format(state.queriesTotal))
                StatCard("BLOCKED", format.format(state.queriesBlocked))
                StatCard("THREATS", format.format(state.queriesZeroDayBlocked))
            }
        }
    }
}

@Composable
private fun ActionCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
            .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Column {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(16.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
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
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
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
