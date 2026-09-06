package com.adblocker.vpn.ui.location

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import com.adblocker.vpn.ui.theme.NeonCyan
import com.adblocker.vpn.ui.theme.cyberBackground
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(viewModel: com.adblocker.vpn.ui.settings.SettingsViewModel = viewModel()) {
    val servers = VpnServerProvider.getServers
    val settings by viewModel.settings.collectAsState()
    val selectedId = settings.selectedVpnLocation

    Scaffold(
        modifier = Modifier.cyberBackground(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("GLOBAL SERVERS", style = MaterialTheme.typography.titleMedium, color = Color.White, letterSpacing = 2.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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
                    text = "Select a region to bypass geo-restrictions. (Note: Premium full-tunnel proxy requires Native Core integration in Phase 2).",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            items(servers) { server ->
                ServerItem(
                    server = server,
                    isSelected = server.id == selectedId,
                    onSelect = { viewModel.setVpnLocation(it.id) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun ServerItem(server: VpnServer, isSelected: Boolean, onSelect: (VpnServer) -> Unit) {
    val animatedBgColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isSelected) NeonCyan.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f),
        label = "bgColorAnim"
    )
    val animatedBorderColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isSelected) NeonCyan else Color.White.copy(alpha = 0.1f),
        label = "borderColorAnim"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .background(animatedBgColor)
            .border(1.dp, animatedBorderColor, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .clickable { onSelect(server) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = server.countryCode,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (isSelected) NeonCyan else Color.White,
                modifier = Modifier.width(40.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = server.name, color = Color.White, fontWeight = FontWeight.Medium)
            }
            androidx.compose.animation.AnimatedVisibility(
                visible = isSelected,
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut()
            ) {
                Icon(Icons.Default.Check, contentDescription = "Selected", tint = NeonCyan)
            }
        }
    }
}
