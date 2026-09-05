package com.adblocker.vpn.ui.firewall

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.adblocker.vpn.data.datastore.SettingsDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val name: String,
    val icon: Drawable? = null
)

class AppFirewallViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = SettingsDataStore(application)
    private val pm = application.packageManager

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps.asStateFlow()

    private val _blockedApps = MutableStateFlow<Set<String>>(emptySet())
    val blockedApps: StateFlow<Set<String>> = _blockedApps.asStateFlow()

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            val blocked = dataStore.settingsFlow.first().blockedInternetApps
            _blockedApps.value = blocked

            val apps = withContext(Dispatchers.IO) {
                val flags = PackageManager.GET_META_DATA
                val packages = pm.getInstalledApplications(flags)
                packages.filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 || blocked.contains(it.packageName) }
                    .map { 
                        AppInfo(
                            packageName = it.packageName,
                            name = it.loadLabel(pm).toString(),
                            icon = it.loadIcon(pm)
                        )
                    }
                    .sortedBy { it.name.lowercase() }
            }
            _installedApps.value = apps
        }
    }

    fun toggleAppBlock(packageName: String, block: Boolean) {
        viewModelScope.launch {
            val current = _blockedApps.value.toMutableSet()
            if (block) {
                current.add(packageName)
            } else {
                current.remove(packageName)
            }
            _blockedApps.value = current
            dataStore.setBlockedInternetApps(current)
        }
    }
}
