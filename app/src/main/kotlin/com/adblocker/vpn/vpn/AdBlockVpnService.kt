package com.adblocker.vpn.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.adblocker.vpn.MainActivity
import com.adblocker.vpn.R
import com.adblocker.vpn.data.datastore.SettingsDataStore
import android.system.OsConstants
import java.net.InetSocketAddress
import java.net.InetAddress
import android.net.ConnectivityManager
import android.net.TrafficStats
import com.adblocker.vpn.data.model.VpnEngineState
import com.adblocker.vpn.data.repository.ExcludedNetworkRepository
import com.adblocker.vpn.util.Constants
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * Foreground VpnService implementing a local DNS-filtering TUN interface.
 *
 * Design: we establish a minimal TUN that only routes DNS (port 53) traffic
 * through userspace (via a narrow route table entry pattern is not directly
 * expressible in VpnService, so instead we route all traffic through the TUN
 * and, for any non-DNS packet, forward it unmodified using a protected raw
 * socket pass-through; DNS packets are intercepted, filtered, and either
 * answered locally (blocked) or forwarded to an upstream resolver).
 *
 * The [NetworkMonitor] tracks the *underlying* physical network. When it
 * matches a user-defined excluded network, the engine flips into pass-through
 * mode: the TUN interface and foreground service keep running (no VPN icon
 * flicker, no reconnect), but every DNS query is forwarded upstream unfiltered.
 */
class AdBlockVpnService : VpnService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var vpnInterface: ParcelFileDescriptor? = null
    private var tunnelJob: Job? = null
    private var trafficMonitorJob: Job? = null

    private lateinit var blocklistManager: BlocklistManager
    private lateinit var dnsProxy: DnsProxy
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var excludedNetworkRepository: ExcludedNetworkRepository
    private var networkMonitor: NetworkMonitor? = null

    private val isPassThrough = AtomicBoolean(false)
    private val queriesTotal = AtomicLong(0)
    private val queriesBlocked = AtomicLong(0)
    private val queriesZeroDay = AtomicLong(0)
    private var currentUpstream = Constants.DEFAULT_UPSTREAM_PRIMARY
    private var activeNetworkLabel: String? = null
    private val useDoh = AtomicBoolean(true)
    private val enableZeroDayProtection = AtomicBoolean(true)
    private var bypassedApps = emptySet<String>()
    private var blockedInternetApps = emptySet<String>()
    private val aggressiveFirewall = AtomicBoolean(false)

    override fun onCreate() {
        super.onCreate()
        blocklistManager = BlocklistManager(cacheDir)
        dnsProxy = DnsProxy(this)
        settingsDataStore = SettingsDataStore(applicationContext)
        excludedNetworkRepository = ExcludedNetworkRepository(applicationContext)
        createNotificationChannel()

        serviceScope.launch {
            settingsDataStore.settingsFlow.collect { settings ->
                currentUpstream = settings.upstreamPrimary
                blocklistManager.setWhitelist(settings.whitelist)
                blocklistManager.setUserBlacklist(settings.blacklist)
                blocklistManager.setActiveLists(settings.activeBlocklists)
                useDoh.set(settings.useDoh)
                enableZeroDayProtection.set(settings.enableZeroDayProtection)
                queriesTotal.set(settings.queriesTotal)
                queriesBlocked.set(settings.queriesBlocked)
                queriesZeroDay.set(settings.queriesZeroDayBlocked)
                bypassedApps = settings.bypassedApps
                
                val previousBlocked = blockedInternetApps
                blockedInternetApps = settings.blockedInternetApps
                aggressiveFirewall.set(settings.aggressiveFirewall)
                
                // Aggressive Firewall: Restart VPN interface to force socket drop if blocked apps changed
                if (aggressiveFirewall.get() && previousBlocked != blockedInternetApps && vpnInterface != null) {
                    serviceScope.launch(Dispatchers.Main) {
                        restartVpnInterface()
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Constants.ACTION_STOP -> {
                stopVpn()
                return START_NOT_STICKY
            }
            else -> startVpn()
        }
        return START_STICKY
    }

    private fun startVpn() {
        if (vpnInterface != null) return // already running

        startForeground(Constants.NOTIFICATION_ID, buildNotification(active = true))

        val builder = Builder()
            .setSession("AdBlock VPN")
            .addAddress(Constants.VPN_ADDRESS, Constants.VPN_ADDRESS_PREFIX)
            .addRoute(Constants.VPN_DNS, 32)
            .addDnsServer(Constants.VPN_DNS)
            // Add IPv6 to prevent DNS leaks or drops
            .addAddress(Constants.VPN_ADDRESS_V6, 128)
            .addRoute(Constants.VPN_DNS_V6, 128)
            .addDnsServer(Constants.VPN_DNS_V6)
            .allowBypass() // Allow non-VPN traffic to pass through the normal network
            .setBlocking(true)
            .setMtu(1500)

        // Apply app bypass
        val finalBypassedApps = bypassedApps.toMutableSet().apply {
            addAll(Constants.DEFAULT_BYPASSED_APPS)
        }
        
        finalBypassedApps.forEach { packageName ->
            try {
                builder.addDisallowedApplication(packageName)
            } catch (e: Exception) {
                Log.w(TAG, "Cannot bypass app $packageName: ${e.message}")
            }
        }

        vpnInterface = try {
            builder.establish()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to establish VPN interface", e)
            null
        }

        if (vpnInterface == null) {
            stopSelf()
            return
        }

        _state.value = VpnEngineState(isRunning = true, isPassThrough = false)
        sendBroadcast(Intent("com.adblocker.vpn.ACTION_UPDATE_WIDGET").apply {
            setPackage(packageName)
        })

        networkMonitor = NetworkMonitor(applicationContext) { active ->
            serviceScope.launch { evaluatePassThrough(active) }
        }.also { it.start() }

        tunnelJob = serviceScope.launch { runTunnelLoop() }
        
        trafficMonitorJob = serviceScope.launch {
            var lastRx = TrafficStats.getTotalRxBytes()
            var lastTx = TrafficStats.getTotalTxBytes()
            while (isActive) {
                delay(1000)
                val currentRx = TrafficStats.getTotalRxBytes()
                val currentTx = TrafficStats.getTotalTxBytes()
                _state.value = _state.value.copy(
                    rxSpeed = maxOf(0L, currentRx - lastRx),
                    txSpeed = maxOf(0L, currentTx - lastTx)
                )
                lastRx = currentRx
                lastTx = currentTx
            }
        }
    }

    private fun stopVpn() {
        tunnelJob?.cancel()
        tunnelJob = null
        trafficMonitorJob?.cancel()
        trafficMonitorJob = null
        networkMonitor?.stop()
        networkMonitor = null
        vpnInterface?.let { runCatching { it.close() } }
        vpnInterface = null
        _state.value = VpnEngineState(isRunning = false, isPassThrough = false)
        sendBroadcast(Intent("com.adblocker.vpn.ACTION_UPDATE_WIDGET").apply {
            setPackage(packageName)
        })
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    private fun restartVpnInterface() {
        Log.i(TAG, "Aggressive Firewall: Restarting VPN Interface")
        tunnelJob?.cancel()
        tunnelJob = null
        vpnInterface?.let { runCatching { it.close() } }
        vpnInterface = null
        
        // Re-establish
        startVpn()
    }

    private suspend fun evaluatePassThrough(active: ActiveNetworkInfo?) {
        val excluded = excludedNetworkRepository.getEnabledSnapshot()
        val shouldBypass = NetworkMonitor.matches(active, excluded)
        isPassThrough.set(shouldBypass)
        activeNetworkLabel = active?.label
        _state.value = _state.value.copy(
            isPassThrough = shouldBypass,
            activeNetworkLabel = active?.label
        )
        updateNotification()
    }

    /** Main packet loop: read from TUN, classify DNS vs. other, respond/forward. */
    private suspend fun runTunnelLoop() = withContext(Dispatchers.IO) {
        val fd = vpnInterface ?: return@withContext
        val input = FileInputStream(fd.fileDescriptor)
        val output = FileOutputStream(fd.fileDescriptor)
        val buffer = ByteArray(32767)

        while (isActive) {
            val length = try {
                input.read(buffer)
            } catch (e: IOException) {
                if (isActive) Log.w(TAG, "TUN read error: ${e.message}")
                break
            }
            if (length <= 0) continue

            val parsed = DnsPacketParser.parseIpv4Udp(buffer, length)
            if (parsed == null || parsed.destPort != Constants.DNS_PORT) {
                // Non-DNS traffic: this simplified engine does not proxy generic
                // IP traffic (that requires a full user-space TCP/IP stack); it
                // is dropped here. In production this is where a NAT/session
                // table + raw-socket relay (e.g. via a native tun2socks core)
                // would forward the packet transparently.
                continue
            }

            // Launch each query independently so a slow upstream lookup never
            // blocks subsequent packets from being read off the TUN.
            launch { handleDnsPacket(buffer.copyOf(length), length, output) }
        }
    }

    private fun handleDnsPacket(packet: ByteArray, length: Int, output: FileOutputStream) {
        val parsed = DnsPacketParser.parseIpv4Udp(packet, length) ?: return
        val hostname = DnsPacketParser.extractQueryName(parsed.payload)

        queriesTotal.incrementAndGet()

        var blocked = false
        var isZeroDay = false
        var isWhitelisted = false
        
        var isAppFirewallBlocked = false
        if (!isPassThrough.get() && hostname != null) {
            // Check if domain is explicitly whitelisted FIRST
            isWhitelisted = blocklistManager.isWhitelisted(hostname)

            // App Firewall Killswitch Check
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && blockedInternetApps.isNotEmpty()) {
                try {
                    val srcAddr = InetAddress.getByAddress(parsed.sourceAddress)
                    val dstAddr = InetAddress.getByAddress(parsed.destAddress)
                    val cm = applicationContext.getSystemService(ConnectivityManager::class.java)
                    val uid = cm.getConnectionOwnerUid(
                        OsConstants.IPPROTO_UDP,
                        InetSocketAddress(srcAddr, parsed.sourcePort),
                        InetSocketAddress(dstAddr, parsed.destPort)
                    )
                    
                    if (uid > 0) {
                        val pm = applicationContext.packageManager
                        val packages = pm.getPackagesForUid(uid)
                        if (packages != null && packages.any { blockedInternetApps.contains(it) }) {
                            blocked = true
                            isAppFirewallBlocked = true
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to get connection owner UID: ${e.message}")
                }
            }

            if (!blocked && !isWhitelisted) {
                blocked = blocklistManager.isBlocked(hostname)
                if (!blocked && enableZeroDayProtection.get()) {
                    isZeroDay = ThreatHeuristics.isZeroDayThreat(hostname)
                    if (isZeroDay) blocked = true
                }
            }
        }

        if (blocked) {
            val reason = if (isZeroDay) "Procedurally Generated (Zero-Day)" else ""
            var isProfiling = false
            var isLocation = false
            if (hostname != null) {
                val lowerHost = hostname.lowercase()
                if (lowerHost.contains("google-analytics") || lowerHost.contains("facebook") || lowerHost.contains("appboy") || lowerHost.contains("mixpanel") || lowerHost.contains("scorecardresearch")) {
                    isProfiling = true
                } else if (lowerHost.contains("location") || lowerHost.contains("map") || lowerHost.contains("geotrust")) {
                    isLocation = true
                }
            }

            if (reason == "Procedurally Generated (Zero-Day)") {
                _state.update { it.copy(
                    queriesZeroDayBlocked = it.queriesZeroDayBlocked + 1,
                    queriesBlocked = it.queriesBlocked + 1,
                    profilingBlocked = it.profilingBlocked + (if(isProfiling) 1 else 0),
                    locationBlocked = it.locationBlocked + (if(isLocation) 1 else 0)
                ) }
                queriesBlocked.incrementAndGet()
                queriesZeroDay.incrementAndGet()
                showThreatNotification("Zero-Day Threat Blocked", "Blocked: $hostname\nReason: Detected as procedurally generated malware domain (Zero-Day).")
            } else if (isAppFirewallBlocked) {
                showThreatNotification("App Firewall Blocked", "Blocked: $hostname\nReason: The app trying to access this site is in your App Firewall (Killswitch) list.")
                _state.update { it.copy(
                    queriesBlocked = it.queriesBlocked + 1,
                    profilingBlocked = it.profilingBlocked + (if(isProfiling) 1 else 0),
                    locationBlocked = it.locationBlocked + (if(isLocation) 1 else 0)
                ) }
                queriesBlocked.incrementAndGet()
            } else if (!isWhitelisted && hostname != null) {
                // Do not show notification for standard ads/trackers
                _state.update { it.copy(
                    queriesBlocked = it.queriesBlocked + 1,
                    profilingBlocked = it.profilingBlocked + (if(isProfiling) 1 else 0),
                    locationBlocked = it.locationBlocked + (if(isLocation) 1 else 0)
                ) }
                queriesBlocked.incrementAndGet()
            } else {
                _state.update { it.copy(
                    queriesBlocked = it.queriesBlocked + 1,
                    profilingBlocked = it.profilingBlocked + (if(isProfiling) 1 else 0),
                    locationBlocked = it.locationBlocked + (if(isLocation) 1 else 0)
                ) }
                queriesBlocked.incrementAndGet()
            }
            
            DnsPacketParser.buildBlockedResponse(packet, length)?.let { response ->
                writeToTun(output, response)
            }
        } else {
            val upstreamResponse = dnsProxy.forward(parsed.payload, currentUpstream, useDoh = useDoh.get())
            if (upstreamResponse != null) {
                // DNS Rebinding Protection — skip for whitelisted domains
                if (!isWhitelisted && DnsPacketParser.containsPrivateIp(upstreamResponse)) {
                    Log.w(TAG, "DNS Rebinding attack prevented for $hostname")
                    queriesBlocked.incrementAndGet()
                    blocked = true
                    DnsPacketParser.buildBlockedResponse(packet, length)?.let { response ->
                        writeToTun(output, response)
                    }
                } else {
                    val ipPacket = DnsPacketParser.buildUdpIpv4Packet(
                        srcAddr = parsed.destAddress,
                        dstAddr = parsed.sourceAddress,
                        srcPort = parsed.destPort,
                        dstPort = parsed.sourcePort,
                        payload = upstreamResponse
                    )
                    writeToTun(output, ipPacket)
                }
            }
        }

        _state.value = _state.value.copy(
            queriesTotal = queriesTotal.get(),
            queriesBlocked = queriesBlocked.get(),
            queriesZeroDayBlocked = queriesZeroDay.get()
        )
        serviceScope.launch {
            settingsDataStore.recordQueryStats(queriesTotal.get(), queriesBlocked.get(), queriesZeroDay.get())
        }
        
        if (hostname != null) {
            val log = com.adblocker.vpn.data.model.DnsLog(System.currentTimeMillis(), hostname, blocked)
            _dnsLogs.tryEmit(log)
        }
    }

    @Synchronized
    private fun writeToTun(output: FileOutputStream, packet: ByteArray) {
        try {
            output.write(packet)
        } catch (e: IOException) {
            Log.w(TAG, "TUN write error: ${e.message}")
        }
    }

    private fun buildNotification(active: Boolean): Notification {
        val statusText = when {
            !active -> getString(R.string.notif_disconnected)
            isPassThrough.get() -> getString(R.string.notif_paused)
            else -> getString(R.string.notif_active)
        }
        val contentIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getService(
            this, 0,
            Intent(this, AdBlockVpnService::class.java).setAction(Constants.ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(statusText)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(contentIntent)
            .addAction(0, "Stop", stopIntent)
            .build()
    }

    private fun updateNotification() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(Constants.NOTIFICATION_ID, buildNotification(active = true))
    }

    private var lastNotificationTime = 0L

    private fun showThreatNotification(title: String, message: String) {
        val now = System.currentTimeMillis()
        if (now - lastNotificationTime < 60000) return // Debounce: Max 1 every 60s
        lastNotificationTime = now

        val nm = getSystemService(NotificationManager::class.java)
        
        // Extract domain and reason from the message string (format: "Blocked: domain.com\nReason: ...")
        val lines = message.split("\n")
        val domain = if (lines.isNotEmpty()) lines[0].removePrefix("Blocked: ") else "Unknown"
        val reason = if (lines.size > 1) lines[1].removePrefix("Reason: ") else "Unknown"
        val encodedReason = java.net.URLEncoder.encode(reason, "UTF-8")
        
        val intent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = android.net.Uri.parse("adblockvpn://blocked/$domain/$encodedReason")
        }

        val contentIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, Constants.THREAT_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Pops up
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        nm.notify((System.currentTimeMillis() % 10000).toInt(), notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            
            // Foreground Service Channel (Must NOT be HIGH importance to avoid constant popups)
            val fgChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_LOW 
            )
            nm.createNotificationChannel(fgChannel)
            
            // Threats Channel (HIGH importance to allow popups)
            val threatChannel = NotificationChannel(
                Constants.THREAT_CHANNEL_ID,
                "Threat Alerts",
                NotificationManager.IMPORTANCE_HIGH 
            )
            nm.createNotificationChannel(threatChannel)
        }
    }

    override fun onRevoke() {
        stopVpn()
        super.onRevoke()
    }

    override fun onDestroy() {
        stopVpn()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "AdBlockVpnService"

        private val _state = MutableStateFlow(VpnEngineState())
        val state: StateFlow<VpnEngineState> = _state.asStateFlow()
        
        private val _dnsLogs = MutableSharedFlow<com.adblocker.vpn.data.model.DnsLog>(
            replay = 100,
            extraBufferCapacity = 50
        )
        val dnsLogs: SharedFlow<com.adblocker.vpn.data.model.DnsLog> = _dnsLogs.asSharedFlow()
    }
}
