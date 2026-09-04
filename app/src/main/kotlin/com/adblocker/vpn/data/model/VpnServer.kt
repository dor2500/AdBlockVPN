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
        VpnServer("local", "Smart DNS Only (AdBlock)", "IL", "127.0.0.1", 1),
        VpnServer("il_tlv", "Tel Aviv, Israel", "IL", "104.28.16.20", 12),
        VpnServer("us_ny", "New York, USA", "US", "104.28.14.25", 85),
        VpnServer("us_la", "Los Angeles, USA", "US", "104.28.18.25", 140),
        VpnServer("uk_lon", "London, UK", "GB", "104.28.21.30", 65),
        VpnServer("de_fra", "Frankfurt, Germany", "DE", "104.28.28.10", 55),
        VpnServer("nl_ams", "Amsterdam, Netherlands", "NL", "104.28.31.10", 60),
        VpnServer("fr_par", "Paris, France", "FR", "104.28.32.10", 62),
        VpnServer("jp_tok", "Tokyo, Japan", "JP", "104.28.24.40", 210),
        VpnServer("sg_sin", "Singapore", "SG", "104.28.25.40", 185),
        VpnServer("au_syd", "Sydney, Australia", "AU", "104.28.26.40", 250),
        VpnServer("ca_tor", "Toronto, Canada", "CA", "104.28.27.40", 110),
        VpnServer("br_sp", "São Paulo, Brazil", "BR", "104.28.30.40", 195)
    )
}
