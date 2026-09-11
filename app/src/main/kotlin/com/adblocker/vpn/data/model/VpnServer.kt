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
        VpnServer("us_east", "United States (New York)", "US", "104.28.10.10", 35),
        VpnServer("us_west", "United States (Los Angeles)", "US", "104.28.11.10", 45),
        VpnServer("us_central", "United States (Chicago)", "US", "104.28.12.10", 40),
        VpnServer("ca_east", "Canada (Toronto)", "CA", "104.28.13.10", 38),
        VpnServer("ca_west", "Canada (Vancouver)", "CA", "104.28.14.10", 48),
        VpnServer("mx", "Mexico (Mexico City)", "MX", "104.28.15.10", 55),
        VpnServer("uk", "United Kingdom (London)", "GB", "104.28.16.10", 80),
        VpnServer("de", "Germany (Frankfurt)", "DE", "104.28.17.10", 85),
        VpnServer("fr", "France (Paris)", "FR", "104.28.18.10", 82),
        VpnServer("nl", "Netherlands (Amsterdam)", "NL", "104.28.19.10", 81),
        VpnServer("it", "Italy (Milan)", "IT", "104.28.20.10", 90),
        VpnServer("es", "Spain (Madrid)", "ES", "104.28.21.10", 95),
        VpnServer("ch", "Switzerland (Zurich)", "CH", "104.28.22.10", 88),
        VpnServer("se", "Sweden (Stockholm)", "SE", "104.28.23.10", 87),
        VpnServer("no", "Norway (Oslo)", "NO", "104.28.24.10", 89),
        VpnServer("dk", "Denmark (Copenhagen)", "DK", "104.28.25.10", 86),
        VpnServer("ru", "Russia (Moscow)", "RU", "104.28.26.10", 110),
        VpnServer("tr", "Turkey (Istanbul)", "TR", "104.28.27.10", 100),
        VpnServer("ua", "Ukraine (Kyiv)", "UA", "104.28.28.10", 105),
        VpnServer("pl", "Poland (Warsaw)", "PL", "104.28.29.10", 92),
        VpnServer("ro", "Romania (Bucharest)", "RO", "104.28.30.10", 98),
        VpnServer("il", "Israel (Tel Aviv)", "IL", "104.28.31.10", 12),
        VpnServer("ae", "UAE (Dubai)", "AE", "104.28.32.10", 60),
        VpnServer("sa", "Saudi Arabia (Riyadh)", "SA", "104.28.33.10", 70),
        VpnServer("za", "South Africa (Johannesburg)", "ZA", "104.28.34.10", 150),
        VpnServer("eg", "Egypt (Cairo)", "EG", "104.28.35.10", 90),
        VpnServer("in", "India (Mumbai)", "IN", "104.28.36.10", 130),
        VpnServer("jp", "Japan (Tokyo)", "JP", "104.28.37.10", 180),
        VpnServer("kr", "South Korea (Seoul)", "KR", "104.28.38.10", 175),
        VpnServer("sg", "Singapore", "SG", "104.28.39.10", 160),
        VpnServer("hk", "Hong Kong", "HK", "104.28.40.10", 165),
        VpnServer("tw", "Taiwan (Taipei)", "TW", "104.28.41.10", 170),
        VpnServer("au", "Australia (Sydney)", "AU", "104.28.42.10", 210),
        VpnServer("nz", "New Zealand (Auckland)", "NZ", "104.28.43.10", 220),
        VpnServer("br", "Brazil (Sao Paulo)", "BR", "104.28.44.10", 140),
        VpnServer("ar", "Argentina (Buenos Aires)", "AR", "104.28.45.10", 155),
        VpnServer("cl", "Chile (Santiago)", "CL", "104.28.46.10", 160),
        VpnServer("co", "Colombia (Bogota)", "CO", "104.28.47.10", 145),
        VpnServer("pe", "Peru (Lima)", "PE", "104.28.48.10", 150)
    )
}
