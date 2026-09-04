package com.adblocker.vpn.data.model

/** Type of network identifier used to match the currently active network. */
enum class NetworkIdentifierType { WIFI_GATEWAY, WIFI_SSID, CELLULAR_MCC_MNC }

data class VpnEngineState(
    val isRunning: Boolean = false,
    val isPassThrough: Boolean = false,
    val activeNetworkLabel: String? = null,
    val queriesTotal: Long = 0,
    val queriesBlocked: Long = 0,
    val queriesZeroDayBlocked: Long = 0
)
