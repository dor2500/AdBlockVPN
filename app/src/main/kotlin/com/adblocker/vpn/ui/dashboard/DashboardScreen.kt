package com.adblocker.vpn.ui.dashboard

import android.content.Intent
import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adblocker.vpn.util.Constants
import com.adblocker.vpn.vpn.AdBlockVpnService
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.engineState.collectAsState()
    val view = LocalView.current

    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            startVpnService(context)
        }
    }

    fun requestStart() {
        val intent = VpnService.prepare(context)
        if (intent != null) {
            vpnPermissionLauncher.launch(intent)
        } else {
            startVpnService(context)
        }
    }

    val isRunning = state.isRunning
    val primaryColor = if (isRunning) Color(0xFF00E676) else Color(0xFF9E9E9E)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(48.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Header
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "AdBlock VPN",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isRunning) "Protected & Anonymous" else "Ready to Connect",
                style = MaterialTheme.typography.bodyLarge,
                color = if (isRunning) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Clean Circular Connect Button
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(120.dp))
                .background(
                    if (isRunning) primaryColor.copy(alpha = 0.15f) 
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .border(
                    width = 4.dp,
                    color = primaryColor.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(120.dp)
                )
                .shadow(
                    elevation = if (isRunning) 16.dp else 4.dp,
                    shape = RoundedCornerShape(120.dp),
                    spotColor = primaryColor
                )
                .clickable {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    if (isRunning) stopVpnService(context) else requestStart()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.PowerSettingsNew,
                contentDescription = "Toggle VPN",
                modifier = Modifier.size(100.dp),
                tint = primaryColor
            )
        }
        
        // Stats
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val format = java.text.NumberFormat.getNumberInstance(java.util.Locale.US)
                StatItem("SCANNED", format.format(state.queriesTotal))
                StatItem("BLOCKED", format.format(state.queriesBlocked))
                StatItem("THREATS", format.format(state.queriesZeroDayBlocked))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )
    }
}

private fun startVpnService(context: android.content.Context) {
    val intent = Intent(context, AdBlockVpnService::class.java).setAction(Constants.ACTION_START)
    ContextCompat.startForegroundService(context, intent)
}

private fun stopVpnService(context: android.content.Context) {
    val intent = Intent(context, AdBlockVpnService::class.java).setAction(Constants.ACTION_STOP)
    context.startService(intent)
}
