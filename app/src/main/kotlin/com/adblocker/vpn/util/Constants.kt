package com.adblocker.vpn.util

object Constants {
    const val VPN_ADDRESS = "10.111.222.1"
    const val VPN_ADDRESS_PREFIX = 32
    const val VPN_DNS = "10.111.222.2"
    const val DNS_PORT = 53
    
    // DNS Options
    const val DEFAULT_UPSTREAM_PRIMARY = "1.1.1.1" // Cloudflare
    const val DEFAULT_UPSTREAM_SECONDARY = "8.8.8.8" // Google
    
    // Blocklists (Constantly updated lists from the internet)
    val BLOCKLISTS = mapOf(
        "AdGuard DNS Filter (Comprehensive)" to "https://adguardteam.github.io/HostlistsRegistry/assets/filter_15.txt",
        "AdGuard Mobile Ads (In-App)" to "https://adguardteam.github.io/HostlistsRegistry/assets/filter_11.txt",
        "AdAway (Mobile/In-App Ads)" to "https://adaway.org/hosts.txt",
        "HaGeZi's Mobile Tracker Blocker" to "https://raw.githubusercontent.com/hagezi/dns-blocklists/main/hosts/pro.txt",
        "HaGeZi's Ultimate (Maximum Protection)" to "https://raw.githubusercontent.com/hagezi/dns-blocklists/main/hosts/ultimate.txt",
        "HaGeZi's Threat Intelligence (Malware)" to "https://raw.githubusercontent.com/hagezi/dns-blocklists/main/hosts/tif.txt",
        "OISD Basic (Safe & Fast)" to "https://small.oisd.nl/domainswild",
        "OISD Big (Comprehensive)" to "https://big.oisd.nl/domainswild",
        "StevenBlack (Ad/Malware/Fakenews)" to "https://raw.githubusercontent.com/StevenBlack/hosts/master/hosts",
        "1Hosts (Pro)" to "https://o0.pages.dev/Pro/hosts.txt",
        "Dan Pollock's (Ads/Trackers)" to "https://someonewhocares.org/hosts/hosts",
        "URLHaus (Malware/Ransomware)" to "https://urlhaus.abuse.ch/downloads/hostfile/",
        "Phishing Army (Anti-Phishing)" to "https://phishing.army/download/phishing_army_blocklist_extended.txt",
        "Bypass Paywalls (Clean)" to "https://raw.githubusercontent.com/bpc-clone/bypass-paywalls-clean-filters/main/bpc-paywall-filter.txt",
        
        // Niche & Specialized Blocklists (Deep Web Scanning)
        "Smart TV Telemetry & Ads" to "https://raw.githubusercontent.com/Perflyst/PiHoleBlocklist/master/SmartTV.txt",
        "Samsung Smart TV Spyware" to "https://raw.githubusercontent.com/mboutolleau/block-samsung-tv-telemetry/refs/heads/master/samsung_tv_telemetry_urls.txt",
        "Amazon Fire TV Trackers" to "https://raw.githubusercontent.com/Perflyst/PiHoleBlocklist/master/AmazonFireTV.txt",
        "Crypto-Jacking Miners (Prigent)" to "https://v.firebog.net/hosts/Prigent-Crypto.txt",
        "NoCoin Anti-Miner" to "https://raw.githubusercontent.com/hagezi/dns-blocklists/main/adblock/tif.txt"
    )
    
    const val NOTIFICATION_CHANNEL_ID = "vpn_status_channel"
    const val NOTIFICATION_ID = 4201
    const val ACTION_START = "com.adblocker.vpn.action.START"
    const val ACTION_STOP = "com.adblocker.vpn.action.STOP"
    const val PREFS_NAME = "adblocker_settings"
}
