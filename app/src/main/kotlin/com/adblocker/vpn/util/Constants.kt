package com.adblocker.vpn.util

object Constants {
    const val VPN_ADDRESS = "10.111.222.1"
    const val VPN_ADDRESS_PREFIX = 32
    const val VPN_DNS = "10.111.222.2"
    const val VPN_ADDRESS_V6 = "fd00:1:fd00:1:fd00:1:fd00:1"
    const val VPN_DNS_V6 = "fd00:1:fd00:1:fd00:1:fd00:2"
    const val DNS_PORT = 53
    
    // DNS Options
    const val DEFAULT_UPSTREAM_PRIMARY = "1.1.1.1" // Cloudflare
    const val DEFAULT_UPSTREAM_SECONDARY = "8.8.8.8" // Google
    
    // Blocklists (Constantly updated lists from the internet)
    val BLOCKLISTS = mapOf(
        // Mobile & In-App Ads (Highly Recommended for Android)
        "AdGuard Mobile Ads (In-App)" to "https://adguardteam.github.io/HostlistsRegistry/assets/filter_11.txt",
        "AdAway (Mobile/In-App Ads)" to "https://adaway.org/hosts.txt",
        "HaGeZi's Mobile Tracker Blocker" to "https://raw.githubusercontent.com/hagezi/dns-blocklists/main/hosts/pro.txt",
        "OISD Mobile & App Trackers" to "https://small.oisd.nl/domainswild",
        
        // Anti-Telemetry & Privacy
        "Android Telemetry Blocklist" to "https://raw.githubusercontent.com/hagezi/dns-blocklists/main/hosts/native.winoffice.txt", // Using a solid native tracker blocker
        "StevenBlack (Ad/Malware/Fakenews)" to "https://raw.githubusercontent.com/StevenBlack/hosts/master/hosts",
        
        // Comprehensive Security
        "HaGeZi's Threat Intelligence (Malware)" to "https://raw.githubusercontent.com/hagezi/dns-blocklists/main/hosts/tif.txt",
        "Phishing Army (Anti-Phishing)" to "https://phishing.army/download/phishing_army_blocklist_extended.txt",
        "URLHaus (Malware/Ransomware)" to "https://urlhaus.abuse.ch/downloads/hostfile/",
        
        // Crypto & Scams
        "Crypto-Jacking Miners (Prigent)" to "https://v.firebog.net/hosts/Prigent-Crypto.txt"
    )
    
    // Hardcoded Essential Whitelist (These will never be blocked, regardless of lists)
    val DEFAULT_WHITELIST = setOf(
        "google.com",
        "googleapis.com",
        "gstatic.com",
        "gemini.google.com",
        "generativelanguage.googleapis.com",
        "ai.google.dev",
        "apple.com",
        "icloud.com",
        "whatsapp.com",
        "whatsapp.net",
        "cdn.whatsapp.net",
        "wa.me",
        "fbcdn.net",
        "fbsbx.com",
        "mmg.whatsapp.net",
        "pps.whatsapp.net",
        "media.whatsapp.net",
        "microsoft.com",
        "windowsupdate.com",
        "office.com",
        "youtube.com",
        "googlevideo.com",
        "ytimg.com",
        "youtube-nocookie.com",
        "tiktokcdn.com",
        "tiktokv.com",
        "byteoversea.com",
        "netflix.com",
        "spotify.com",
        "amazon.com",
        "aws.amazon.com",
        "facebook.com",
        "instagram.com",
        "paypal.com",
        "github.com",
        "githubusercontent.com",
        "android.com",
        "aliexpress.com",
        "alicdn.com",
        "alibaba.com",
        "aliexpress.ru",
        "wolt.com",
        "wolt.net",
        "woltapi.com"
    )
    
    const val NOTIFICATION_CHANNEL_ID = "vpn_status_channel"
    const val NOTIFICATION_ID = 4201
    const val ACTION_START = "com.adblocker.vpn.action.START"
    const val ACTION_STOP = "com.adblocker.vpn.action.STOP"
    const val PREFS_NAME = "adblocker_settings"
}
