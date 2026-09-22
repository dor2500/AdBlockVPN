package com.adblocker.vpn.ui.dashboard

import android.content.Intent
import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.AutoAwesome
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
import androidx.compose.ui.graphics.drawscope.rotate
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
    viewModel: DashboardViewModel = viewModel(),
    onNavigateToAssistant: () -> Unit = {}
) {
    val context = LocalContext.current
    val state by viewModel.engineState.collectAsState()
    val view = LocalView.current
    val settingsViewModel: com.adblocker.vpn.ui.settings.SettingsViewModel = viewModel()

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
    val infiniteBgTransition = rememberInfiniteTransition(label = "bg_anim")
    val bgOffset1 by infiniteBgTransition.animateFloat(
        initialValue = 0f, targetValue = 2000f,
        animationSpec = infiniteRepeatable(tween(15000, easing = LinearEasing), RepeatMode.Reverse),
        label = "bg_offset1"
    )
    val bgOffset2 by infiniteBgTransition.animateFloat(
        initialValue = 2000f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Reverse),
        label = "bg_offset2"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isRunning) {
                    val c1 = primaryColor.copy(alpha = 0.15f * pulseAlpha)
                    val c2 = secondaryColor.copy(alpha = 0.15f * pulseAlpha)
                    Brush.linearGradient(
                        colors = listOf(c1, bg, c2, bg),
                        start = androidx.compose.ui.geometry.Offset(bgOffset1, bgOffset2),
                        end = androidx.compose.ui.geometry.Offset(bgOffset2, bgOffset1)
                    )
                } else {
                    Brush.verticalGradient(listOf(bg, bg))
                }
            )
            .padding(24.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        val settingsState by settingsViewModel.settings.collectAsState()
        val themes = listOf("glass", "aurora", "cyberpunk", "amethyst", "ocean", "sunset", "luxury", "monochrome", "forest")
        IconButton(
            onClick = {
                val currentIndex = themes.indexOf(settingsState.selectedTheme.lowercase()).takeIf { it >= 0 } ?: 0
                val nextIndex = (currentIndex + 1) % themes.size
                settingsViewModel.setTheme(themes[nextIndex])
            },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(Icons.Filled.Settings, contentDescription = "Change Theme", tint = MaterialTheme.colorScheme.onBackground.copy(alpha=0.5f))
        }

        ExtendedFloatingActionButton(
            onClick = onNavigateToAssistant,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 16.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
        ) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = "AI Assistant")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cyber AI")
        }

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
                val shimmerOffset by infiniteBgTransition.animateFloat(
                    initialValue = -500f,
                    targetValue = 2000f,
                    animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
                    label = "shimmer"
                )
                val shimmerBrush = Brush.linearGradient(
                    colors = listOf(primaryColor.copy(alpha=0.8f), Color.White, primaryColor.copy(alpha=0.8f)),
                    start = androidx.compose.ui.geometry.Offset(shimmerOffset, 0f),
                    end = androidx.compose.ui.geometry.Offset(shimmerOffset + 400f, 0f)
                )

                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = primaryColor.copy(alpha = if (isRunning) pulseAlpha else 0.2f),
                            blurRadius = if (isRunning) 16f else 4f
                        )
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (isRunning) {
                    Text(
                        text = stringResource(R.string.dash_connection_secured),
                        style = MaterialTheme.typography.bodyLarge.copy(brush = shimmerBrush)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.dash_not_connected),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.5f + (pulseAlpha * 0.5f))
                    )
                }
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
                    val reverseRadarRotation by infiniteTransition.animateFloat(
                        initialValue = 360f,
                        targetValue = 0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(3000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ), label = "radar_reverse"
                    )
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        // Outer fast sweep
                        rotate(radarRotation) {
                            drawArc(
                                brush = Brush.sweepGradient(
                                    0f to Color.Transparent,
                                    0.8f to Color.White.copy(alpha = 0.05f),
                                    1f to Color.White.copy(alpha = 0.4f)
                                ),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = true,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                            )
                        }
                        // Inner slow reverse sweep
                        rotate(reverseRadarRotation) {
                            val inset = size.width * 0.15f
                            drawArc(
                                brush = Brush.sweepGradient(
                                    0f to Color.Transparent,
                                    0.8f to Color.White.copy(alpha = 0.05f),
                                    1f to Color.White.copy(alpha = 0.5f)
                                ),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = true,
                                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                                size = androidx.compose.ui.geometry.Size(size.width - inset*2, size.height - inset*2)
                            )
                        }
                    }
                }

                // Animated Glowing Shield Toggle
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val pressScale by animateFloatAsState(targetValue = if (isPressed) 0.85f else 1f, label="press")

                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale((if (isRunning) pulseScale else 1f) * pressScale)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.02f)),
                                radius = 250f
                            ),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                        .shadow(
                            elevation = if (isRunning) 32.dp * pulseAlpha else 0.dp,
                            shape = androidx.compose.foundation.shape.CircleShape,
                            spotColor = primaryColor,
                            ambientColor = primaryColor
                        )
                        .border(
                            width = 1.dp,
                            color = if (isRunning) primaryColor.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
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
                // Bento Grid
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        ThreatLevelGauge(state.queriesBlocked, primaryColor)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        AdEaterPet(state.queriesBlocked)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                val matrixLogs = AdBlockVpnService.dnsLogs.replayCache.reversed().take(4)
                
                // Live Matrix Feed (Bento Style)
                if (matrixLogs.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("LIVE MATRIX FEED", color = Color(0xFF00FF00), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(Color(0xFF00FF00).copy(alpha = pulseAlpha))
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            matrixLogs.forEach { log ->
                                val color = if (log.isBlocked) MaterialTheme.colorScheme.error else Color(0xFF00FF00).copy(alpha=0.7f)
                                val prefix = if (log.isBlocked) "[BLOCKED]" else "[ALLOWED]"
                                Text(
                                    text = "$prefix ${log.domain}",
                                    color = color,
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                                    maxLines = 1,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
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
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f))
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
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val isThreats = label == stringResource(R.string.dash_threats) && value != "0"
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                if (targetState.replace(",", "").toLongOrNull() ?: 0L > initialState.replace(",", "").toLongOrNull() ?: 0L) {
                    (slideInVertically { height -> height } + fadeIn()) togetherWith (slideOutVertically { height -> -height } + fadeOut())
                } else {
                    (slideInVertically { height -> -height } + fadeIn()) togetherWith (slideOutVertically { height -> height } + fadeOut())
                }
            }, label = "stat_anim"
        ) { targetValue ->
            Text(
                text = targetValue,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isThreats) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        }
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
    val badge = when {
        blockedCount < 50 -> "🥉 NOVICE"
        blockedCount < 200 -> "🥈 GUARDIAN"
        blockedCount < 1000 -> "🥇 CYBER NINJA"
        else -> "💎 MATRIX LORD"
    }

    Card(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("THREATS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text(level, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = levelColor)
            Spacer(modifier = Modifier.height(4.dp))
            Text("$blockedCount BLOCKED", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.7f))
            Spacer(modifier = Modifier.height(8.dp))
            Text(badge, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = levelColor.copy(alpha=0.9f))
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
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha=0.15f))
    ) {
        Column(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("AD-EATER", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text(face, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text("LVL: ${blockedCount / 10}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.7f))
            Spacer(modifier = Modifier.height(8.dp))
            val rawProgress = (blockedCount % 10) / 10f
            val animatedProgress by animateFloatAsState(
                targetValue = rawProgress,
                animationSpec = tween(800, easing = androidx.compose.animation.core.LinearOutSlowInEasing),
                label = "xp_bar"
            )
            androidx.compose.material3.LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth(0.7f).height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
        }
    }
}
