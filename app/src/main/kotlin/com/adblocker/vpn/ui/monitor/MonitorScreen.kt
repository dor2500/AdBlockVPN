@file:OptIn(ExperimentalMaterial3Api::class)
package com.adblocker.vpn.ui.monitor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

@Composable
fun MonitorScreen(viewModel: com.adblocker.vpn.ui.settings.SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val logs by AdBlockVpnService.dnsLogs.collectAsState(initial = null)
    // We reverse the logs so newest is on top
    val logsList = AdBlockVpnService.dnsLogs.replayCache.reversed()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live Traffic Monitor") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
        colors = CardDefaults.cardColors(
            containerColor = if (log.isBlocked) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val color = if (log.isBlocked) Color.Red else Color.Green
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                Text(time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(log.domain, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace)
            }
            IconButton(onClick = { onWhitelist(log.domain) }) {
                Icon(
                    androidx.compose.material.icons.Icons.Default.CheckCircle,
                    contentDescription = "Whitelist",
                    tint = Color.Green.copy(alpha = 0.7f)
                )
            }
            IconButton(onClick = { onBlacklist(log.domain) }) {
                Icon(
                    androidx.compose.material.icons.Icons.Default.Cancel,
                    contentDescription = "Blacklist",
                    tint = Color.Red.copy(alpha = 0.7f)
                )
            }
        }
    }
}
