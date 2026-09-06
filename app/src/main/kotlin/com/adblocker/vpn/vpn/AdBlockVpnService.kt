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
import com.adblocker.vpn.data.model.VpnEngineState
import com.adblocker.vpn.data.repository.ExcludedNetworkRepository
import com.adblocker.vpn.util.Constants
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        bypassedApps.forEach { packageName ->
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
    }

    private fun stopVpn() {
        tunnelJob?.cancel()
        tunnelJob = null
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
        
        if (!isPassThrough.get() && hostname != null) {
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
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to get connection owner UID: ${e.message}")
                }
            }

            if (!blocked) {
                blocked = blocklistManager.isBlocked(hostname)
                if (!blocked && enableZeroDayProtection.get()) {
                    isZeroDay = ThreatHeuristics.isZeroDayThreat(hostname)
                    if (isZeroDay) blocked = true
                }
            }
        }

        if (blocked) {
            queriesBlocked.incrementAndGet()
            if (isZeroDay) queriesZeroDay.incrementAndGet()
            
            DnsPacketParser.buildBlockedResponse(packet, length)?.let { response ->
                writeToTun(output, response)
            }
        } else {
            val upstreamResponse = dnsProxy.forward(parsed.payload, currentUpstream, useDoh = useDoh.get())
            if (upstreamResponse != null) {
                // DNS Rebinding Protection
                if (DnsPacketParser.containsPrivateIp(upstreamResponse)) {
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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
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
