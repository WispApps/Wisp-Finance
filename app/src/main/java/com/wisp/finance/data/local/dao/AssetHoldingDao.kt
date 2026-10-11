package com.wisp.finance.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.wisp.finance.data.local.entity.AssetHoldingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetHoldingDao {
    @Query("SELECT * FROM asset_holdings ORDER BY archived ASC, kind ASC, updated_at DESC")
    fun observeAll(): Flow<List<AssetHoldingEntity>>

    @Upsert
    suspend fun upsert(item: AssetHoldingEntity)

    @Query("DELETE FROM asset_holdings WHERE id = :id")
    suspend fun deleteById(id: String)
}
