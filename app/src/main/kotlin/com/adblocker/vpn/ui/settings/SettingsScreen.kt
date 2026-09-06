@file:OptIn(ExperimentalMaterial3Api::class)
package com.adblocker.vpn.ui.settings

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.adblocker.vpn.ui.theme.cyberBackground
import androidx.compose.ui.platform.LocalContext
import com.adblocker.vpn.BuildConfig
import com.adblocker.vpn.ui.theme.NeonGreen
import com.adblocker.vpn.ui.theme.GlassBackground
import com.adblocker.vpn.ui.theme.GlassBorder

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
            title = { Text("Update Available") },
            text = { Text("Version ${updateInfo!!.latestVersion} is available!\n\n${updateInfo!!.releaseNotes}") },
            confirmButton = {
                Button(onClick = {
                    showUpdateDialog = false
                    Updater.downloadAndInstallUpdate(context, updateInfo!!.downloadUrl, updateInfo!!.latestVersion)
                }) {
                    Text("Update Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateDialog = false }) {
                    Text("Later")
                }
            }
        )
    }
    
    if (updateMessage != null) {
        AlertDialog(
            onDismissRequest = { updateMessage = null },
            title = { Text("Check for Updates") },
            text = { Text(updateMessage!!) },
            confirmButton = {
                Button(onClick = { updateMessage = null }) { Text("OK") }
            }
        )
    }

    Scaffold(
        modifier = Modifier.cyberBackground(),
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("SETTINGS", fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 1.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
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
            
            // --- Appearance Section ---
            item {
                SectionHeader("APPEARANCE")
                SettingsCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeButton(name = "System", isSelected = settings.selectedTheme == "system", onClick = { viewModel.setTheme("system") }, modifier = Modifier.weight(1f))
                        ThemeButton(name = "Light", isSelected = settings.selectedTheme == "light", onClick = { viewModel.setTheme("light") }, modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeButton(name = "Slate", isSelected = settings.selectedTheme == "slate", onClick = { viewModel.setTheme("slate") }, modifier = Modifier.weight(1f))
                        ThemeButton(name = "Midnight", isSelected = settings.selectedTheme == "midnight", onClick = { viewModel.setTheme("midnight") }, modifier = Modifier.weight(1f))
                    }
                }
            }

            // --- Network Control Section ---
            item {
                SectionHeader("NETWORK CONTROL")
                SettingsCard {
                    NavigationRow(title = "Manage Excluded Networks", onClick = onNavigateToExcluded)
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    NavigationRow(title = "App Bypass (Split Tunneling)", onClick = onNavigateToAppBypass)
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    NavigationRow(title = "App Firewall (Killswitch)", onClick = onNavigateToAppFirewall, isDestructive = true)
                }
            }

            // --- Advanced Protection Section ---
            item {
                SectionHeader("ADVANCED PROTECTION")
                SettingsCard {
                    SwitchRow("DNS-over-HTTPS (DoH)", "Encrypts DNS queries (may increase latency)", settings.useDoh) {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        viewModel.setUseDoh(it)
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    SwitchRow("Zero-Day DGA Heuristics", "Blocks procedurally generated malware domains", settings.enableZeroDayProtection) {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        viewModel.setEnableZeroDayProtection(it)
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    SwitchRow("Auto-start on boot", "Launch VPN service when device starts", settings.autoStartOnBoot) {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        viewModel.setAutoStart(it)
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    SwitchRow("Aggressive Firewall", "Restart interface immediately on app rule change", settings.aggressiveFirewall) {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        viewModel.setAggressiveFirewall(it)
                    }
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { viewModel.resetStats() }, 
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                    ) {
                        Text("Reset Query Statistics", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // --- Upstream DNS Section ---
            item {
                SectionHeader("UPSTREAM DNS")
                SettingsCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { upstreamPrimary = "1.1.1.1"; upstreamSecondary = "1.0.0.1" }, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                            Text("Cloudflare")
                        }
                        OutlinedButton(onClick = { upstreamPrimary = "8.8.8.8"; upstreamSecondary = "8.8.4.4" }, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                            Text("Google")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { upstreamPrimary = "9.9.9.9"; upstreamSecondary = "149.112.112.112" }, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                            Text("Quad9")
                        }
                        OutlinedButton(onClick = { upstreamPrimary = "94.140.14.14"; upstreamSecondary = "94.140.15.15" }, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                            Text("AdGuard")
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = upstreamPrimary,
                        onValueChange = { upstreamPrimary = it },
                        label = { Text("Primary resolver") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = upstreamSecondary,
                        onValueChange = { upstreamSecondary = it },
                        label = { Text("Secondary resolver") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.setUpstream(upstreamPrimary, upstreamSecondary) }, 
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Save DNS Configuration", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // --- Blocklist Sources Section ---
            item {
                SectionHeader("BLOCKLIST SOURCES")
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
                                colors = CheckboxDefaults.colors(checkedColor = NeonGreen, checkmarkColor = Color.Black)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(name, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                        }
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.refreshBlocklists(settings.activeBlocklists) },
                        enabled = updateState !is BlocklistUpdateState.Updating,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text(if (updateState is BlocklistUpdateState.Updating) "UPDATING..." else "SYNC BLOCKLISTS NOW", fontWeight = FontWeight.Bold)
                    }
                    when (val s = updateState) {
                        is BlocklistUpdateState.Success -> Text("Loaded ${s.domainCount} domains", color = NeonGreen, modifier = Modifier.padding(top = 8.dp))
                        is BlocklistUpdateState.Error -> Text("Error: ${s.message}", color = Color.Red, modifier = Modifier.padding(top = 8.dp))
                        else -> {}
                    }
                }
            }

            // --- Whitelist Section ---
            item {
                SectionHeader("WHITELIST (NEVER BLOCK)")
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
                                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                            }
                        }
                    }
                }
            }

            // --- Custom Blacklist Section ---
            item {
                SectionHeader("CUSTOM BLACKLIST (ALWAYS BLOCK)")
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
                                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                            }
                        }
                    }
                }
            }

            // --- About & Updates Section ---
            item {
                SectionHeader("ABOUT & UPDATES")
                SettingsCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Current Version", color = Color.White)
                        Text("v${BuildConfig.VERSION_NAME}", color = Color.LightGray)
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
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black)
                    ) {
                        if (isCheckingUpdate) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black, strokeWidth = 2.dp)
                        } else {
                            Text("Check for Updates", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = onNavigateToChangelog,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("View Changelog", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
        style = MaterialTheme.typography.labelMedium,
        color = NeonGreen,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GlassBackground),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, GlassBorder, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun ThemeButton(name: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val targetContainerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val targetContentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
    
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
    val color = if (isDestructive) Color.Red else Color.White
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, modifier = Modifier.weight(1f), color = color, fontWeight = if (isDestructive) FontWeight.Bold else FontWeight.Normal)
        Icon(Icons.Filled.Close, contentDescription = null, tint = color.copy(alpha = 0.5f))
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
            Text(title, color = Color.White, fontWeight = FontWeight.Medium)
            Text(subtitle, color = Color.LightGray, fontSize = 12.sp, lineHeight = 16.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen, checkedTrackColor = NeonGreen.copy(alpha=0.3f))
        )
    }
}

@Composable
private fun DomainInputRow(value: String, onValueChange: (String) -> Unit, onAdd: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("example.com") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        Spacer(Modifier.width(12.dp))
        IconButton(
            onClick = onAdd,
            modifier = Modifier
                .size(50.dp)
                .background(NeonGreen, RoundedCornerShape(12.dp))
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Add", tint = Color.Black)
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
        Text(domain, modifier = Modifier.weight(1f), color = Color.White, fontSize = 15.sp)
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(Icons.Rounded.Delete, contentDescription = "Remove", tint = Color.Red.copy(alpha = 0.8f))
        }
    }
}
