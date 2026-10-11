package com.wisp.finance.domain.repository

import com.wisp.finance.domain.model.AssetHolding
import kotlinx.coroutines.flow.Flow

interface AssetHoldingRepository {
    fun observeAll(): Flow<List<AssetHolding>>
    suspend fun save(item: AssetHolding)
    suspend fun deleteById(id: String)
}
