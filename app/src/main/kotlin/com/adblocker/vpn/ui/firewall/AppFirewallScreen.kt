package com.adblocker.vpn.ui.firewall

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
import com.adblocker.vpn.ui.theme.NeonCyan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppFirewallScreen(
    onBack: () -> Unit,
    viewModel: AppFirewallViewModel = viewModel()
) {
    val apps by viewModel.installedApps.collectAsState()
    val blockedApps by viewModel.blockedApps.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Firewall (Killswitch)", color = NeonCyan) },
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
                    text = "Apps turned ON will have their DNS requests blackholed, effectively blocking them from the internet.",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            items(apps) { app ->
                val isBlocked = blockedApps.contains(app.packageName)
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
                        checked = isBlocked,
                        onCheckedChange = { viewModel.toggleAppBlock(app.packageName, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Red,
                            checkedTrackColor = Color.Red.copy(alpha = 0.3f),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.DarkGray
                        )
                    )
                }
            }
        }
    }
}
