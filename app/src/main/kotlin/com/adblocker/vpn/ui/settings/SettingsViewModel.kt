package com.adblocker.vpn.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.adblocker.vpn.data.datastore.AppSettings
import com.adblocker.vpn.data.datastore.SettingsDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class BlocklistUpdateState {
    object Idle : BlocklistUpdateState()
    object Updating : BlocklistUpdateState()
    data class Success(val domainCount: Int) : BlocklistUpdateState()
    data class Error(val message: String) : BlocklistUpdateState()
}

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = SettingsDataStore(application)

    val settings: StateFlow<AppSettings> = dataStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    private val _updateState = MutableStateFlow<BlocklistUpdateState>(BlocklistUpdateState.Idle)
    val updateState: StateFlow<BlocklistUpdateState> = _updateState.asStateFlow()

    fun setActiveBlocklists(urls: Set<String>) = viewModelScope.launch { dataStore.setActiveBlocklists(urls) }

    fun setUpstream(primary: String, secondary: String) =
        viewModelScope.launch { dataStore.setUpstream(primary, secondary) }

    private fun extractDomain(input: String): String {
        var domain = input.trim().lowercase()
        try {
            if (domain.startsWith("http://") || domain.startsWith("https://")) {
                val uri = java.net.URI(domain)
                uri.host?.let { domain = it }
            } else if (domain.contains("/")) {
                domain = domain.substringBefore("/")
            }
        } catch (e: Exception) {
            // ignore
        }
        domain = domain.removePrefix("www.")
        return domain
    }

    fun addWhitelist(domain: String) = viewModelScope.launch { 
        val cleanDomain = extractDomain(domain)
        if (cleanDomain.isNotBlank()) {
            dataStore.addToWhitelist(cleanDomain) 
        }
    }
    fun removeWhitelist(domain: String) = viewModelScope.launch { dataStore.removeFromWhitelist(domain) }
    fun addBlacklist(domain: String) = viewModelScope.launch { dataStore.addToBlacklist(domain) }
    fun removeBlacklist(domain: String) = viewModelScope.launch { dataStore.removeFromBlacklist(domain) }
    fun setAutoStart(enabled: Boolean) = viewModelScope.launch { dataStore.setAutoStartOnBoot(enabled) }
    
    fun setVpnLocation(locationId: String) = viewModelScope.launch { dataStore.setVpnLocation(locationId) }

    fun setUseDoh(enabled: Boolean) = viewModelScope.launch {
        dataStore.setUseDoh(enabled)
    }

    fun setEnableZeroDayProtection(enabled: Boolean) = viewModelScope.launch {
        dataStore.setEnableZeroDayProtection(enabled)
    }
    
    fun setAggressiveFirewall(enabled: Boolean) = viewModelScope.launch {
        dataStore.setAggressiveFirewall(enabled)
    }

    fun setTheme(themeId: String) = viewModelScope.launch {
        dataStore.setTheme(themeId)
    }

    fun setVpnProtocol(protocol: String) = viewModelScope.launch { dataStore.setVpnProtocol(protocol) }
    fun setMultiHopEnabled(enabled: Boolean) = viewModelScope.launch { dataStore.setMultiHopEnabled(enabled) }
    fun setCustomMtu(mtu: Int) = viewModelScope.launch { dataStore.setCustomMtu(mtu) }
    fun setAutoConnectWifi(enabled: Boolean) = viewModelScope.launch { dataStore.setAutoConnectWifi(enabled) }
    
    fun resetStats() = viewModelScope.launch { dataStore.resetStats() }

    /**
     * Triggers a blocklist refresh. The actual download runs inside the running
     * VpnService's BlocklistManager instance; here we drive the same manager
     * type standalone (via cacheDir) so an update works even before the VPN
     * has ever started, then the service will pick up the new cache file the
     * next time it (re)loads.
     */
    fun refreshBlocklists(urls: Set<String>) {
        viewModelScope.launch {
            _updateState.value = BlocklistUpdateState.Updating
            val manager = com.adblocker.vpn.vpn.BlocklistManager(getApplication<Application>().cacheDir)
            manager.setActiveLists(urls)
            val result = manager.updateAll()
            _updateState.value = result.fold(
                onSuccess = { count -> BlocklistUpdateState.Success(count) },
                onFailure = { e -> BlocklistUpdateState.Error(e.message ?: "Update failed") }
            )
            dataStore.setActiveBlocklists(urls)
        }
    }
}
