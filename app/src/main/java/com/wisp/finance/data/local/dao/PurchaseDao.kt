package com.wisp.finance.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.wisp.finance.data.local.entity.PurchaseEntity
import java.time.Instant
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {

    @Query(
        """
        SELECT *
        FROM purchases
        ORDER BY occurred_at DESC, created_at DESC
        """
    )
    fun observeAll(): Flow<List<PurchaseEntity>>

    @Query(
        """
        SELECT *
        FROM purchases
        WHERE occurred_at >= :start
          AND occurred_at < :endExclusive
        ORDER BY occurred_at DESC, created_at DESC
        """
    )
    fun observeBetween(
        start: Instant,
        endExclusive: Instant,
    ): Flow<List<PurchaseEntity>>

    @Query(
        """
        SELECT *
        FROM purchases
        WHERE category_id = :categoryId
        ORDER BY occurred_at DESC, created_at DESC
        """
    )
    fun observeByCategory(categoryId: String): Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM purchases WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): PurchaseEntity?

    @Upsert
    suspend fun upsert(purchase: PurchaseEntity)

    @Query("DELETE FROM purchases WHERE id = :id")
    suspend fun deleteById(id: String)
}
