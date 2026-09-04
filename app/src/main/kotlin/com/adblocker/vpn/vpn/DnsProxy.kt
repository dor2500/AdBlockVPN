package com.adblocker.vpn.vpn

import android.net.VpnService
import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress

/**
 * Forwards a DNS query to an upstream resolver (1.1.1.1 / 8.8.8.8) using a plain
 * UDP socket that is "protected" via VpnService.protect() so the reply doesn't
 * get routed back into our own TUN interface (which would cause a loop).
 */
class DnsProxy(private val vpnService: VpnService) {

    fun forward(
        queryPayload: ByteArray,
        upstreamHost: String,
        useDoh: Boolean = true,
        timeoutMs: Int = 4000
    ): ByteArray? {
        if (useDoh) {
            return forwardHttps(queryPayload, upstreamHost, timeoutMs)
        }
        var socket: DatagramSocket? = null
        return try {
            socket = DatagramSocket().apply {
                soTimeout = timeoutMs
            }
            vpnService.protect(socket)

            val request = DatagramPacket(
                queryPayload, queryPayload.size,
                InetSocketAddress(upstreamHost, 53)
            )
            socket.send(request)

            val responseBuffer = ByteArray(1500)
            val responsePacket = DatagramPacket(responseBuffer, responseBuffer.size)
            socket.receive(responsePacket)

            responseBuffer.copyOfRange(0, responsePacket.length)
        } catch (e: Exception) {
            Log.w(TAG, "UDP forward failed for $upstreamHost: ${e.message}")
            null
        } finally {
            socket?.close()
        }
    }

    private fun forwardHttps(
        queryPayload: ByteArray,
        upstreamHost: String,
        timeoutMs: Int
    ): ByteArray? {
        return try {
            // Map common IPs to their DoH endpoint, default to standard format
            val urlStr = when (upstreamHost) {
                "1.1.1.1" -> "https://cloudflare-dns.com/dns-query"
                "8.8.8.8" -> "https://dns.google/dns-query"
                "9.9.9.9" -> "https://dns.quad9.net/dns-query"
                else -> "https://$upstreamHost/dns-query"
            }
            val url = java.net.URL(urlStr)
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/dns-message")
            connection.setRequestProperty("Accept", "application/dns-message")
            connection.connectTimeout = timeoutMs
            connection.readTimeout = timeoutMs
            connection.doOutput = true
            
            connection.outputStream.use { os ->
                os.write(queryPayload)
            }
            
            if (connection.responseCode == 200) {
                connection.inputStream.readBytes()
            } else {
                Log.w(TAG, "DoH failed with code: ${connection.responseCode}")
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "HTTPS forward failed for $upstreamHost: ${e.message}")
            null
        }
    }

    companion object {
        private const val TAG = "DnsProxy"
    }
}
