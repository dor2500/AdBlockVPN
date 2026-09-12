package com.adblocker.vpn.util

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.adblocker.vpn.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val isUpdateAvailable: Boolean,
    val latestVersion: String,
    val releaseNotes: String,
    val downloadUrl: String
)

data class ReleaseInfo(
    val version: String,
    val name: String,
    val notes: String,
    val date: String
)

object Updater {
    private const val TAG = "Updater"
    private const val GITHUB_UPDATE_JSON_URL = "https://raw.githubusercontent.com/dor2500/AdBlockVPN/main/update.json"

    suspend fun fetchChangelog(): List<ReleaseInfo> = withContext(Dispatchers.IO) {
        val releases = mutableListOf<ReleaseInfo>()
        try {
            val timestamp = System.currentTimeMillis()
            val url = URL("$GITHUB_UPDATE_JSON_URL?t=$timestamp")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Cache-Control", "no-cache")
            
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                val json = JSONObject(response)
                var tagName = json.optString("latestVersion", "")
                if (tagName.startsWith("v")) {
                    tagName = tagName.substring(1)
                }
                
                val releaseNotes = json.optString("releaseNotes", "No release notes available.")
                // Add the current latest as the only release in the changelog, since update.json only holds latest.
                if (tagName.isNotEmpty()) {
                    releases.add(ReleaseInfo(tagName, "v$tagName", releaseNotes, ""))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        releases
    }


    suspend fun checkForUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val timestamp = System.currentTimeMillis()
            val url = URL("$GITHUB_UPDATE_JSON_URL?t=$timestamp")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            // Prevent any local caching
            connection.setRequestProperty("Cache-Control", "no-cache")

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                val json = JSONObject(response)
                var tagName = json.optString("latestVersion", "")
                if (tagName.startsWith("v")) {
                    tagName = tagName.substring(1)
                }
                
                val releaseNotes = json.optString("releaseNotes", "No release notes available.")
                val downloadUrl = json.optString("downloadUrl", "")

                val isUpdateAvailable = downloadUrl.isNotEmpty() && isVersionNewer(BuildConfig.VERSION_NAME, tagName)
                return@withContext UpdateInfo(isUpdateAvailable, tagName, releaseNotes, downloadUrl)
            } else {
                Log.w(TAG, "Update JSON returned code ${connection.responseCode}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check for updates via update.json", e)
        }
        return@withContext null
    }

    fun downloadAndInstallUpdate(context: Context, downloadUrl: String, version: String) {
        try {
            val fileName = "AdBlockVPN-$version.apk"
            val request = DownloadManager.Request(Uri.parse(downloadUrl))
                .setTitle(context.getString(com.adblocker.vpn.R.string.app_name) + " Update")
                .setDescription("Downloading version $version...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
                .setMimeType("application/vnd.android.package-archive")

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = downloadManager.enqueue(request)

            val onComplete = object : BroadcastReceiver() {
                override fun onReceive(ctxt: Context, intent: Intent) {
                    if (intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) == downloadId) {
                        try {
                            val file = File(ctxt.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
                            if (file.exists()) {
                                val uri = FileProvider.getUriForFile(
                                    ctxt,
                                    "${ctxt.packageName}.fileprovider",
                                    file
                                )
                                val installIntent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "application/vnd.android.package-archive")
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                                }
                                ctxt.startActivity(installIntent)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error installing update", e)
                        }
                        try {
                            ctxt.unregisterReceiver(this)
                        } catch (e: Exception) {}
                    }
                }
            }

            ContextCompat.registerReceiver(
                context,
                onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                ContextCompat.RECEIVER_EXPORTED
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start download", e)
            // Fallback to browser
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }


    private fun isVersionNewer(currentVersion: String, newVersion: String): Boolean {
        val currentParts = currentVersion.split(".").map { it.toIntOrNull() ?: 0 }
        val newParts = newVersion.split(".").map { it.toIntOrNull() ?: 0 }

        val length = maxOf(currentParts.size, newParts.size)
        for (i in 0 until length) {
            val curr = currentParts.getOrElse(i) { 0 }
            val newV = newParts.getOrElse(i) { 0 }
            if (newV > curr) return true
            if (newV < curr) return false
        }
        return false
    }
}
