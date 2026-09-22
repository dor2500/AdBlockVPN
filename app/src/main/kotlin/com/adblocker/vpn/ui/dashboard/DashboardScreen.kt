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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import android.view.HapticFeedbackConstants
import com.adblocker.vpn.util.Constants
import com.adblocker.vpn.vpn.AdBlockVpnService
import kotlin.random.Random

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(),
    onNavigateToAssistant: () -> Unit = {}
) {
    val context = LocalContext.current
    val state by viewModel.engineState.collectAsState()
    val view = LocalView.current
    val settingsViewModel: com.adblocker.vpn.ui.settings.SettingsViewModel = viewModel()
    val settingsState by settingsViewModel.settings.collectAsState()

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
    val primaryColor = MaterialTheme.colorScheme.primary
    val bg = MaterialTheme.colorScheme.background

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Floating Particles & Spatial Mesh Background
        SpatialBackground(isRunning = isRunning, primaryColor = primaryColor, bg = bg)

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            // Top Bar
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Vision Dashboard",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                val themes = listOf("glass", "aurora", "cyberpunk", "amethyst", "ocean", "sunset", "luxury", "monochrome", "forest")
                IconButton(onClick = {
                    val currentIndex = themes.indexOf(settingsState.selectedTheme.lowercase()).takeIf { it >= 0 } ?: 0
                    val nextIndex = (currentIndex + 1) % themes.size
                    settingsViewModel.setTheme(themes[nextIndex])
                }) {
                    Icon(Icons.Filled.Settings, "Theme", tint = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bento Box Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                // Hero Widget (Full Width)
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                    HeroShieldWidget(isRunning, view, { if(isRunning) stopVpnService(context) else requestStart() })
                }

                // Stats Widget 1
                item {
                    GlassWidget(title = "Threats Blocked") {
                        AnimatedCounter(value = state.queriesBlocked.toString(), isError = state.queriesBlocked > 0L)
                    }
                }

                // Stats Widget 2
                item {
                    GlassWidget(title = "Bandwidth Saved") {
                        AnimatedCounter(value = formatBytes(state.queriesBlocked * 120 * 1024), isError = false)
                    }
                }

                // Pet Widget (Full Width)
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                    AdEaterWidget(blockedCount = state.queriesBlocked)
                }
            }
        }

        // Cyber AI FAB
        ExtendedFloatingActionButton(
            onClick = onNavigateToAssistant,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 16.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 12.dp)
        ) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = "AI Assistant")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cyber AI")
        }
    }
}

@Composable
fun SpatialBackground(isRunning: Boolean, primaryColor: Color, bg: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg_anim")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Restart), label = "particles"
    )

    Canvas(modifier = Modifier.fillMaxSize().background(bg)) {
        if (isRunning) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.2f), bg),
                    center = Offset(size.width / 2, size.height / 3),
                    radius = size.width
                )
            )
            // Draw floating particles
            val random = Random(42) // Fixed seed for stable particles
            for (i in 0..50) {
                val startX = random.nextFloat() * size.width
                val startY = (random.nextFloat() * size.height - offsetY) % size.height
                val y = if (startY < 0) size.height + startY else startY
                val alpha = random.nextFloat() * 0.5f + 0.1f
                drawCircle(
                    color = primaryColor.copy(alpha = alpha),
                    radius = random.nextFloat() * 4f + 2f,
                    center = Offset(startX, y)
                )
            }
        }
    }
}

@Composable
fun HeroShieldWidget(isRunning: Boolean, view: android.view.View, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "press")

    val pulseScale by rememberInfiniteTransition(label="").animateFloat(
        initialValue = 0.98f, targetValue = 1.02f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse), label=""
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .scale(pressScale)
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(32.dp))
            .clickable(interactionSource = interactionSource, indication = null) {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        if (isRunning) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(pulseScale)
                    .background(
                        Brush.radialGradient(listOf(MaterialTheme.colorScheme.primary.copy(0.4f), Color.Transparent)),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.Shield, contentDescription = null,
                tint = if (isRunning) MaterialTheme.colorScheme.primary else Color.Gray,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isRunning) "SECURED" else "TAP TO CONNECT",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isRunning) MaterialTheme.colorScheme.primary else Color.Gray
            )
        }
    }
}

@Composable
fun GlassWidget(title: String, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            content()
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AdEaterWidget(blockedCount: Long) {
    val face = when {
        blockedCount == 0L -> "( - _ - ) Zzz"
        blockedCount < 50 -> "( ^ _ ^ ) Yummy!"
        blockedCount < 200 -> "\\( O _ O )/ MORE!"
        else -> "((( 👾 ))) RAMPAGE!"
    }
    
    val rawProgress = (blockedCount % 10) / 10f
    val animatedProgress by animateFloatAsState(rawProgress, tween(800), label = "")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("AD-EATER PET", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            Text(face, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("LVL ${blockedCount / 10}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(8.dp))
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.width(120.dp).height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
            }
        }
    }
}

@Composable
fun AnimatedCounter(value: String, isError: Boolean) {
    AnimatedContent(
        targetState = value,
        transitionSpec = {
            (slideInVertically { height -> height } + fadeIn()) togetherWith (slideOutVertically { height -> -height } + fadeOut())
        }, label = "stat_anim"
    ) { targetValue ->
        Text(
            text = targetValue,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun startVpnService(context: android.content.Context) {
    val intent = Intent(context, AdBlockVpnService::class.java).apply {
        action = Constants.ACTION_START
    }
    ContextCompat.startForegroundService(context, intent)
}

private fun stopVpnService(context: android.content.Context) {
    val intent = Intent(context, AdBlockVpnService::class.java).apply {
        action = Constants.ACTION_STOP
    }
    ContextCompat.startForegroundService(context, intent)
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format("%.1f KB", kb)
    val mb = kb / 1024.0
    if (mb < 1024) return String.format("%.1f MB", mb)
    val gb = mb / 1024.0
    return String.format("%.1f GB", gb)
}
