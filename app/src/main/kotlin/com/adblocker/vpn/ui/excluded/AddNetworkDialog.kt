package com.adblocker.vpn.ui.excluded

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.adblocker.vpn.data.model.NetworkIdentifierType

@Composable
fun AddNetworkDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: NetworkIdentifierType, value: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(NetworkIdentifierType.WIFI_GATEWAY) }
    var typeMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Excluded Network") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = typeMenuExpanded,
                    onExpandedChange = { typeMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = typeDisplayName(type),
                        onValueChange = {},
                        label = { Text("Match by") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = typeMenuExpanded,
                        onDismissRequest = { typeMenuExpanded = false }
                    ) {
                        NetworkIdentifierType.values().forEach { option ->
                            DropdownMenuItem(
                                text = { Text(typeDisplayName(option)) },
                                onClick = { type = option; typeMenuExpanded = false }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text(valueLabel(type)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.ifBlank { value }, type, value) },
                enabled = value.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private fun typeDisplayName(type: NetworkIdentifierType): String = when (type) {
    NetworkIdentifierType.WIFI_GATEWAY -> "Wi-Fi Gateway IP"
    NetworkIdentifierType.WIFI_SSID -> "Wi-Fi SSID"
    NetworkIdentifierType.CELLULAR_MCC_MNC -> "Carrier MCC-MNC"
}

private fun valueLabel(type: NetworkIdentifierType): String = when (type) {
    NetworkIdentifierType.WIFI_GATEWAY -> "Gateway IP (e.g. 192.168.1.1)"
    NetworkIdentifierType.WIFI_SSID -> "SSID"
    NetworkIdentifierType.CELLULAR_MCC_MNC -> "MCC-MNC (e.g. 425 01)"
}
