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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adblocker.vpn.util.Constants
import com.adblocker.vpn.vpn.AdBlockVpnService
import com.adblocker.vpn.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.engineState.collectAsState()

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

    Scaffold(
        topBar = {
            // Minimalist Top Bar, no huge backgrounds
            CenterAlignedTopAppBar(
                title = { Text("AdBlock VPN", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(top = 40.dp, bottom = 24.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // Connection Status Text
            val (statusText, statusColor) = when {
                !state.isRunning -> "DISCONNECTED" to Color.Gray
                state.isPassThrough -> "PAUSED" to Color(0xFFFFB300)
                else -> "PROTECTED" to NeonGreen
            }
            
            Text(
                text = statusText,
                style = MaterialTheme.typography.titleMedium,
                color = statusColor,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(60.dp))

            // Premium Connect Button
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.0f,
                targetValue = if (state.isRunning && !state.isPassThrough) 0.6f else 0.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ), label = "alpha"
            )

            val primaryColor = if (state.isRunning) (if (state.isPassThrough) Color(0xFFFFB300) else NeonCyan) else Color.DarkGray
            val secondaryColor = if (state.isRunning) (if (state.isPassThrough) Color(0xFFFF8F00) else NeonGreen) else Color.Gray
            val gradient = Brush.linearGradient(listOf(primaryColor, secondaryColor))
            
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(primaryColor.copy(alpha = pulseAlpha), Color.Transparent)))
                    .clickable {
                        if (state.isRunning) stopVpnService(context) else requestStart()
                    },
                contentAlignment = Alignment.Center
            ) {
                // Inner button
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .shadow(if (state.isRunning) 25.dp else 5.dp, CircleShape, spotColor = primaryColor)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(3.dp, gradient, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.PowerSettingsNew,
                        contentDescription = "Power",
                        modifier = Modifier.size(68.dp),
                        tint = if (state.isRunning) Color.White else Color.Gray
                    )
                }
            }

            Spacer(Modifier.height(80.dp))

            // Minimalist Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val format = java.text.NumberFormat.getNumberInstance(java.util.Locale.US)
                StatItem("QUERIES", format.format(state.queriesTotal))
                StatItem("BLOCKED", format.format(state.queriesBlocked))
                StatItem("ZERO-DAY", format.format(state.queriesZeroDayBlocked))
                val pct = if (state.queriesTotal > 0) (state.queriesBlocked * 100 / state.queriesTotal) else 0
                StatItem("RATIO", "$pct%")
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Light, color = Color.White)
        Spacer(Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, letterSpacing = 1.sp)
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
