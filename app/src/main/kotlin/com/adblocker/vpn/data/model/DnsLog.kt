package com.adblocker.vpn.data.model

data class DnsLog(
    val timestamp: Long,
    val domain: String,
    val isBlocked: Boolean
)
