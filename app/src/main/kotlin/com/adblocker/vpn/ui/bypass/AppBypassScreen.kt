package com.adblocker.vpn.ui.bypass

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adblocker.vpn.R
import com.adblocker.vpn.ui.theme.NeonGreen
import com.adblocker.vpn.ui.theme.cyberBackground
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBypassScreen(
    onBack: () -> Unit,
    viewModel: AppBypassViewModel = viewModel()
) {
    val apps by viewModel.installedApps.collectAsState()
    val bypassedApps by viewModel.bypassedApps.collectAsState()

    Scaffold(
        modifier = Modifier.cyberBackground(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("APP BYPASS", style = MaterialTheme.typography.titleMedium, color = Color.White, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
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
                    text = "Apps turned ON will bypass the AdBlocker VPN completely and use normal unmonitored internet.",
                    color = Color.LightGray,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            items(apps) { app ->
                val isBypassed = bypassedApps.contains(app.packageName)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                        .background(if (isBypassed) Color.White.copy(alpha = 0.05f) else Color.DarkGray.copy(alpha = 0.4f))
                        .border(1.dp, if (isBypassed) NeonGreen.copy(alpha=0.5f) else Color.DarkGray, androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = app.name, color = if (isBypassed) NeonGreen else Color.White, fontWeight = FontWeight.Bold)
                        Text(text = app.packageName, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(
                        checked = isBypassed,
                        onCheckedChange = { viewModel.toggleAppBypass(app.packageName, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NeonGreen,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.DarkGray
                        )
                    )
                }
            }
        }
    }
}
