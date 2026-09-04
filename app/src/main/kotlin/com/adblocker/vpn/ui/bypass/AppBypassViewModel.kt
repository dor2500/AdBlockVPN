package com.adblocker.vpn.ui.bypass

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

data class AppInfo(
    val packageName: String,
    val name: String
)

class AppBypassViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = SettingsDataStore(application)
    private val pm = application.packageManager

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps.asStateFlow()

    private val _bypassedApps = MutableStateFlow<Set<String>>(emptySet())
    val bypassedApps: StateFlow<Set<String>> = _bypassedApps.asStateFlow()

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            val bypassed = dataStore.settingsFlow.first().bypassedApps
            _bypassedApps.value = bypassed

            val apps = withContext(Dispatchers.IO) {
                val flags = PackageManager.GET_META_DATA
                val packages = pm.getInstalledApplications(flags)
                packages.filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 || bypassed.contains(it.packageName) }
                    .map { 
                        AppInfo(
                            packageName = it.packageName,
                            name = it.loadLabel(pm).toString()
                        )
                    }
                    .sortedBy { it.name.lowercase() }
            }
            _installedApps.value = apps
        }
    }

    fun toggleAppBypass(packageName: String, bypass: Boolean) {
        viewModelScope.launch {
            val current = _bypassedApps.value.toMutableSet()
            if (bypass) {
                current.add(packageName)
            } else {
                current.remove(packageName)
            }
            _bypassedApps.value = current
            dataStore.setBypassedApps(current)
        }
    }
}
