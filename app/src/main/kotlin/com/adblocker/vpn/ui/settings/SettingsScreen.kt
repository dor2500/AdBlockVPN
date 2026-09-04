@file:OptIn(ExperimentalMaterial3Api::class)
package com.adblocker.vpn.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adblocker.vpn.util.Constants
import com.adblocker.vpn.ui.theme.NeonGreen

@Composable
fun SettingsScreen(
    onNavigateToExcluded: () -> Unit,
    onNavigateToAppBypass: () -> Unit,
    onNavigateToAppFirewall: () -> Unit,
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val updateState by viewModel.updateState.collectAsState()

    var upstreamPrimary by remember(settings.upstreamPrimary) { mutableStateOf(settings.upstreamPrimary) }
    var upstreamSecondary by remember(settings.upstreamSecondary) { mutableStateOf(settings.upstreamSecondary) }
    var newWhitelistDomain by remember { mutableStateOf("") }
    var newBlacklistDomain by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {
            item {
                Spacer(Modifier.height(16.dp))
                
                // Excluded Networks Section
                Text("Network Bypass", style = MaterialTheme.typography.titleSmall, color = NeonGreen, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Card(
                    onClick = onNavigateToExcluded,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Close, contentDescription = null, tint = Color.Gray)
                        Spacer(Modifier.width(16.dp))
                        Text("Manage Excluded Networks", modifier = Modifier.weight(1f))
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                Card(
                    onClick = onNavigateToAppBypass,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Close, contentDescription = null, tint = Color.Gray)
                        Spacer(Modifier.width(16.dp))
                        Text("App Bypass (Split Tunneling)", modifier = Modifier.weight(1f))
                    }
                }

                Spacer(Modifier.height(8.dp))

                Card(
                    onClick = onNavigateToAppFirewall,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Close, contentDescription = null, tint = Color.Red)
                        Spacer(Modifier.width(16.dp))
                        Text("App Firewall (Killswitch)", modifier = Modifier.weight(1f), color = Color.Red)
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                HorizontalDivider(color = Color.DarkGray)
                Spacer(Modifier.height(24.dp))

                Text("Blocklist Sources", style = MaterialTheme.typography.titleSmall, color = NeonGreen, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                
                Constants.BLOCKLISTS.forEach { (name, url) ->
                    val isChecked = settings.activeBlocklists.contains(url)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            colors = CheckboxDefaults.colors(checkedColor = NeonGreen, checkmarkColor = Color.Black),
                            onCheckedChange = { checked ->
                                val newSet = settings.activeBlocklists.toMutableSet()
                                if (checked) newSet.add(url) else newSet.remove(url)
                                viewModel.setActiveBlocklists(newSet)
                            }
                        )
                        Text(name, modifier = Modifier.padding(start = 8.dp))
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.refreshBlocklists(settings.activeBlocklists) },
                    enabled = updateState !is BlocklistUpdateState.Updating,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (updateState is BlocklistUpdateState.Updating) "Updating..." else "Sync Blocklists Now")
                }
                when (val s = updateState) {
                    is BlocklistUpdateState.Success -> Text(
                        "Loaded ${s.domainCount} domains",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    is BlocklistUpdateState.Error -> Text(
                        "Error: ${s.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    else -> {}
                }

                Spacer(Modifier.height(24.dp))
                HorizontalDivider(color = Color.DarkGray)
                Spacer(Modifier.height(24.dp))

                Text("Upstream DNS", style = MaterialTheme.typography.titleSmall, color = NeonGreen, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { upstreamPrimary = "1.1.1.1"; upstreamSecondary = "1.0.0.1" }, modifier = Modifier.weight(1f)) {
                        Text("Cloudflare")
                    }
                    OutlinedButton(onClick = { upstreamPrimary = "8.8.8.8"; upstreamSecondary = "8.8.4.4" }, modifier = Modifier.weight(1f)) {
                        Text("Google")
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { upstreamPrimary = "9.9.9.9"; upstreamSecondary = "149.112.112.112" }, modifier = Modifier.weight(1f)) {
                        Text("Quad9")
                    }
                    OutlinedButton(onClick = { upstreamPrimary = "94.140.14.14"; upstreamSecondary = "94.140.15.15" }, modifier = Modifier.weight(1f)) {
                        Text("AdGuard")
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = upstreamPrimary,
                    onValueChange = { upstreamPrimary = it },
                    label = { Text("Primary resolver") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = upstreamSecondary,
                    onValueChange = { upstreamSecondary = it },
                    label = { Text("Secondary resolver") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = { viewModel.setUpstream(upstreamPrimary, upstreamSecondary) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Save DNS Configuration")
                }

                Spacer(Modifier.height(24.dp))
                HorizontalDivider(color = Color.DarkGray)
                Spacer(Modifier.height(24.dp))
                
                Text("Advanced Cyber Protection", style = MaterialTheme.typography.titleSmall, color = NeonGreen, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("DNS-over-HTTPS (DoH)", modifier = Modifier.weight(1f))
                    Switch(
                        checked = settings.useDoh,
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen, checkedTrackColor = NeonGreen.copy(alpha=0.3f)),
                        onCheckedChange = { viewModel.setUseDoh(it) }
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Zero-Day DGA Heuristics", modifier = Modifier.weight(1f))
                    Switch(
                        checked = settings.enableZeroDayProtection,
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen, checkedTrackColor = NeonGreen.copy(alpha=0.3f)),
                        onCheckedChange = { viewModel.setEnableZeroDayProtection(it) }
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Auto-start on boot", modifier = Modifier.weight(1f))
                    Switch(
                        checked = settings.autoStartOnBoot,
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen, checkedTrackColor = NeonGreen.copy(alpha=0.3f)),
                        onCheckedChange = { viewModel.setAutoStart(it) }
                    )
                }

                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { viewModel.resetStats() }, 
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Text("Reset Query Statistics")
                }

                Spacer(Modifier.height(24.dp))
                HorizontalDivider(color = Color.DarkGray)
                Spacer(Modifier.height(24.dp))

                Text("Whitelist (never block)", style = MaterialTheme.typography.titleSmall, color = NeonGreen, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                DomainInputRow(
                    value = newWhitelistDomain,
                    onValueChange = { newWhitelistDomain = it },
                    onAdd = {
                        if (newWhitelistDomain.isNotBlank()) {
                            viewModel.addWhitelist(newWhitelistDomain)
                            newWhitelistDomain = ""
                        }
                    }
                )
            }

            items(settings.whitelist.toList()) { domain ->
                DomainChip(domain) { viewModel.removeWhitelist(domain) }
            }

            item {
                Spacer(Modifier.height(24.dp))
                HorizontalDivider(color = Color.DarkGray)
                Spacer(Modifier.height(24.dp))
                Text("Custom Blacklist (always block)", style = MaterialTheme.typography.titleSmall, color = NeonGreen, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                DomainInputRow(
                    value = newBlacklistDomain,
                    onValueChange = { newBlacklistDomain = it },
                    onAdd = {
                        if (newBlacklistDomain.isNotBlank()) {
                            viewModel.addBlacklist(newBlacklistDomain)
                            newBlacklistDomain = ""
                        }
                    }
                )
            }

            items(settings.blacklist.toList()) { domain ->
                DomainChip(domain) { viewModel.removeBlacklist(domain) }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun DomainInputRow(value: String, onValueChange: (String) -> Unit, onAdd: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("domain.com") },
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        Button(onClick = onAdd) { Text("Add") }
    }
}

@Composable
private fun DomainChip(domain: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(domain, modifier = Modifier.weight(1f))
        IconButton(onClick = onRemove) {
            Icon(Icons.Filled.Close, contentDescription = "Remove")
        }
    }
}
