package com.adblocker.vpn.data.db

import androidx.room.TypeConverter
import com.adblocker.vpn.data.model.NetworkIdentifierType

class Converters {
    @TypeConverter
    fun fromType(type: NetworkIdentifierType): String = type.name

    @TypeConverter
    fun toType(value: String): NetworkIdentifierType = NetworkIdentifierType.valueOf(value)
}
