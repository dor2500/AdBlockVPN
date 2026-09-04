package com.adblocker.vpn.vpn

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.telephony.TelephonyManager
import android.util.Log
import com.adblocker.vpn.data.db.ExcludedNetworkEntity
import com.adblocker.vpn.data.model.NetworkIdentifierType

/** A resolved snapshot of the currently active (non-VPN) underlying network. */
data class ActiveNetworkInfo(
    val isWifi: Boolean,
    val isCellular: Boolean,
    val wifiGatewayIp: String? = null,
    val wifiSsid: String? = null,
    val cellularMccMnc: String? = null,
    val label: String
)

/**
 * Wraps ConnectivityManager.NetworkCallback and resolves each network transition
 * into an ActiveNetworkInfo the VPN service can match against the user's excluded
 * network list. Uses gateway IP (LinkProperties) as the primary Wi-Fi identifier
 * so no location permission is required; SSID is read best-effort as a fallback
 * and will simply be null on API levels/OEMs that require location for it.
 */
class NetworkMonitor(
    private val context: Context,
    private val onNetworkChanged: (ActiveNetworkInfo?) -> Unit
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private var callback: ConnectivityManager.NetworkCallback? = null

    fun start() {
        val request = android.net.NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val cb = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(
                network: Network,
                capabilities: NetworkCapabilities
            ) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) return
                resolveAndNotify(network, capabilities)
            }

            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                val caps = connectivityManager.getNetworkCapabilities(network)
                if (caps == null || caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) return
                resolveAndNotify(network, caps)
            }

            override fun onLost(network: Network) {
                onNetworkChanged(null)
            }
        }
        callback = cb
        connectivityManager.registerNetworkCallback(request, cb)
    }

    fun stop() {
        callback?.let {
            runCatching { connectivityManager.unregisterNetworkCallback(it) }
        }
        callback = null
    }

    @SuppressLint("MissingPermission")
    private fun resolveAndNotify(network: Network, capabilities: NetworkCapabilities) {
        val linkProperties = connectivityManager.getLinkProperties(network)
        val info = when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                val gateway = linkProperties?.routes
                    ?.firstOrNull { it.isDefaultRoute }
                    ?.gateway?.hostAddress
                val ssid = readSsidBestEffort()
                ActiveNetworkInfo(
                    isWifi = true,
                    isCellular = false,
                    wifiGatewayIp = gateway,
                    wifiSsid = ssid,
                    label = ssid ?: gateway ?: "Wi-Fi"
                )
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                val mccMnc = runCatching { tm.simOperator }.getOrNull()?.takeIf { it.isNotBlank() }
                val carrier = runCatching { tm.networkOperatorName }.getOrNull()
                ActiveNetworkInfo(
                    isWifi = false,
                    isCellular = true,
                    cellularMccMnc = mccMnc,
                    label = carrier ?: mccMnc ?: "Cellular"
                )
            }
            else -> null
        }
        onNetworkChanged(info)
    }

    /** Best-effort SSID read; returns null if unavailable without location permission. */
    @SuppressLint("MissingPermission")
    private fun readSsidBestEffort(): String? = try {
        val wifiManager = context.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val info: WifiInfo? = wifiManager?.connectionInfo
        info?.ssid?.trim('"')?.takeIf { it.isNotBlank() && it != WifiManager.UNKNOWN_SSID }
    } catch (e: Exception) {
        Log.d("NetworkMonitor", "SSID unavailable: ${e.message}")
        null
    }

    companion object {
        /** True if [active] matches any enabled entry in [excluded]. */
        fun matches(active: ActiveNetworkInfo?, excluded: List<ExcludedNetworkEntity>): Boolean {
            if (active == null) return false
            return excluded.any { entry ->
                when (entry.identifierType) {
                    NetworkIdentifierType.WIFI_GATEWAY ->
                        active.isWifi && active.wifiGatewayIp != null &&
                            active.wifiGatewayIp == entry.identifierValue
                    NetworkIdentifierType.WIFI_SSID ->
                        active.isWifi && active.wifiSsid != null &&
                            active.wifiSsid == entry.identifierValue
                    NetworkIdentifierType.CELLULAR_MCC_MNC ->
                        active.isCellular && active.cellularMccMnc != null &&
                            active.cellularMccMnc == entry.identifierValue
                }
            }
        }
    }
}
