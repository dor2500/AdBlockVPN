package com.adblocker.vpn.vpn

import android.net.VpnService
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

/**
 * Forwards a DNS query to an upstream resolver (1.1.1.1 / 8.8.8.8).
 * Uses OkHttp for DoH to enable connection pooling and HTTP/2 multiplexing,
 * which drastically reduces latency compared to creating a new TCP connection
 * for every query.
 */
class DnsProxy(private val vpnService: VpnService) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(4, TimeUnit.SECONDS)
        .writeTimeout(4, TimeUnit.SECONDS)
        .connectionPool(ConnectionPool(10, 5, TimeUnit.MINUTES))
        .build()

    private val dnsMediaType = "application/dns-message".toMediaType()

    fun forward(
        queryPayload: ByteArray,
        upstreamHost: String,
        useDoh: Boolean = true,
        timeoutMs: Int = 4000
    ): ByteArray? {
        if (useDoh) {
            return forwardHttps(queryPayload, upstreamHost)
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
        upstreamHost: String
    ): ByteArray? {
        return try {
            // Use IP addresses directly to avoid DNS lookups that would loop back into our VPN
            val urlStr = when (upstreamHost) {
                "1.1.1.1" -> "https://1.1.1.1/dns-query"
                "8.8.8.8" -> "https://8.8.8.8/dns-query"
                "9.9.9.9" -> "https://9.9.9.9/dns-query"
                else -> "https://$upstreamHost/dns-query"
            }

            val request = Request.Builder()
                .url(urlStr)
                .post(queryPayload.toRequestBody(dnsMediaType))
                .header("Accept", "application/dns-message")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.bytes()
                } else {
                    Log.w(TAG, "DoH failed with code: ${response.code}")
                    null
                }
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
