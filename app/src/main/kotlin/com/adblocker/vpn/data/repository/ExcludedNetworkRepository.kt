package com.adblocker.vpn.data.repository

import android.content.Context
import com.adblocker.vpn.data.db.AppDatabase
import com.adblocker.vpn.data.db.ExcludedNetworkEntity
import com.adblocker.vpn.data.model.NetworkIdentifierType
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for user-defined "excluded" (bypass) networks.
 * Read from both the UI (management screen) and the VPN service's
 * NetworkCallback (to decide whether to enter pass-through mode).
 */
class ExcludedNetworkRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).excludedNetworkDao()

    fun observeAll(): Flow<List<ExcludedNetworkEntity>> = dao.observeAll()

    suspend fun getEnabledSnapshot(): List<ExcludedNetworkEntity> = dao.getAllEnabled()

    suspend fun add(displayName: String, type: NetworkIdentifierType, value: String): Long =
        dao.insert(
            ExcludedNetworkEntity(
                displayName = displayName,
                identifierType = type,
                identifierValue = value
            )
        )

    suspend fun setEnabled(entity: ExcludedNetworkEntity, enabled: Boolean) {
        dao.update(entity.copy(enabled = enabled))
    }

    suspend fun delete(entity: ExcludedNetworkEntity) = dao.delete(entity)
}
