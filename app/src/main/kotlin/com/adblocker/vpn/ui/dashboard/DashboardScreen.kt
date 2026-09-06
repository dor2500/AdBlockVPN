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
import androidx.compose.ui.res.stringResource
import com.adblocker.vpn.R
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Security
import androidx.compose.ui.text.style.TextOverflow
import com.adblocker.vpn.data.model.DnsLog
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.take
import com.adblocker.vpn.util.Constants
import com.adblocker.vpn.util.Updater
import com.adblocker.vpn.util.UpdateInfo
import com.adblocker.vpn.vpn.AdBlockVpnService
import com.adblocker.vpn.ui.theme.*
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.draw.scale

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

    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val info = Updater.checkForUpdate()
        if (info != null && info.isUpdateAvailable) {
            updateInfo = info
            showUpdateDialog = true
        }
    }

    if (showUpdateDialog && updateInfo != null) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = { Text(stringResource(R.string.update_available)) },
            text = { Text(stringResource(R.string.update_desc, updateInfo!!.latestVersion, updateInfo!!.releaseNotes)) },
            confirmButton = {
                Button(onClick = {
                    showUpdateDialog = false
                    Updater.downloadAndInstallUpdate(context, updateInfo!!.downloadUrl, updateInfo!!.latestVersion)
                }) {
                    Text(stringResource(R.string.update_now))
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateDialog = false }) {
                    Text(stringResource(R.string.later))
                }
            }
        )
    }

    fun requestStart() {
        val intent = VpnService.prepare(context)
        if (intent != null) {
            vpnPermissionLauncher.launch(intent)
        } else {
            startVpnService(context)
        }
    }

    // Collect latest 15 logs
    val logs by viewModel.dnsLogs
        .scan(emptyList<DnsLog>()) { acc, log -> 
            (listOf(log) + acc).take(15) 
        }
        .collectAsState(initial = emptyList())

    Scaffold(
        modifier = Modifier.cyberBackground(),
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.app_name).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Removed old title since we now have TopAppBar
            Spacer(Modifier.height(16.dp))

            // Connection Status Text
            val (statusText, statusColor) = when {
                !state.isRunning -> stringResource(R.string.disconnected) to MaterialTheme.colorScheme.onSurfaceVariant
                state.isPassThrough -> stringResource(R.string.protection_paused) to MaterialTheme.colorScheme.secondary
                else -> stringResource(R.string.secured) to MaterialTheme.colorScheme.primary
            }

            // Dynamic Glass Pulsing Connect Button
            val view = LocalView.current
            
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseScale"
            )
            val currentScale = if (state.isRunning) pulseScale else 1f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer glow pulse
                if (state.isRunning) {
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .scale(currentScale)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(PrimaryGradient.copy(alpha = 0.3f))
                    )
                }

                // Main circular glass button
                Card(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            if (state.isRunning) stopVpnService(context) else requestStart()
                        },
                    shape = androidx.compose.foundation.shape.CircleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent,
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (state.isRunning) 16.dp else 0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = if (state.isRunning) PrimaryGradient else Brush.linearGradient(listOf(GlassBackground, GlassBackground)),
                                alpha = 1f
                            )
                            .border(
                                width = 1.dp,
                                color = GlassBorder,
                                shape = androidx.compose.foundation.shape.CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                if (state.isRunning) Icons.Filled.Shield else Icons.Filled.PowerSettingsNew,
                                contentDescription = "Power",
                                modifier = Modifier.size(48.dp),
                                tint = if (state.isRunning) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (state.isRunning) Color.White else statusColor,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))

            // Glassmorphism Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassBackground)
                    .border(0.5.dp, GlassBorder, RoundedCornerShape(16.dp))
                    .padding(vertical = 16.dp, horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val format = java.text.NumberFormat.getNumberInstance(java.util.Locale.US)
                StatItem(stringResource(R.string.queries), format.format(state.queriesTotal))
                StatItem(stringResource(R.string.blocked), format.format(state.queriesBlocked))
                StatItem(stringResource(R.string.zero_day), format.format(state.queriesZeroDayBlocked))
            }

            Spacer(Modifier.height(24.dp))

            // Glassmorphic Live Threat Feed
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassBackground)
                    .border(0.5.dp, GlassBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(PrimaryGradient.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Security, contentDescription = null, tint = PremiumCyan, modifier = Modifier.size(14.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(R.string.live_threat_feed), style = MaterialTheme.typography.labelSmall, color = Color.White, letterSpacing = 1.sp)
                }
                Spacer(Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(logs) { log ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = log.domain,
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (log.isBlocked) DangerGradient else SuccessGradient)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (log.isBlocked) stringResource(R.string.log_blocked).uppercase() else stringResource(R.string.log_allowed).uppercase(),
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Light, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
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
