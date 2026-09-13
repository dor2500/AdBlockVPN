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
import androidx.compose.ui.res.stringResource
import com.adblocker.vpn.R

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel()
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
    val primaryColor = MaterialTheme.colorScheme.primary // Glow
    val secondaryColor = MaterialTheme.colorScheme.secondary // Accent
    val bg = MaterialTheme.colorScheme.background

    // Background gradient animation when running
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "alpha"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    // A subtle gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isRunning) Brush.radialGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.15f * pulseAlpha), bg),
                    radius = 800f
                ) else Brush.verticalGradient(listOf(bg, bg))
            )
            .padding(24.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Header
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isRunning) stringResource(R.string.dash_connection_secured) else stringResource(R.string.dash_not_connected),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isRunning) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            
            // Radar Animation Box wrapping the Shield
            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isRunning) {
                    val radarRotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ), label = "radar"
                    )
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        androidx.compose.ui.graphics.drawscope.rotate(radarRotation) {
                            drawArc(
                                brush = Brush.sweepGradient(
                                    0f to Color.Transparent,
                                    0.8f to primaryColor.copy(alpha = 0.1f),
                                    1f to primaryColor.copy(alpha = 0.6f)
                                ),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = true
                            )
                        }
                    }
                }

                // Animated Glowing Shield Toggle
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(if (isRunning) pulseScale else 1f)
                        .background(Color.Transparent, shape = androidx.compose.foundation.shape.CircleShape)
                        .shadow(
                            elevation = if (isRunning) 32.dp * pulseAlpha else 0.dp,
                            shape = androidx.compose.foundation.shape.CircleShape,
                            spotColor = primaryColor,
                            ambientColor = primaryColor
                        )
                        .border(
                            width = if (isRunning) 4.dp else 1.dp,
                            color = if (isRunning) primaryColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        if (isRunning) stopVpnService(context) else requestStart()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = "Toggle Shield",
                    modifier = Modifier.size(72.dp),
                    tint = if (isRunning) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            }
            
            Spacer(modifier = Modifier.weight(1f))

            if (isRunning) {
                ThreatLevelGauge(state.queriesBlocked, primaryColor)
            }

            val latestLog by viewModel.dnsLogs.collectAsState(initial = null)
            val matrixLogs = AdBlockVpnService.dnsLogs.replayCache.reversed().take(4)

            // Live Matrix Feed
            if (isRunning && matrixLogs.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha=0.6f)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00FF00).copy(alpha=0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("LIVE MATRIX FEED", color = Color(0xFF00FF00), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        matrixLogs.forEach { log ->
                            val color = if (log.isBlocked) MaterialTheme.colorScheme.error else Color(0xFF00FF00).copy(alpha=0.7f)
                            val prefix = if (log.isBlocked) "[BLOCKED]" else "[ALLOWED]"
                            Text(
                                text = "$prefix ${log.domain}",
                                color = color,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Data Heist Shield UI
            if (isRunning) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "DATA HEIST PREVENTED",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        val format = java.text.NumberFormat.getNumberInstance(java.util.Locale.US)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem("Profiling", format.format(state.profilingBlocked))
                            StatItem("Location", format.format(state.locationBlocked))
                            StatItem("Zero-Day", format.format(state.queriesZeroDayBlocked))
                            StatItem("Total Ads", format.format(state.queriesBlocked))
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Connect VPN to see live protection stats", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (isRunning) {
                AdEaterPet(state.queriesBlocked)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val isThreats = label == stringResource(R.string.dash_threats) && value != "0"
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isThreats) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
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

@Composable
fun ThreatLevelGauge(blockedCount: Long, primaryColor: Color) {
    val level = when {
        blockedCount < 100 -> "LOW"
        blockedCount < 500 -> "MEDIUM"
        else -> "HIGH"
    }
    val levelColor = when {
        blockedCount < 100 -> Color(0xFF00FF00)
        blockedCount < 500 -> Color(0xFFFFA500)
        else -> Color.Red
    }
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, levelColor.copy(alpha=0.5f))
    ) {
        Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("NETWORK THREAT LEVEL", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text(level, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black), color = levelColor)
            Spacer(modifier = Modifier.height(4.dp))
            Text("$blockedCount threats intercepted", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.7f))
        }
    }
}

@Composable
fun AdEaterPet(blockedCount: Long) {
    val face = when {
        blockedCount == 0L -> "( - _ - ) Zzz"
        blockedCount < 50 -> "( ^ _ ^ ) Yummy!"
        blockedCount < 200 -> "\\( O _ O )/ MORE!"
        else -> "((( 👾 ))) RAMPAGE!"
    }
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha=0.2f))
    ) {
        Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("AD-EATER PET", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text(face, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text("LVL: ${blockedCount / 10}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.7f))
        }
    }
}
