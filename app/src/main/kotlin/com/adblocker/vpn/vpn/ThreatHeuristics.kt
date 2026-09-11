package com.adblocker.vpn.vpn

import kotlin.math.log2

object ThreatHeuristics {

    /**
     * Calculates the Shannon Entropy of a string.
     * High entropy indicates randomness (like DGA malware domains).
     */
    fun calculateEntropy(s: String): Double {
        if (s.isEmpty()) return 0.0
        val frequencies = s.groupingBy { it }.eachCount()
        val length = s.length.toDouble()
        return frequencies.values.sumOf { count ->
            val p = count / length
            -p * log2(p)
        }
    }

    private val cache = object : java.util.LinkedHashMap<String, Boolean>(1000, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Boolean>?): Boolean {
            return size > 1000
        }
    }

    /**
     * Checks if a domain is a potential Zero-Day threat.
     * Removes the TLD and calculates entropy of the main domain part.
     * Uses LRU cache for performance.
     */
    fun isZeroDayThreat(domain: String): Boolean {
        synchronized(cache) {
            cache[domain]?.let { return it }
        }

        // Strip common TLDs to avoid them artificially lowering entropy
        val parts = domain.split(".")
        if (parts.size < 2) return false
        val mainPart = parts.dropLast(1).joinToString("")
        
        // Short domains don't have enough data for reliable entropy
        if (mainPart.length < 10) return false
        
        val entropy = calculateEntropy(mainPart)
        
        // Threshold: 3.8 is quite random for a typical English dictionary domain
        val isThreat = entropy > 3.8
        
        synchronized(cache) {
            cache[domain] = isThreat
        }
        
        return isThreat
    }
}
