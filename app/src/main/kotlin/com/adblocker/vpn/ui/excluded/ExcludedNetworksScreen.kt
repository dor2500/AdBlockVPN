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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Excluded Networks") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showManualDialog = true }) {
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.WifiFind, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Exclude Current Network")
            }

            Spacer(Modifier.height(16.dp))

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
    LazyColumn {
        items(networks, key = { it.id }) { entity ->
            ExcludedNetworkRow(
                entity = entity,
                onToggle = { enabled -> viewModel.setEnabled(entity, enabled) },
                onDelete = { viewModel.delete(entity) }
            )
            Divider()
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
            .padding(vertical = 8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(entity.displayName, style = MaterialTheme.typography.bodyLarge)
            Text(
                "${typeLabel(entity.identifierType)}: ${entity.identifierValue}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
        Switch(checked = entity.enabled, onCheckedChange = onToggle)
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete")
        }
    }
}

private fun typeLabel(type: NetworkIdentifierType): String = when (type) {
    NetworkIdentifierType.WIFI_GATEWAY -> "Wi-Fi Gateway"
    NetworkIdentifierType.WIFI_SSID -> "Wi-Fi SSID"
    NetworkIdentifierType.CELLULAR_MCC_MNC -> "Carrier (MCC-MNC)"
}
