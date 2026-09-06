package com.adblocker.vpn.service

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.adblocker.vpn.vpn.AdBlockVpnService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class VpnTileService : TileService() {
    
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val isRunning = AdBlockVpnService.state.value.isRunning
        
        if (isRunning) {
            val intent = Intent(this, AdBlockVpnService::class.java).apply {
                action = com.adblocker.vpn.util.Constants.ACTION_STOP
            }
            startService(intent)
        } else {
            // Can't start VPN directly from TileService if it needs consent,
            // but if already consented, we can start it. We'll send a connect intent.
            val intent = Intent(this, AdBlockVpnService::class.java).apply {
                action = com.adblocker.vpn.util.Constants.ACTION_START
            }
            try {
                startService(intent)
            } catch (e: Exception) {
                // Ignore if we can't start from background
            }
        }
        
        // Update state optimistically
        val tile = qsTile
        if (tile != null) {
            tile.state = if (isRunning) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
            tile.updateTile()
        }
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val isRunning = AdBlockVpnService.state.value.isRunning
        tile.state = if (isRunning) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
