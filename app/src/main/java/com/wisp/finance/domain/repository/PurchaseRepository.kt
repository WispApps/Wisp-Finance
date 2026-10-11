package com.wisp.finance.domain.repository

import com.wisp.finance.domain.model.Purchase
import java.time.Instant
import kotlinx.coroutines.flow.Flow

interface PurchaseRepository {

    fun observeAll(): Flow<List<Purchase>>

    fun observeBetween(
        start: Instant,
        endExclusive: Instant,
    ): Flow<List<Purchase>>

    fun observeByCategory(categoryId: String): Flow<List<Purchase>>

    suspend fun getById(id: String): Purchase?

    suspend fun save(purchase: Purchase)

    suspend fun deleteById(id: String)
}
