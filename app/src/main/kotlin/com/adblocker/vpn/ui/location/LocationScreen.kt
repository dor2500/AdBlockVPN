package com.adblocker.vpn.ui.location

import androidx.compose.ui.res.stringResource
import com.adblocker.vpn.R

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adblocker.vpn.data.model.VpnServer
import com.adblocker.vpn.data.model.VpnServerProvider
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(viewModel: com.adblocker.vpn.ui.settings.SettingsViewModel = viewModel()) {
    val servers = VpnServerProvider.getServers
    val settings by viewModel.settings.collectAsState()
    val selectedId = settings.selectedVpnLocation

    Scaffold(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.locations_title), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Text(
                    text = "Select a region to route your traffic. Low latency servers are recommended for best performance.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }
            items(servers) { server ->
                ServerItem(
                    server = server,
                    isSelected = server.id == selectedId,
                    onSelect = { viewModel.setVpnLocation(it.id) }
                )
            }
            item {

@Composable
fun ServerItem(server: VpnServer, isSelected: Boolean, onSelect: (VpnServer) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clickable { onSelect(server) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isSelected) 0.6f else 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flag Icon Placeholder (can be replaced with actual images if needed)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.background, shape = androidx.compose.foundation.shape.CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), shape = androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(server.name.take(2).uppercase(), color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = server.name, 
                    color = MaterialTheme.colorScheme.onBackground, 
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.SignalCellularAlt, 
                        contentDescription = "Ping", 
                        tint = if (server.latencyMs < 50) MaterialTheme.colorScheme.secondary else if (server.latencyMs < 120) Color(0xFFFFCC00) else Color(0xFFFF3B30),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${server.latencyMs} ms", 
                        color = MaterialTheme.colorScheme.onSurfaceVariant, 
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), shape = androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
