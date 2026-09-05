package com.adblocker.vpn.ui.excluded

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.WifiFind
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adblocker.vpn.data.db.ExcludedNetworkEntity
import com.adblocker.vpn.data.model.NetworkIdentifierType
import com.adblocker.vpn.ui.theme.cyberBackground
import com.adblocker.vpn.ui.theme.NeonGreen
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.text.font.FontWeight

@Composable
fun ExcludedNetworksScreen(
    onBack: () -> Unit,
    viewModel: ExcludedNetworksViewModel = viewModel()
) {
    val context = LocalContext.current
    val networks by viewModel.networks.collectAsState()
    var showManualDialog by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    Scaffold(
        modifier = Modifier.cyberBackground(),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("EXCLUDED NETWORKS", style = MaterialTheme.typography.titleMedium, color = Color.White, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showManualDialog = true },
                containerColor = NeonGreen,
                contentColor = Color.Black
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add network")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Button(
                onClick = {
                    val suggestion = viewModel.detectCurrentNetwork(context)
                    if (suggestion != null) {
                        viewModel.addNetwork(suggestion.displayName, suggestion.type, suggestion.value)
                        snackbarMessage = "Excluded \"${suggestion.displayName}\""
                    } else {
                        snackbarMessage = "Could not detect current network"
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f), contentColor = NeonGreen)
            ) {
                Icon(Icons.Filled.WifiFind, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("EXCLUDE CURRENT NETWORK", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(24.dp))

            if (networks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("No excluded networks yet.", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumnNetworks(networks, viewModel)
            }
        }
    }

    if (showManualDialog) {
        AddNetworkDialog(
            onDismiss = { showManualDialog = false },
            onConfirm = { name, type, value ->
                viewModel.addNetwork(name, type, value)
                showManualDialog = false
            }
        )
    }
}

@Composable
private fun LazyColumnNetworks(
    networks: List<ExcludedNetworkEntity>,
    viewModel: ExcludedNetworksViewModel
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(networks, key = { it.id }) { entity ->
            ExcludedNetworkRow(
                entity = entity,
                onToggle = { enabled -> viewModel.setEnabled(entity, enabled) },
                onDelete = { viewModel.delete(entity) }
            )
        }
    }
}

@Composable
private fun ExcludedNetworkRow(
    entity: ExcludedNetworkEntity,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .background(if (entity.enabled) Color.White.copy(alpha = 0.05f) else Color.Red.copy(alpha=0.05f))
            .border(1.dp, if (entity.enabled) Color.White.copy(alpha = 0.1f) else Color.Red.copy(alpha=0.3f), androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(entity.displayName, style = MaterialTheme.typography.bodyLarge, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                "${typeLabel(entity.identifierType)}: ${entity.identifierValue}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray
            )
        }
        Switch(
            checked = entity.enabled, 
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NeonGreen,
                checkedTrackColor = NeonGreen.copy(alpha = 0.3f)
            )
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha=0.7f))
        }
    }
}

private fun typeLabel(type: NetworkIdentifierType): String = when (type) {
    NetworkIdentifierType.WIFI_GATEWAY -> "Wi-Fi Gateway"
    NetworkIdentifierType.WIFI_SSID -> "Wi-Fi SSID"
    NetworkIdentifierType.CELLULAR_MCC_MNC -> "Carrier (MCC-MNC)"
}
