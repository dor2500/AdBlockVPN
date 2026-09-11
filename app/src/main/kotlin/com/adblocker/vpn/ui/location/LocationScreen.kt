package com.adblocker.vpn.ui.location

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
            TopAppBar(
                title = { Text("GLOBAL NETWORK", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground, letterSpacing = 2.sp, fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Text(
                    text = "Select a region to route your traffic. Low latency servers are recommended for best performance.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 24.dp, top = 8.dp)
                )
            }
            items(servers) { server ->
                ServerItem(
                    server = server,
                    isSelected = server.id == selectedId,
                    onSelect = { viewModel.setVpnLocation(it.id) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

private fun getFlagEmoji(countryCode: String): String {
    if (countryCode.length != 2) return "🌍"
    val firstLetter = Character.codePointAt(countryCode, 0) - 0x41 + 0x1F1E6
    val secondLetter = Character.codePointAt(countryCode, 1) - 0x41 + 0x1F1E6
    return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
}

@Composable
fun ServerItem(server: VpnServer, isSelected: Boolean, onSelect: (VpnServer) -> Unit) {
    val animatedBgColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        label = "bgColorAnim"
    )
    val animatedBorderColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
        label = "borderColorAnim"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
            .background(animatedBgColor)
            .border(2.dp, animatedBorderColor, androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
            .clickable { onSelect(server) }
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = getFlagEmoji(server.countryCode),
                fontSize = 32.sp,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = server.name, 
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, 
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.SignalCellularAlt, 
                        contentDescription = "Ping", 
                        tint = if (server.latencyMs < 50) Color(0xFF00FF87) else if (server.latencyMs < 120) Color(0xFFFFC107) else Color(0xFFFF5252),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${server.latencyMs} ms", 
                        color = MaterialTheme.colorScheme.onSurfaceVariant, 
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            androidx.compose.animation.AnimatedVisibility(
                visible = isSelected,
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

