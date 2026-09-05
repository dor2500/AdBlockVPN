package com.adblocker.vpn.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.adblocker.vpn.data.datastore.SettingsDataStore
import com.adblocker.vpn.data.model.VpnEngineState
import com.adblocker.vpn.data.model.DnsLog
import com.adblocker.vpn.vpn.AdBlockVpnService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsDataStore = SettingsDataStore(application)

    val engineState: StateFlow<VpnEngineState> = AdBlockVpnService.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VpnEngineState())

    val settings = settingsDataStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        
    val dnsLogs = AdBlockVpnService.dnsLogs
}
