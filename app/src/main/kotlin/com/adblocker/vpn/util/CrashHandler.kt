package com.adblocker.vpn.util

import android.content.Context
import android.content.Intent
import com.adblocker.vpn.ui.CrashActivity
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

class CrashHandler(private val context: Context) : Thread.UncaughtExceptionHandler {
    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, exception: Throwable) {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        exception.printStackTrace(pw)
        val stackTrace = sw.toString()

        val intent = Intent(context, CrashActivity::class.java).apply {
            putExtra("CRASH_LOG", stackTrace)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)

        defaultHandler?.uncaughtException(thread, exception)
        exitProcess(1)
    }
}
