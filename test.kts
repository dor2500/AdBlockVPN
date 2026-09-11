import java.net.URL
import java.net.HttpURLConnection
import java.io.BufferedReader
import java.io.InputStreamReader

fun isVersionNewer(currentVersion: String, newVersion: String): Boolean {
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

fun main() {
    println("Version Check: " + isVersionNewer("6.5.34", "6.5.35"))
    
    try {
        val url = URL("https://api.github.com/repos/dor2500/AdBlockVPN/releases/latest")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
        connection.setRequestProperty("User-Agent", "AdBlockVPN-Updater")
        
        println("Response Code: ${connection.responseCode}")
    } catch(e: Exception) {
        println("Exception: ${e.message}")
    }
}
main()
