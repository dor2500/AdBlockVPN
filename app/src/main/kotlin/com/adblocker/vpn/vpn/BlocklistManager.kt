package com.adblocker.vpn.vpn

import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BlocklistManager(private val cacheDir: File) {

    @Volatile private var blockedDomains: Set<String> = BUNDLED_DEFAULT_BLOCKLIST
    private val whitelist = ConcurrentHashMap.newKeySet<String>()
    private val userBlacklist = ConcurrentHashMap.newKeySet<String>()

    private var activeUrls = emptySet<String>()

    fun setWhitelist(domains: Set<String>) {
        whitelist.clear()
        whitelist.addAll(domains.map { it.lowercase() })
        synchronized(domainResultCache) { domainResultCache.clear() }
    }

    fun setUserBlacklist(domains: Set<String>) {
        userBlacklist.clear()
        userBlacklist.addAll(domains.map { it.lowercase() })
        synchronized(domainResultCache) { domainResultCache.clear() }
    }

    suspend fun setActiveLists(urls: Set<String>) {
        if (urls == activeUrls) return
        activeUrls = urls
        loadActiveListsFromDisk()
        synchronized(domainResultCache) { domainResultCache.clear() }
    }

    private suspend fun loadActiveListsFromDisk() = withContext(Dispatchers.IO) {
        val newSet = HashSet<String>()
        if (activeUrls.isEmpty()) {
            newSet.addAll(BUNDLED_DEFAULT_BLOCKLIST)
        } else {
            for (url in activeUrls) {
                val cacheFile = getCacheFileForUrl(url)
                if (cacheFile.exists()) {
                    newSet.addAll(readHostsFile(cacheFile.bufferedReader()))
                }
            }
            if (newSet.isEmpty()) {
                newSet.addAll(BUNDLED_DEFAULT_BLOCKLIST)
            }
        }
        blockedDomains = newSet
        Log.i(TAG, "Loaded ${blockedDomains.size} blocked domains from ${activeUrls.size} lists")
    }

    /**
     * Check if a domain is explicitly whitelisted (user whitelist or DEFAULT_WHITELIST).
     * Walks up the domain tree so whitelisting "example.com" covers "sub.example.com".
     */
    fun isWhitelisted(host: String): Boolean {
        val normalized = host.lowercase().removeSuffix(".")
        
        // Also walk UP the domain tree: if the user whitelisted "sub.example.com",
        // make sure we also catch it when checking "sub.example.com" against "example.com" entries
        var domain = normalized
        while (true) {
            if (whitelist.contains(domain)) return true
            if (com.adblocker.vpn.util.Constants.DEFAULT_WHITELIST.contains(domain)) return true
            val dot = domain.indexOf('.')
            if (dot < 0) break
            domain = domain.substring(dot + 1)
        }
        return false
    }

    private val domainResultCache = object : java.util.LinkedHashMap<String, Boolean>(500, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Boolean>?): Boolean = size > 2000
    }

    fun isBlocked(host: String): Boolean {
        val normalized = host.lowercase().removeSuffix(".")
        
        synchronized(domainResultCache) {
            domainResultCache[normalized]?.let { return it }
        }

        // Check whitelist first (with full subdomain + parent matching)
        if (isWhitelisted(normalized)) {
            synchronized(domainResultCache) { domainResultCache[normalized] = false }
            return false
        }
        if (userBlacklist.contains(normalized)) {
            synchronized(domainResultCache) { domainResultCache[normalized] = true }
            return true
        }

        var domain = normalized
        while (true) {
            if (blockedDomains.contains(domain)) {
                synchronized(domainResultCache) { domainResultCache[normalized] = true }
                return true
            }
            val dot = domain.indexOf('.')
            if (dot < 0) break
            domain = domain.substring(dot + 1)
        }
        
        synchronized(domainResultCache) { domainResultCache[normalized] = false }
        return false
    }

    suspend fun updateAll(): Result<Int> = runCatching {
        var totalLoaded = 0
        withContext(Dispatchers.IO) {
            for (url in activeUrls) {
                try {
                    val cacheFile = getCacheFileForUrl(url)
                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.connectTimeout = 15_000
                    connection.readTimeout = 30_000
                    connection.instanceFollowRedirects = true
                    connection.inputStream.use { input ->
                        cacheFile.outputStream().use { output -> input.copyTo(output) }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to download blocklist: $url", e)
                }
            }
            loadActiveListsFromDisk()
            totalLoaded = blockedDomains.size
        }
        totalLoaded
    }

    private fun getCacheFileForUrl(url: String): File {
        return File(cacheDir, "blocklist_${url.hashCode()}.txt")
    }

    private fun readHostsFile(reader: BufferedReader): Set<String> {
        val set = HashSet<String>()
        reader.use { r ->
            r.forEachLine { rawLine ->
                val line = rawLine.substringBefore('#').trim()
                if (line.isEmpty()) return@forEachLine
                val parts = line.split(Regex("\\s+"))
                val domain = if (parts.size >= 2) parts[1] else parts[0]
                if (domain.isNotBlank() && domain != "0.0.0.0" && domain != "localhost") {
                    set.add(domain.lowercase())
                }
            }
        }
        return set
    }

    companion object {
        private const val TAG = "BlocklistManager"

        val BUNDLED_DEFAULT_BLOCKLIST: Set<String> = setOf(
            "doubleclick.net", "googlesyndication.com", "googleadservices.com",
            "adservice.google.com", "ads.yahoo.com", "advertising.com",
            "adnxs.com", "taboola.com", "outbrain.com", "scorecardresearch.com",
            "quantserve.com", "moatads.com", "mopub.com", "adcolony.com",
            "chartboost.com", "vungle.com", "unityads.unity3d.com", "applovin.com",
            "amazon-adsystem.com", "criteo.com", "pubmatic.com", "rubiconproject.com",
            "openx.net", "smartadserver.com", "adform.net", "media.net"
        )
    }
}
