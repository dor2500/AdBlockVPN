package com.adblocker.vpn.ui.dashboard

import android.content.Intent
import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.IconButton
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
        
    var searchQuery by remember { mutableStateOf("") }
    val filteredLogs = remember(logs, searchQuery) {
        if (searchQuery.isBlank()) logs
        else logs.filter { it.domain.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        modifier = Modifier.cyberBackground(),
        containerColor = Color.Transparent,
        topBar = {
            val topBarIconTint by androidx.compose.animation.animateColorAsState(
                targetValue = if (state.isRunning) PremiumCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "topbar_icon_tint"
            )
            CenterAlignedTopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Shield,
                            contentDescription = null, // Decorative icon next to title
                            tint = topBarIconTint,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.app_name).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    IconButton(onClick = {
                        android.widget.Toast.makeText(context, "Settings coming soon", android.widget.Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.settings),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            SecuredNetworkParticles(isRunning = state.isRunning)
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
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
                            .background(brush = PrimaryGradient, alpha = 0.3f)
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
                                color = if (state.isRunning) GlassBorder else Red500.copy(alpha = 0.3f),
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
                                contentDescription = if (state.isRunning) stringResource(R.string.stop_vpn) else stringResource(R.string.start_vpn),
                                modifier = Modifier.size(48.dp).scale(currentScale),
                                tint = if (state.isRunning) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val dotAlpha by infiniteTransition.animateFloat(
                                    initialValue = 0.4f,
                                    targetValue = 1f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(800, easing = LinearEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "dotPulse"
                                )
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(if (state.isRunning) Color.White.copy(alpha = dotAlpha) else statusColor)
                                )
                                Spacer(Modifier.width(6.dp))
                                androidx.compose.animation.AnimatedContent(
                                    targetState = statusText,
                                    transitionSpec = {
                                        androidx.compose.animation.fadeIn(animationSpec = tween(300)) + androidx.compose.animation.slideInVertically(animationSpec = tween(300)) { height -> height } togetherWith
                                                androidx.compose.animation.fadeOut(animationSpec = tween(300)) + androidx.compose.animation.slideOutVertically(animationSpec = tween(300)) { height -> -height }
                                    },
                                    label = "statusTextAnim"
                                ) { text ->
                                    Text(
                                        text = text,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (state.isRunning) Color.White else statusColor,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
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
                val blockPercent = if (state.queriesTotal > 0) {
                    String.format("%.1f%%", state.queriesBlocked.toFloat() / state.queriesTotal * 100)
                } else "0%"
                StatItem(stringResource(R.string.block_rate), blockPercent)
            }

            Spacer(Modifier.height(12.dp))

            // Protection Level Badge
            val protectionLevel = when {
                !state.isRunning -> "INACTIVE"
                state.queriesBlocked > 100 -> "MAXIMUM"
                state.queriesBlocked > 0 -> "STANDARD"
                else -> "ACTIVE"
            }
            val protectionColor = when (protectionLevel) {
                "MAXIMUM" -> NeonGreen
                "STANDARD" -> PremiumCyan
                "ACTIVE" -> Color(0xFFFFB74D)
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(protectionColor.copy(alpha = 0.15f))
                        .border(0.5.dp, protectionColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val dotAlphaValue by infiniteTransition.animateFloat(
                            initialValue = 0.4f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "protectionDotPulse"
                        )
                        val finalAlpha = if (state.isRunning) dotAlphaValue else 1f
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(protectionColor.copy(alpha = finalAlpha))
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Protection: $protectionLevel",
                            style = MaterialTheme.typography.labelSmall,
                            color = protectionColor,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Connection Duration Timer
            if (state.isRunning) {
                var elapsedSeconds by remember { mutableLongStateOf(0L) }
                LaunchedEffect(state.isRunning) {
                    elapsedSeconds = 0L
                    while (true) {
                        kotlinx.coroutines.delay(1000L)
                        elapsedSeconds++
                    }
                }
                val hours = elapsedSeconds / 3600
                val minutes = (elapsedSeconds % 3600) / 60
                val seconds = elapsedSeconds % 60
                val timeText = String.format("%02d:%02d:%02d", hours, minutes, seconds)

                val timerBorderFraction by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(3000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "timerBorder"
                )
                val timerBorderColor = androidx.compose.ui.graphics.lerp(
                    PremiumCyan.copy(alpha = 0.4f),
                    NeonGreen.copy(alpha = 0.4f),
                    timerBorderFraction
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(GlassBackground)
                        .border(width = 0.5.dp, color = timerBorderColor, shape = RoundedCornerShape(12.dp))
                        .padding(vertical = 10.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⏱",
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.connection_duration),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.titleMedium,
                        color = PremiumCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
            
            if (state.isRunning) {
                NetworkGraph(rxSpeed = state.rxSpeed, txSpeed = state.txSpeed)
                Spacer(Modifier.height(24.dp))
            }

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
                        val threatPulse by infiniteTransition.animateFloat(
                            initialValue = 0.8f,
                            targetValue = 1.2f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "threatPulse"
                        )
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(brush = PrimaryGradient, alpha = 0.2f),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Security, contentDescription = null, tint = PremiumCyan, modifier = Modifier.size(14.dp).scale(threatPulse))
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(R.string.live_threat_feed), style = MaterialTheme.typography.labelSmall, color = Color.White, letterSpacing = 1.sp)
                        Spacer(Modifier.width(8.dp))
                        
                        // Count Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(GlassBorder)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${logs.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringResource(R.string.search_domains_placeholder), color = Color.Gray, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Search
                    ),
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.clear_search), tint = Color.Gray)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PremiumCyan,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(Modifier.height(12.dp))
                androidx.compose.animation.AnimatedContent(
                    targetState = filteredLogs.isEmpty(),
                    label = "ThreatFeedAnimation"
                ) { isEmpty ->
                    if (isEmpty) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val emptyInfinite = rememberInfiniteTransition(label = "emptyPulse")
                                val emptyAlpha by emptyInfinite.animateFloat(
                                    initialValue = 0.3f,
                                    targetValue = 0.8f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1500, easing = LinearEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "emptyAlpha"
                                )
                                Icon(
                                    Icons.Filled.Search,
                                    contentDescription = null,
                                    tint = Color.Gray.copy(alpha = emptyAlpha),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = if (searchQuery.isNotEmpty()) stringResource(R.string.no_domains_found) else stringResource(R.string.no_network_activity),
                                    color = Color.Gray.copy(alpha = emptyAlpha),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(
                                items = filteredLogs,
                                key = { it.timestamp.toString() + it.domain }
                            ) { log ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(GlassBackground)
                                        .border(0.5.dp, if (log.isBlocked) Color(0x33FF5252) else GlassBorder, RoundedCornerShape(12.dp))
                                        .clickable(
                                            onClickLabel = "Copy domain to clipboard"
                                        ) {
                                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            val clip = android.content.ClipData.newPlainText("domain", log.domain)
                                            clipboard.setPrimaryClip(clip)
                                            android.widget.Toast.makeText(context, "Copied: ${log.domain}", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(12.dp),
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
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (log.isBlocked) DangerGradient else SuccessGradient)
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (log.isBlocked) Icons.Filled.Close else Icons.Filled.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = if (log.isBlocked) stringResource(R.string.log_blocked).uppercase() else stringResource(R.string.log_allowed).uppercase(),
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            Text(
                text = "v${com.adblocker.vpn.BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                fontSize = 10.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        } // Close Column
        } // Close Box
    } // Close Scaffold body
} // Close DashboardScreen

@Composable
private fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.semantics(mergeDescendants = true) { 
            contentDescription = "$label: $value"
        }
    ) {
        androidx.compose.animation.AnimatedContent(
            targetState = value,
            transitionSpec = {
                androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) togetherWith
                        androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(300))
            },
            label = "stat_value"
        ) { targetValue ->
            val valueColor = if (targetValue != "0" && targetValue != "0%") PremiumCyan else MaterialTheme.colorScheme.onSurface
            Text(targetValue, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Light, color = valueColor)
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.8.sp, fontSize = 9.sp)
    }
}

@Composable
private fun NetworkGraph(rxSpeed: Long, txSpeed: Long) {
    val rxHistory = remember { mutableStateListOf<Float>() }
    val txHistory = remember { mutableStateListOf<Float>() }
    val maxPoints = 30

    LaunchedEffect(rxSpeed, txSpeed) {
        rxHistory.add(rxSpeed.toFloat() / 1024f) // kbps
        txHistory.add(txSpeed.toFloat() / 1024f) // kbps
        if (rxHistory.size > maxPoints) rxHistory.removeAt(0)
        if (txHistory.size > maxPoints) txHistory.removeAt(0)
    }

    val maxVal = maxOf(1f, rxHistory.maxOrNull() ?: 1f, txHistory.maxOrNull() ?: 1f)

    val isNetworkActive = rxSpeed > 0 || txSpeed > 0
    val borderColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isNetworkActive) PremiumCyan.copy(alpha = 0.5f) else GlassBorder,
        animationSpec = androidx.compose.animation.core.tween(500),
        label = "graph_border"
    )
    val borderWidth by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isNetworkActive) 1.dp else 0.5.dp,
        animationSpec = androidx.compose.animation.core.tween(500),
        label = "graph_border_width"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GlassBackground)
            .border(borderWidth, borderColor, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text("Network Activity (KB/s)", style = MaterialTheme.typography.labelSmall, color = Color.White, letterSpacing = 1.sp)
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("↓ ${String.format("%.1f", rxSpeed.toFloat() / 1024f)}", color = PremiumCyan, style = MaterialTheme.typography.labelSmall)
            Text("↑ ${String.format("%.1f", txSpeed.toFloat() / 1024f)}", color = PremiumPurple, style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.height(8.dp))
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxWidth().height(60.dp)) {
            val width = size.width
            val height = size.height
            val stepX = width / (maxPoints - 1)
            
            // Draw Rx (Download)
            if (rxHistory.isNotEmpty()) {
                val path = androidx.compose.ui.graphics.Path()
                val fillPath = androidx.compose.ui.graphics.Path()
                rxHistory.forEachIndexed { index, value ->
                    val x = index * stepX
                    val y = height - (value / maxVal * height)
                    if (index == 0) {
                        path.moveTo(x, y)
                        fillPath.moveTo(x, height)
                        fillPath.lineTo(x, y)
                    } else {
                        path.lineTo(x, y)
                        fillPath.lineTo(x, y)
                    }
                }
                fillPath.lineTo((rxHistory.size - 1) * stepX, height)
                fillPath.close()
                drawPath(fillPath, color = PremiumCyan.copy(alpha = 0.2f))
                drawPath(path, color = PremiumCyan, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))
            }

            // Draw Tx (Upload)
            if (txHistory.isNotEmpty()) {
                val path = androidx.compose.ui.graphics.Path()
                val fillPath = androidx.compose.ui.graphics.Path()
                txHistory.forEachIndexed { index, value ->
                    val x = index * stepX
                    val y = height - (value / maxVal * height)
                    if (index == 0) {
                        path.moveTo(x, y)
                        fillPath.moveTo(x, height)
                        fillPath.lineTo(x, y)
                    } else {
                        path.lineTo(x, y)
                        fillPath.lineTo(x, y)
                    }
                }
                fillPath.lineTo((txHistory.size - 1) * stepX, height)
                fillPath.close()
                drawPath(fillPath, color = PremiumPurple.copy(alpha = 0.2f))
                drawPath(path, color = PremiumPurple, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))
            }
        }
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
private fun SecuredNetworkParticles(isRunning: Boolean) {
    if (!isRunning) return
    
    val particleCount = 20
    val particles = remember { 
        List(particleCount) { 
            androidx.compose.ui.geometry.Offset(
                x = (Math.random() * 1000).toFloat(), 
                y = (Math.random() * 2000).toFloat()
            ) 
        }.toMutableStateList() 
    }
    
    val velocities = remember {
        List(particleCount) {
            androidx.compose.ui.geometry.Offset(
                x = (Math.random() * 2 - 1).toFloat() * 1.5f,
                y = (Math.random() * 2 - 1).toFloat() * 1.5f
            )
        }.toMutableStateList()
    }
    
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(16) // ~60fps
            for (i in particles.indices) {
                var newX = particles[i].x + velocities[i].x
                var newY = particles[i].y + velocities[i].y
                
                if (newX < 0 || newX > 1500) velocities[i] = velocities[i].copy(x = -velocities[i].x)
                if (newY < 0 || newY > 2500) velocities[i] = velocities[i].copy(y = -velocities[i].y)
                
                particles[i] = androidx.compose.ui.geometry.Offset(newX, newY)
            }
        }
    }
    
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            drawCircle(
                color = PremiumCyan.copy(alpha = 0.5f),
                radius = 4f,
                center = particle
            )
        }
        
        // Draw connecting lines if close
        for (i in particles.indices) {
            for (j in i + 1 until particles.size) {
                val p1 = particles[i]
                val p2 = particles[j]
                val distance = kotlin.math.sqrt(
                    (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y)
                )
                if (distance < 300f) {
                    val alpha = (1f - (distance / 300f)) * 0.3f
                    drawLine(
                        color = PremiumCyan.copy(alpha = alpha),
                        start = p1,
                        end = p2,
                        strokeWidth = 2f
                    )
                }
            }
        }
    }
}
