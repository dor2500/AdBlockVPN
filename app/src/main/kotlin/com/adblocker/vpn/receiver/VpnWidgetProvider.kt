package com.adblocker.vpn.receiver

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.adblocker.vpn.MainActivity
import com.adblocker.vpn.R
import com.adblocker.vpn.vpn.AdBlockVpnService
import com.adblocker.vpn.util.Constants

class VpnWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TOGGLE_VPN) {
            val isRunning = AdBlockVpnService.state.value.isRunning
            if (isRunning) {
                val stopIntent = Intent(context, AdBlockVpnService::class.java).apply {
                    action = Constants.ACTION_STOP
                }
                context.startService(stopIntent)
            } else {
                val startIntent = Intent(context, AdBlockVpnService::class.java).apply {
                    action = Constants.ACTION_START
                }
                try {
                    context.startService(startIntent)
                } catch (e: Exception) {
                    // Cannot start foreground service directly from background in some OS versions
                    // Fallback to opening the app
                    val mainIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(mainIntent)
                }
            }
            
            // Trigger a manual update to reflect the new state
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, VpnWidgetProvider::class.java)
            )
            onUpdate(context, appWidgetManager, appWidgetIds)
        } else if (intent.action == ACTION_UPDATE_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, VpnWidgetProvider::class.java)
            )
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    companion object {
        const val ACTION_TOGGLE_VPN = "com.adblocker.vpn.ACTION_TOGGLE_VPN"
        const val ACTION_UPDATE_WIDGET = "com.adblocker.vpn.ACTION_UPDATE_WIDGET"

        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val isRunning = AdBlockVpnService.state.value.isRunning
            val views = RemoteViews(context.packageName, R.layout.widget_vpn)

            if (isRunning) {
                views.setTextViewText(R.id.widget_status_text, "Connected")
                views.setTextColor(R.id.widget_status_text, android.graphics.Color.parseColor("#4CAF50"))
            } else {
                views.setTextViewText(R.id.widget_status_text, "Disconnected")
                views.setTextColor(R.id.widget_status_text, android.graphics.Color.parseColor("#F44336"))
            }

            // Set up click intent
            val toggleIntent = Intent(context, VpnWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE_VPN
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, toggleIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_icon, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_status_text, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
