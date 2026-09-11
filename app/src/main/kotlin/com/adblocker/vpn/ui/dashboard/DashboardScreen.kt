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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isRunning) {
                    androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            MaterialTheme.colorScheme.background
                        ),
                        radius = 1500f
                    )
                } else {
                    androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.background)
                }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            
            // Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isRunning) "PROTECTED" else "UNPROTECTED",
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (isRunning) "Your connection is secure" else "Tap to connect",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Massive Toggle Button with Radar Animation
            val infiniteTransition = rememberInfiniteTransition()
            
            // Core button scale
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = if (isRunning) 1.02f else 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "button_pulse"
            )

            // Radar waves
            val wave1Scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = if (isRunning) 1.6f else 1f,
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

            Box(contentAlignment = Alignment.Center) {
                // Radar Waves Background
                if (isRunning) {
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .scale(wave1Scale)
                            .clip(RoundedCornerShape(120.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = wave1Alpha))
                    )
                }

                // Main 3D Glass Button
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .scale(scale)
                        .clip(RoundedCornerShape(120.dp))
                        .background(
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = if (isRunning) listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                ) else listOf(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
                                )
                            )
                        )
                        .border(
                            width = 2.dp,
                            color = if (isRunning) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(120.dp)
                        )
                        .shadow(
                            elevation = if (isRunning) 32.dp else 16.dp,
                            shape = RoundedCornerShape(120.dp),
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

            // Timer
            if (isRunning) {
                var elapsedSeconds by remember { mutableLongStateOf(0L) }
                LaunchedEffect(isRunning) {
                    elapsedSeconds = 0L
                    while (true) {
                        kotlinx.coroutines.delay(1000L)
                        elapsedSeconds++
                    }
                }
                val hours = elapsedSeconds / 3600
                val minutes = (elapsedSeconds % 3600) / 60
                val seconds = elapsedSeconds % 60
                
                Text(
                    text = String.format("%02d:%02d:%02d", hours, minutes, seconds),
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Spacer(Modifier.height(30.dp))
            }

            // Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(2.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
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
