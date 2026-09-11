@file:OptIn(ExperimentalMaterial3Api::class)
package com.adblocker.vpn.ui.monitor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.adblocker.vpn.data.model.DnsLog
import com.adblocker.vpn.vpn.AdBlockVpnService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.border
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun MonitorScreen(viewModel: com.adblocker.vpn.ui.settings.SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val logs by AdBlockVpnService.dnsLogs.collectAsState(initial = null)
    // We reverse the logs so newest is on top
    val logsList = AdBlockVpnService.dnsLogs.replayCache.reversed()

    Scaffold(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "live_dot")
                        val dotAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 1f,
                            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                                animation = androidx.compose.animation.core.tween(1000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                            ),
                            label = "live_dot_alpha"
                        )
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error.copy(alpha = dotAlpha))
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "LIVE TRAFFIC", 
                            style = MaterialTheme.typography.titleMedium, 
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Black
                        ) 
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        if (logsList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No DNS queries yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(logsList) { log ->
                    LogItemCard(
                        log = log,
                        onWhitelist = { viewModel.addWhitelist(it) },
                        onBlacklist = { viewModel.addBlacklist(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LogItemCard(log: DnsLog, onWhitelist: (String) -> Unit, onBlacklist: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.onBackground.copy(alpha=0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val color = if (log.isBlocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                Text(time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    log.domain, 
                    style = MaterialTheme.typography.bodyMedium, 
                    fontFamily = FontFamily.Monospace, 
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = { onWhitelist(log.domain) }) {
                Icon(
                    androidx.compose.material.icons.Icons.Default.CheckCircle,
                    contentDescription = "Whitelist",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = { onBlacklist(log.domain) }) {
                Icon(
                    androidx.compose.material.icons.Icons.Default.Cancel,
                    contentDescription = "Blacklist",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
