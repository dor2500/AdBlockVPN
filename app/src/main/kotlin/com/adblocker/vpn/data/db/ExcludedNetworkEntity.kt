package com.adblocker.vpn.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.adblocker.vpn.data.model.NetworkIdentifierType

@Entity(tableName = "excluded_networks")
data class ExcludedNetworkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val displayName: String,
    val identifierType: NetworkIdentifierType,
    /** Gateway IP for Wi-Fi, SSID for Wi-Fi fallback, or MCC-MNC string for cellular. */
    val identifierValue: String,
    val enabled: Boolean = true
)
