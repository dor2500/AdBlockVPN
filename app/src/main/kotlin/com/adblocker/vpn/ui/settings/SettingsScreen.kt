@file:OptIn(ExperimentalMaterial3Api::class)
package com.adblocker.vpn.ui.settings

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.adblocker.vpn.util.Constants
import com.adblocker.vpn.util.Updater
import com.adblocker.vpn.util.UpdateInfo
import androidx.compose.ui.platform.LocalContext
import com.adblocker.vpn.BuildConfig

import androidx.compose.ui.res.stringResource
import com.adblocker.vpn.R

@Composable
fun SettingsScreen(
    onNavigateToExcluded: () -> Unit,
    onNavigateToAppBypass: () -> Unit,
    onNavigateToAppFirewall: () -> Unit,
    onNavigateToChangelog: () -> Unit,
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val view = LocalView.current

    var upstreamPrimary by remember(settings.upstreamPrimary) { mutableStateOf(settings.upstreamPrimary) }
    var upstreamSecondary by remember(settings.upstreamSecondary) { mutableStateOf(settings.upstreamSecondary) }
    var newWhitelistDomain by remember { mutableStateOf("") }
    var newBlacklistDomain by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var isCheckingUpdate by remember { mutableStateOf(false) }
    var updateMessage by remember { mutableStateOf<String?>(null) }

    if (showUpdateDialog && updateInfo != null) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = { Text(stringResource(R.string.update_available_title)) },
            text = { Text(stringResource(R.string.update_desc, updateInfo!!.latestVersion, updateInfo!!.releaseNotes)) },
            confirmButton = {
                Button(onClick = {
                    showUpdateDialog = false
                    Updater.downloadAndInstallUpdate(context, updateInfo!!.downloadUrl, updateInfo!!.latestVersion)
                }) {
                    Text(stringResource(R.string.update_now_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateDialog = false }) {
                    Text(stringResource(R.string.later_button))
                }
            }
        )
    }
    
    if (updateMessage != null) {
        AlertDialog(
            onDismissRequest = { updateMessage = null },
            title = { Text(stringResource(R.string.check_for_updates_title)) },
            text = { Text(updateMessage!!) },
            confirmButton = {
                Button(onClick = { updateMessage = null }) { Text(stringResource(R.string.ok)) }
            }
        )
    }

    Scaffold(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.settings_title), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            
            // --- Appearance & Language Section ---
            item {
                SectionHeader(stringResource(R.string.settings_appearance))
                SettingsCard {
                    Text(stringResource(R.string.settings_language), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val currentLocales = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales()
                        val isHe = currentLocales.toLanguageTags().contains("he")
                        
                        OutlinedButton(
                            onClick = { androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(androidx.core.os.LocaleListCompat.forLanguageTags("en")) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (!isHe) MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (!isHe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(stringResource(R.string.settings_english), fontWeight = if (!isHe) FontWeight.Bold else FontWeight.Normal)
                        }
                        
                        OutlinedButton(
                            onClick = { androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(androidx.core.os.LocaleListCompat.forLanguageTags("he")) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isHe) MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (isHe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(stringResource(R.string.settings_hebrew), fontWeight = if (isHe) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                    
                    DividerItem()
                    
                    Text(stringResource(R.string.settings_theme), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeButton(name = "Glass Dark", isSelected = settings.selectedTheme == "glass", onClick = { viewModel.setTheme("glass") }, modifier = Modifier.weight(1f))
                        ThemeButton(name = "Aurora", isSelected = settings.selectedTheme == "aurora", onClick = { viewModel.setTheme("aurora") }, modifier = Modifier.weight(1f))
                        ThemeButton(name = "Eclipse", isSelected = settings.selectedTheme == "eclipse", onClick = { viewModel.setTheme("eclipse") }, modifier = Modifier.weight(1f))
                    }
                }
            }

            // --- Network Control Section ---
            item {
                SectionHeader(stringResource(R.string.settings_network_control))
                SettingsCard {
                    NavigationRow(title = stringResource(R.string.settings_manage_excluded), onClick = onNavigateToExcluded)
                    DividerItem()
                    NavigationRow(title = stringResource(R.string.settings_app_bypass), onClick = onNavigateToAppBypass)
                    DividerItem()
                    NavigationRow(title = stringResource(R.string.settings_app_firewall), onClick = onNavigateToAppFirewall, isDestructive = true)
                }
            }

            // --- Pro Mode: Advanced Protocols ---
            item {
                SectionHeader(stringResource(R.string.settings_pro_mode))
                SettingsCard {
                    Text(stringResource(R.string.settings_vpn_protocol), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(stringResource(R.string.settings_select_tunnel), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    
                    var expanded by remember { mutableStateOf(false) }
                    val protocols = mapOf(
                        "wireguard" to "WireGuard (Fastest)",
                        "openvpn_udp" to "OpenVPN UDP",
                        "openvpn_tcp" to "OpenVPN TCP",
                        "ikev2" to "IKEv2 / IPsec",
                        "stealthguard" to "StealthGuard (Obfuscated)"
                    )
                    
                    Box {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(protocols[settings.vpnProtocol] ?: "WireGuard")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            protocols.forEach { (id, label) ->
                                DropdownMenuItem(
                                    text = { Text(label, color = MaterialTheme.colorScheme.onSurface) },
                                    onClick = {
                                        viewModel.setVpnProtocol(id)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    DividerItem()
                    
                    SwitchRow(
                        stringResource(R.string.settings_multi_hop), 
                        stringResource(R.string.settings_multi_hop_desc), 
                        settings.multiHopEnabled
                    ) {
                        viewModel.setMultiHopEnabled(it)
                    }
                    
                    DividerItem()
                    
                    SwitchRow(
                        stringResource(R.string.settings_auto_connect), 
                        stringResource(R.string.settings_auto_connect_desc), 
                        settings.autoConnectInsecureWifi
                    ) {
                        viewModel.setAutoConnectWifi(it)
                    }
                }
            }

            // --- Advanced Protection Section ---
            item {
                SectionHeader(stringResource(R.string.settings_advanced_protection))
                SettingsCard {
                    SwitchRow(stringResource(R.string.settings_doh), stringResource(R.string.settings_doh_desc), settings.useDoh) {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        viewModel.setUseDoh(it)
                    }
                    DividerItem()
                    SwitchRow(stringResource(R.string.settings_zeroday_opt), stringResource(R.string.settings_zeroday_desc), settings.enableZeroDayProtection) {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        viewModel.setEnableZeroDayProtection(it)
                    }
                    DividerItem()
                    SwitchRow(stringResource(R.string.settings_autostart), stringResource(R.string.settings_autostart_desc), settings.autoStartOnBoot) {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        viewModel.setAutoStart(it)
                    }
                    DividerItem()
                    SwitchRow(stringResource(R.string.settings_aggressive), stringResource(R.string.settings_aggressive_desc), settings.aggressiveFirewall) {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        viewModel.setAggressiveFirewall(it)
                    }
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { viewModel.resetStats() }, 
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.settings_reset_stats), fontWeight = FontWeight.Bold)
                    }
                }
            }

            // --- Upstream DNS Section ---
            item {
                SectionHeader(stringResource(R.string.settings_upstream))
                SettingsCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { upstreamPrimary = "1.1.1.1"; upstreamSecondary = "1.0.0.1" }, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.cloudflare))
                        }
                        OutlinedButton(onClick = { upstreamPrimary = "8.8.8.8"; upstreamSecondary = "8.8.4.4" }, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.google))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { upstreamPrimary = "9.9.9.9"; upstreamSecondary = "149.112.112.112" }, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.quad9))
                        }
                        OutlinedButton(onClick = { upstreamPrimary = "94.140.14.14"; upstreamSecondary = "94.140.15.15" }, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.adguard))
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = upstreamPrimary,
                        onValueChange = { upstreamPrimary = it },
                        label = { Text(stringResource(R.string.settings_primary_resolver)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = upstreamSecondary,
                        onValueChange = { upstreamSecondary = it },
                        label = { Text(stringResource(R.string.settings_secondary_resolver)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.setUpstream(upstreamPrimary, upstreamSecondary) }, 
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                    ) {
                        Text(stringResource(R.string.settings_save_dns), fontWeight = FontWeight.Bold)
                    }
                }
            }

            // --- Blocklist Sources Section ---
            item {
                SectionHeader(stringResource(R.string.settings_blocklists))
                SettingsCard {
                    Constants.BLOCKLISTS.forEach { (name, url) ->
                        val isChecked = settings.activeBlocklists.contains(url)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    val newSet = settings.activeBlocklists.toMutableSet()
                                    if (!isChecked) newSet.add(url) else newSet.remove(url)
                                    viewModel.setActiveBlocklists(newSet)
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = null,
                                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary, checkmarkColor = MaterialTheme.colorScheme.onPrimary)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(name, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                        }
                        DividerItem()
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.refreshBlocklists(settings.activeBlocklists) },
                        enabled = updateState !is BlocklistUpdateState.Updating,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text(if (updateState is BlocklistUpdateState.Updating) stringResource(R.string.settings_updating) else stringResource(R.string.settings_sync_now), fontWeight = FontWeight.Bold)
                    }
                    when (val s = updateState) {
                        is BlocklistUpdateState.Success -> Text(stringResource(R.string.loaded_domains, s.domainCount), color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
                        is BlocklistUpdateState.Error -> Text(stringResource(R.string.error_prefix, s.message), color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                        else -> {}
                    }
                }
            }

            // --- Whitelist Section ---
            item {
                SectionHeader(stringResource(R.string.settings_whitelist))
                DomainInputRow(value = newWhitelistDomain, onValueChange = { newWhitelistDomain = it }) {
                    if (newWhitelistDomain.isNotBlank()) {
                        viewModel.addWhitelist(newWhitelistDomain)
                        newWhitelistDomain = ""
                    }
                }
            }

            if (settings.whitelist.isNotEmpty()) {
                item {
                    SettingsCard {
                        settings.whitelist.forEachIndexed { index, domain ->
                            DomainChip(domain) { viewModel.removeWhitelist(domain) }
                            if (index < settings.whitelist.size - 1) {
                                DividerItem()
                            }
                        }
                    }
                }
            }

            // --- Custom Blacklist Section ---
            item {
                SectionHeader(stringResource(R.string.settings_blacklist))
                DomainInputRow(value = newBlacklistDomain, onValueChange = { newBlacklistDomain = it }) {
                    if (newBlacklistDomain.isNotBlank()) {
                        viewModel.addBlacklist(newBlacklistDomain)
                        newBlacklistDomain = ""
                    }
                }
            }

            if (settings.blacklist.isNotEmpty()) {
                item {
                    SettingsCard {
                        settings.blacklist.forEachIndexed { index, domain ->
                            DomainChip(domain) { viewModel.removeBlacklist(domain) }
                            if (index < settings.blacklist.size - 1) {
                                DividerItem()
                            }
                        }
                    }
                }
            }

            // --- About & Updates Section ---
            item {
                SectionHeader(stringResource(R.string.settings_about))
                SettingsCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.settings_current_version), color = MaterialTheme.colorScheme.onSurface)
                        Text("v${BuildConfig.VERSION_NAME}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = {
                            if (!isCheckingUpdate) {
                                isCheckingUpdate = true
                                scope.launch {
                                    val info = Updater.checkForUpdate()
                                    isCheckingUpdate = false
                                    if (info != null) {
                                        if (info.isUpdateAvailable) {
                                            updateInfo = info
                                            showUpdateDialog = true
                                        } else {
                                            updateMessage = "You are on the latest version."
                                        }
                                    } else {
                                        updateMessage = "Failed to check for updates. GitHub rate limit or network error."
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                    ) {
                        if (isCheckingUpdate) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        } else {
                            Text(stringResource(R.string.settings_check_updates), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = onNavigateToChangelog,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(stringResource(R.string.settings_view_changelog), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Black,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun ThemeButton(name: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val targetContainerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val targetContentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    
    val containerColor by androidx.compose.animation.animateColorAsState(targetContainerColor, label = "theme_bg")
    val contentColor by androidx.compose.animation.animateColorAsState(targetContentColor, label = "theme_text")
    
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = containerColor, contentColor = contentColor),
        modifier = modifier
    ) {
        Text(name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun NavigationRow(title: String, onClick: () -> Unit, isDestructive: Boolean = false) {
    val color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, modifier = Modifier.weight(1f), color = color, fontWeight = if (isDestructive) FontWeight.Bold else FontWeight.Normal)
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = color.copy(alpha = 0.5f))
    }
}

@Composable
private fun SwitchRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, lineHeight = 16.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.onPrimary, checkedTrackColor = MaterialTheme.colorScheme.primary)
        )
    }
}

@Composable
private fun DomainInputRow(value: String, onValueChange: (String) -> Unit, onAdd: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(stringResource(R.string.example_domain)) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        Spacer(Modifier.width(12.dp))
        IconButton(
            onClick = onAdd,
            modifier = Modifier
                .size(50.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
private fun DomainChip(domain: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(domain, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(Icons.Rounded.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun DividerItem() {
    HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
}
