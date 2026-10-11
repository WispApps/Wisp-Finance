package com.wisp.finance.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.wisp.finance.data.local.entity.MortgageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MortgageDao {
    @Query("SELECT * FROM mortgages ORDER BY active DESC, updated_at DESC")
    fun observeAll(): Flow<List<MortgageEntity>>

    @Upsert
    suspend fun upsert(item: MortgageEntity)

    @Query("DELETE FROM mortgages WHERE id = :id")
    suspend fun deleteById(id: String)
}
