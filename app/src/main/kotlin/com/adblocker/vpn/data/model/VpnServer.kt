package com.adblocker.vpn.data.model

data class VpnServer(
    val id: String,
    val name: String,
    val countryCode: String,
    val ipAddress: String,
    val latencyMs: Int
)

object VpnServerProvider {
    val getServers = listOf(
        VpnServer("local", "Smart DNS Only (AdBlock)", "IL", "127.0.0.1", 1)
    )
}
