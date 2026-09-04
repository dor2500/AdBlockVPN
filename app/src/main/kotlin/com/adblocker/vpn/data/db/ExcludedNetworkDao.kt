package com.adblocker.vpn.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExcludedNetworkDao {
    @Query("SELECT * FROM excluded_networks ORDER BY id DESC")
    fun observeAll(): Flow<List<ExcludedNetworkEntity>>

    @Query("SELECT * FROM excluded_networks WHERE enabled = 1")
    suspend fun getAllEnabled(): List<ExcludedNetworkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ExcludedNetworkEntity): Long

    @Update
    suspend fun update(entity: ExcludedNetworkEntity)

    @Delete
    suspend fun delete(entity: ExcludedNetworkEntity)

    @Query("DELETE FROM excluded_networks WHERE id = :id")
    suspend fun deleteById(id: Long)
}
