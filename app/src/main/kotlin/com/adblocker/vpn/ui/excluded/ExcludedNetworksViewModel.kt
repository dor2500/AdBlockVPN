package com.adblocker.vpn.ui.excluded

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.telephony.TelephonyManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.adblocker.vpn.data.db.ExcludedNetworkEntity
import com.adblocker.vpn.data.model.NetworkIdentifierType
import com.adblocker.vpn.data.repository.ExcludedNetworkRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CurrentNetworkSuggestion(
    val displayName: String,
    val type: NetworkIdentifierType,
    val value: String
)

class ExcludedNetworksViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ExcludedNetworkRepository(application)

    val networks: StateFlow<List<ExcludedNetworkEntity>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addNetwork(displayName: String, type: NetworkIdentifierType, value: String) {
        viewModelScope.launch { repository.add(displayName, type, value) }
    }

    fun setEnabled(entity: ExcludedNetworkEntity, enabled: Boolean) {
        viewModelScope.launch { repository.setEnabled(entity, enabled) }
    }

    fun delete(entity: ExcludedNetworkEntity) {
        viewModelScope.launch { repository.delete(entity) }
    }

    /** Detects the currently connected network so the user can exclude it in one tap. */
    @SuppressLint("MissingPermission")
    fun detectCurrentNetwork(context: Context): CurrentNetworkSuggestion? {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return null
        val caps = cm.getNetworkCapabilities(network) ?: return null
        val linkProperties = cm.getLinkProperties(network)

        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                val gateway = linkProperties?.routes
                    ?.firstOrNull { it.isDefaultRoute }?.gateway?.hostAddress
                val ssid = runCatching {
                    (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager)
                        ?.connectionInfo?.ssid?.trim('"')
                }.getOrNull()
                when {
                    gateway != null -> CurrentNetworkSuggestion(
                        displayName = ssid ?: "Wi-Fi ($gateway)",
                        type = NetworkIdentifierType.WIFI_GATEWAY,
                        value = gateway
                    )
                    !ssid.isNullOrBlank() -> CurrentNetworkSuggestion(
                        displayName = ssid,
                        type = NetworkIdentifierType.WIFI_SSID,
                        value = ssid
                    )
                    else -> null
                }
            }
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                val mccMnc = runCatching { tm.simOperator }.getOrNull()?.takeIf { it.isNotBlank() }
                val carrier = runCatching { tm.networkOperatorName }.getOrNull()
                if (mccMnc != null) {
                    CurrentNetworkSuggestion(
                        displayName = carrier ?: mccMnc,
                        type = NetworkIdentifierType.CELLULAR_MCC_MNC,
                        value = mccMnc
                    )
                } else null
            }
            else -> null
        }
    }
}
