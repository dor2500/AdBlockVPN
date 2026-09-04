package com.adblocker.vpn.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.core.content.ContextCompat
import com.adblocker.vpn.data.datastore.SettingsDataStore
import com.adblocker.vpn.vpn.AdBlockVpnService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = SettingsDataStore(context.applicationContext).settingsFlow.first()
                // VpnService.prepare() returning null means the user already granted
                // consent previously; only then can we auto-start without UI.
                val needsConsent = VpnService.prepare(context) != null
                if (settings.autoStartOnBoot && !needsConsent) {
                    val serviceIntent = Intent(context, AdBlockVpnService::class.java)
                    ContextCompat.startForegroundService(context, serviceIntent)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
