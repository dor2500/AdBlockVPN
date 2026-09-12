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
    private const val GITHUB_LATEST_RELEASE_URL = "https://raw.githubusercontent.com/dor2500/AdBlockVPN/main/update.json"
    private const val GITHUB_RELEASES_URL = "https://api.github.com/repos/dor2500/AdBlockVPN/releases"

    suspend fun fetchChangelog(): List<ReleaseInfo> = withContext(Dispatchers.IO) {
        val releases = mutableListOf<ReleaseInfo>()
        try {
            val url = URL(GITHUB_RELEASES_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.setRequestProperty("User-Agent", "AdBlockVPN-Updater")

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                val jsonArray = org.json.JSONArray(response)
                for (i in 0 until jsonArray.length()) {
                    val json = jsonArray.getJSONObject(i)
                    var tagName = json.getString("tag_name")
                    if (tagName.startsWith("v")) tagName = tagName.substring(1)
                    val name = json.optString("name", tagName)
                    val body = json.optString("body", "No release notes available.")
                    val date = json.optString("published_at", "")
                    releases.add(ReleaseInfo(tagName, name, body, date))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch changelog", e)
        }
        releases
    }

    suspend fun checkForUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.github.com/repos/dor2500/AdBlockVPN/releases/latest")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.setRequestProperty("User-Agent", "AdBlockVPN-Updater")
            // Prevent any local caching
            connection.setRequestProperty("Cache-Control", "no-cache")

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                val json = JSONObject(response)
                var tagName = json.getString("tag_name")
                if (tagName.startsWith("v")) {
                    tagName = tagName.substring(1)
                }
                
                val releaseNotes = json.optString("body", "No release notes available.")
                var downloadUrl = ""
                
                // Find the APK download URL in the assets
                if (json.has("assets")) {
                    val assets = json.getJSONArray("assets")
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val assetName = asset.getString("name")
                        if (assetName.endsWith(".apk")) {
                            downloadUrl = asset.getString("browser_download_url")
                            break
                        }
                    }
                }

                val isUpdateAvailable = downloadUrl.isNotEmpty() && isVersionNewer(BuildConfig.VERSION_NAME, tagName)
                return@withContext UpdateInfo(isUpdateAvailable, tagName, releaseNotes, downloadUrl)
            } else {
                Log.w(TAG, "Update GitHub API returned code ${connection.responseCode}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check for updates via GitHub API", e)
        }
        return@withContext null
    }

    fun downloadAndInstallUpdate(context: Context, downloadUrl: String, version: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open browser for update", e)
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
