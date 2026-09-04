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
import com.adblocker.vpn.ui.theme.DarkBackground
import com.adblocker.vpn.ui.theme.NeonGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBypassScreen(
    onBack: () -> Unit,
    viewModel: AppBypassViewModel = viewModel()
) {
    val apps by viewModel.installedApps.collectAsState()
    val bypassedApps by viewModel.bypassedApps.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Bypass (Split Tunnel)", color = NeonGreen) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
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
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            items(apps) { app ->
                val isBypassed = bypassedApps.contains(app.packageName)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = app.name, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(text = app.packageName, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(
                        checked = isBypassed,
                        onCheckedChange = { viewModel.toggleAppBypass(app.packageName, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonGreen,
                            checkedTrackColor = NeonGreen.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }
    }
}
