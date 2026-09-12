package com.adblocker.vpn

import android.app.Application
import com.adblocker.vpn.util.CrashHandler

class AdBlockerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler(this))
    }
}
