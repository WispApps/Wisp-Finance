package com.wisp.finance.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.wisp.finance.data.local.entity.TaxEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaxEntryDao {
    @Query("SELECT * FROM tax_entries ORDER BY tax_year DESC, occurred_on DESC, created_at DESC")
    fun observeAll(): Flow<List<TaxEntryEntity>>

    @Upsert
    suspend fun upsert(item: TaxEntryEntity)

    @Query("DELETE FROM tax_entries WHERE id = :id")
    suspend fun deleteById(id: String)
}
