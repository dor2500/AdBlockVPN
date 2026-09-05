package com.adblocker.vpn.vpn

import java.nio.ByteBuffer

/**
 * Minimal IPv4 + UDP + DNS parser/builder for packets read from the TUN device.
 * We only need enough of each layer to (a) recognise a DNS query on port 53
 * and (b) synthesize a matching response — full IP stack reimplementation is
 * out of scope, so anything that isn't IPv4/UDP/DNS is treated as "not DNS"
 * and forwarded untouched via the TUN socket-protect + real socket path.
 */
object DnsPacketParser {

    data class ParsedUdp(
        val sourceAddress: ByteArray,
        val destAddress: ByteArray,
        val sourcePort: Int,
        val destPort: Int,
        val ipHeaderLength: Int,
        val payload: ByteArray
    )

    /** Returns null if [packet] is not a well-formed IPv4/UDP datagram. */
    fun parseIpv4Udp(packet: ByteArray, length: Int): ParsedUdp? {
        if (length < 20) return null
        val versionAndIhl = packet[0].toInt() and 0xFF
        val version = versionAndIhl shr 4
        if (version != 4) return null // IPv6 not handled by this simplified engine
        val ihl = (versionAndIhl and 0x0F) * 4
        if (length < ihl + 8) return null

        val protocol = packet[9].toInt() and 0xFF
        if (protocol != 17) return null // UDP only

        val srcAddr = packet.copyOfRange(12, 16)
        val dstAddr = packet.copyOfRange(16, 20)

        val udpOffset = ihl
        val srcPort = ((packet[udpOffset].toInt() and 0xFF) shl 8) or (packet[udpOffset + 1].toInt() and 0xFF)
        val dstPort = ((packet[udpOffset + 2].toInt() and 0xFF) shl 8) or (packet[udpOffset + 3].toInt() and 0xFF)
        val udpLength = ((packet[udpOffset + 4].toInt() and 0xFF) shl 8) or (packet[udpOffset + 5].toInt() and 0xFF)

        val payloadStart = udpOffset + 8
        val payloadEnd = minOf(length, udpOffset + udpLength)
        if (payloadEnd <= payloadStart) return null
        val payload = packet.copyOfRange(payloadStart, payloadEnd)

        return ParsedUdp(srcAddr, dstAddr, srcPort, dstPort, ihl, payload)
    }

    /** Extracts the queried hostname from a DNS query payload (question section only). */
    fun extractQueryName(dnsPayload: ByteArray): String? {
        if (dnsPayload.size < 12) return null
        var pos = 12 // past the 12-byte DNS header
        val sb = StringBuilder()
        while (pos < dnsPayload.size) {
            val len = dnsPayload[pos].toInt() and 0xFF
            if (len == 0) break
            if (pos + 1 + len > dnsPayload.size) return null
            if (sb.isNotEmpty()) sb.append('.')
            sb.append(String(dnsPayload, pos + 1, len, Charsets.US_ASCII))
            pos += 1 + len
        }
        return if (sb.isEmpty()) null else sb.toString()
    }

    /**
     * Builds a synthetic IPv4/UDP/DNS response packet that answers the original
     * query (matching transaction id/question) with A record 0.0.0.0, i.e. a
     * blocked-domain response, addressed back to the original requester.
     */
    fun buildBlockedResponse(original: ByteArray, originalLength: Int): ByteArray? {
        val parsed = parseIpv4Udp(original, originalLength) ?: return null
        val dnsQuery = parsed.payload
        if (dnsQuery.size < 12) return null

        val txId = dnsQuery.copyOfRange(0, 2)
        // Question section is everything from byte 12 to the end of the original query.
        val question = dnsQuery.copyOfRange(12, dnsQuery.size)

        val dnsHeader = ByteBuffer.allocate(12).apply {
            put(txId)
            putShort(0x8180.toShort()) // standard response, recursion available, no error
            putShort(1)                // QDCOUNT
            putShort(1)                // ANCOUNT
            putShort(0)                // NSCOUNT
            putShort(0)                // ARCOUNT
        }.array()

        // Answer: name = pointer to offset 12 (0xC00C), type A, class IN, TTL, RDLENGTH=4, RDATA=0.0.0.0
        val answer = ByteBuffer.allocate(16).apply {
            putShort(0xC00C.toShort())
            putShort(1)   // TYPE A
            putShort(1)   // CLASS IN
            putInt(60)    // TTL
            putShort(4)   // RDLENGTH
            put(byteArrayOf(0, 0, 0, 0)) // 0.0.0.0
        }.array()

        val dnsResponse = dnsHeader + question + answer

        return buildUdpIpv4Packet(
            srcAddr = parsed.destAddress,   // swap: we are now the "server"
            dstAddr = parsed.sourceAddress,
            srcPort = parsed.destPort,
            dstPort = parsed.sourcePort,
            payload = dnsResponse
        )
    }

    /** Wraps [payload] in a UDP header, then an IPv4 header, for injection back into the TUN. */
    fun buildUdpIpv4Packet(
        srcAddr: ByteArray,
        dstAddr: ByteArray,
        srcPort: Int,
        dstPort: Int,
        payload: ByteArray
    ): ByteArray {
        val udpLength = 8 + payload.size
        val totalLength = 20 + udpLength

        val buffer = ByteBuffer.allocate(totalLength)

        // --- IPv4 header ---
        buffer.put((0x45).toByte())           // version=4, IHL=5 (20 bytes, no options)
        buffer.put(0)                          // DSCP/ECN
        buffer.putShort(totalLength.toShort())
        buffer.putShort(0)                     // identification
        buffer.putShort(0x4000.toShort())      // flags=DF, fragment offset=0
        buffer.put(64)                          // TTL
        buffer.put(17)                          // protocol = UDP
        buffer.putShort(0)                     // checksum placeholder
        buffer.put(srcAddr)
        buffer.put(dstAddr)

        // --- UDP header ---
        buffer.putShort(srcPort.toShort())
        buffer.putShort(dstPort.toShort())
        buffer.putShort(udpLength.toShort())
        buffer.putShort(0) // UDP checksum (0 = not computed; valid for IPv4)

        buffer.put(payload)

        val bytes = buffer.array()
        val ipChecksum = computeChecksum(bytes, 0, 20)
        bytes[10] = (ipChecksum shr 8).toByte()
        bytes[11] = (ipChecksum and 0xFF).toByte()
        return bytes
    }

    private fun computeChecksum(data: ByteArray, offset: Int, length: Int): Int {
        var sum = 0L
        var i = offset
        while (i < offset + length) {
            val word = ((data[i].toInt() and 0xFF) shl 8) or
                (if (i + 1 < offset + length) (data[i + 1].toInt() and 0xFF) else 0)
            sum += word
            i += 2
        }
        while (sum shr 16 != 0L) sum = (sum and 0xFFFF) + (sum shr 16)
        return (sum.inv() and 0xFFFF).toInt()
    }

    /**
     * Minimal parser to check if any returned A record contains a private/local IPv4 address.
     * This is used for DNS Rebinding protection.
     */
    fun containsPrivateIp(dnsResponse: ByteArray): Boolean {
        try {
            if (dnsResponse.size < 12) return false
            val qdCount = ((dnsResponse[4].toInt() and 0xFF) shl 8) or (dnsResponse[5].toInt() and 0xFF)
            val anCount = ((dnsResponse[6].toInt() and 0xFF) shl 8) or (dnsResponse[7].toInt() and 0xFF)
            
            if (anCount == 0) return false

            var pos = 12
            // Skip questions
            for (i in 0 until qdCount) {
                while (pos < dnsResponse.size) {
                    val len = dnsResponse[pos].toInt() and 0xFF
                    if (len == 0) {
                        pos++
                        break
                    }
                    if ((len and 0xC0) == 0xC0) {
                        pos += 2
                        break
                    }
                    pos += len + 1
                }
                pos += 4 // QTYPE, QCLASS
            }

            // Parse answers
            for (i in 0 until anCount) {
                if (pos >= dnsResponse.size) return false
                // Skip Name
                if ((dnsResponse[pos].toInt() and 0xC0) == 0xC0) {
                    pos += 2
                } else {
                    while (pos < dnsResponse.size) {
                        val len = dnsResponse[pos].toInt() and 0xFF
                        if (len == 0) {
                            pos++
                            break
                        }
                        pos += len + 1
                    }
                }
                
                if (pos + 10 > dnsResponse.size) return false
                val type = ((dnsResponse[pos].toInt() and 0xFF) shl 8) or (dnsResponse[pos + 1].toInt() and 0xFF)
                val dataLen = ((dnsResponse[pos + 8].toInt() and 0xFF) shl 8) or (dnsResponse[pos + 9].toInt() and 0xFF)
                pos += 10
                
                if (type == 1 && dataLen == 4) { // TYPE A (IPv4)
                    if (pos + 4 > dnsResponse.size) return false
                    val ip1 = dnsResponse[pos].toInt() and 0xFF
                    val ip2 = dnsResponse[pos + 1].toInt() and 0xFF
                    
                    // Check private ranges:
                    // 127.0.0.0/8
                    // 10.0.0.0/8
                    // 172.16.0.0/12
                    // 192.168.0.0/16
                    // 0.0.0.0/8
                    if (ip1 == 127 || ip1 == 10 || ip1 == 0 || (ip1 == 192 && ip2 == 168) || (ip1 == 172 && ip2 in 16..31)) {
                        return true
                    }
                }
                pos += dataLen
            }
        } catch (e: Exception) {
            // Ignore parsing errors, assume false
        }
        return false
    }
}
