package com.adblocker.vpn.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.adblocker.vpn.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = Constants.PREFS_NAME)

data class AppSettings(
    val activeBlocklists: Set<String> = setOf(Constants.BLOCKLISTS.keys.first()),
    val upstreamPrimary: String = Constants.DEFAULT_UPSTREAM_PRIMARY,
    val upstreamSecondary: String = Constants.DEFAULT_UPSTREAM_SECONDARY,
    val whitelist: Set<String> = emptySet(),
    val blacklist: Set<String> = emptySet(),
    val autoStartOnBoot: Boolean = true,
    val useDoh: Boolean = false,
    val enableZeroDayProtection: Boolean = true,
    val queriesTotal: Long = 0,
    val queriesBlocked: Long = 0,
    val queriesZeroDayBlocked: Long = 0,
    val selectedVpnLocation: String = "local", // "local" means just DNS, other IDs mean full VPN
    val bypassedApps: Set<String> = emptySet(),
    val blockedInternetApps: Set<String> = emptySet(),
    val aggressiveFirewall: Boolean = false,
    val selectedTheme: String = "system"
)

class SettingsDataStore(private val context: Context) {

    private object Keys {
        val ACTIVE_BLOCKLISTS = stringSetPreferencesKey("active_blocklists")
        val UPSTREAM_PRIMARY = stringPreferencesKey("upstream_primary")
        val UPSTREAM_SECONDARY = stringPreferencesKey("upstream_secondary")
        val WHITELIST = stringSetPreferencesKey("whitelist")
        val BLACKLIST = stringSetPreferencesKey("blacklist")
        val AUTO_START = booleanPreferencesKey("auto_start_on_boot")
        val USE_DOH = booleanPreferencesKey("use_doh")
        val ENABLE_ZERO_DAY = booleanPreferencesKey("enable_zero_day")
        val QUERIES_TOTAL = longPreferencesKey("queries_total")
        val QUERIES_BLOCKED = longPreferencesKey("queries_blocked")
        val QUERIES_ZERO_DAY = longPreferencesKey("queries_zero_day")
        val SELECTED_VPN_LOCATION = stringPreferencesKey("selected_vpn_location")
        val BYPASSED_APPS = stringSetPreferencesKey("bypassed_apps")
        val BLOCKED_INTERNET_APPS = stringSetPreferencesKey("blocked_internet_apps")
        val AGGRESSIVE_FIREWALL = booleanPreferencesKey("aggressive_firewall")
        val SELECTED_THEME = stringPreferencesKey("selected_theme")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            activeBlocklists = prefs[Keys.ACTIVE_BLOCKLISTS] ?: setOf(Constants.BLOCKLISTS.keys.first()),
            upstreamPrimary = prefs[Keys.UPSTREAM_PRIMARY] ?: Constants.DEFAULT_UPSTREAM_PRIMARY,
            upstreamSecondary = prefs[Keys.UPSTREAM_SECONDARY] ?: Constants.DEFAULT_UPSTREAM_SECONDARY,
            whitelist = prefs[Keys.WHITELIST] ?: emptySet(),
            blacklist = prefs[Keys.BLACKLIST] ?: emptySet(),
            autoStartOnBoot = prefs[Keys.AUTO_START] ?: true,
            useDoh = prefs[Keys.USE_DOH] ?: false,
            enableZeroDayProtection = prefs[Keys.ENABLE_ZERO_DAY] ?: true,
            queriesTotal = prefs[Keys.QUERIES_TOTAL] ?: 0,
            queriesBlocked = prefs[Keys.QUERIES_BLOCKED] ?: 0,
            queriesZeroDayBlocked = prefs[Keys.QUERIES_ZERO_DAY] ?: 0,
            selectedVpnLocation = prefs[Keys.SELECTED_VPN_LOCATION] ?: "local",
            bypassedApps = prefs[Keys.BYPASSED_APPS] ?: emptySet(),
            blockedInternetApps = prefs[Keys.BLOCKED_INTERNET_APPS] ?: emptySet(),
            aggressiveFirewall = prefs[Keys.AGGRESSIVE_FIREWALL] ?: false,
            selectedTheme = prefs[Keys.SELECTED_THEME] ?: "system"
        )
    }

    suspend fun setActiveBlocklists(lists: Set<String>) {
        context.dataStore.edit { it[Keys.ACTIVE_BLOCKLISTS] = lists }
    }

    suspend fun setUpstream(primary: String, secondary: String) {
        context.dataStore.edit {
            it[Keys.UPSTREAM_PRIMARY] = primary
            it[Keys.UPSTREAM_SECONDARY] = secondary
        }
    }

    suspend fun addToWhitelist(domain: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.WHITELIST] ?: emptySet()
            prefs[Keys.WHITELIST] = current + domain.lowercase().trim()
        }
    }

    suspend fun removeFromWhitelist(domain: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.WHITELIST] ?: emptySet()
            prefs[Keys.WHITELIST] = current - domain.lowercase().trim()
        }
    }

    suspend fun addToBlacklist(domain: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.BLACKLIST] ?: emptySet()
            prefs[Keys.BLACKLIST] = current + domain.lowercase().trim()
        }
    }

    suspend fun removeFromBlacklist(domain: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.BLACKLIST] ?: emptySet()
            prefs[Keys.BLACKLIST] = current - domain.lowercase().trim()
        }
    }

    suspend fun setAutoStartOnBoot(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_START] = enabled }
    }

    suspend fun setUseDoh(enabled: Boolean) {
        context.dataStore.edit { it[Keys.USE_DOH] = enabled }
    }

    suspend fun setEnableZeroDayProtection(enabled: Boolean) {
        context.dataStore.edit { it[Keys.ENABLE_ZERO_DAY] = enabled }
    }

    suspend fun setVpnLocation(locationId: String) {
        context.dataStore.edit { it[Keys.SELECTED_VPN_LOCATION] = locationId }
    }

    suspend fun setBypassedApps(packages: Set<String>) {
        context.dataStore.edit { it[Keys.BYPASSED_APPS] = packages }
    }

    suspend fun setBlockedInternetApps(packages: Set<String>) {
        context.dataStore.edit { it[Keys.BLOCKED_INTERNET_APPS] = packages }
    }

    suspend fun setAggressiveFirewall(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AGGRESSIVE_FIREWALL] = enabled }
    }

    suspend fun setTheme(themeId: String) {
        context.dataStore.edit { it[Keys.SELECTED_THEME] = themeId }
    }

    suspend fun recordQueryStats(total: Long, blocked: Long, zeroDay: Long = 0) {
        context.dataStore.edit {
            it[Keys.QUERIES_TOTAL] = total
            it[Keys.QUERIES_BLOCKED] = blocked
            it[Keys.QUERIES_ZERO_DAY] = zeroDay
        }
    }

    suspend fun resetStats() {
        context.dataStore.edit {
            it[Keys.QUERIES_TOTAL] = 0
            it[Keys.QUERIES_BLOCKED] = 0
            it[Keys.QUERIES_ZERO_DAY] = 0
        }
    }
}
