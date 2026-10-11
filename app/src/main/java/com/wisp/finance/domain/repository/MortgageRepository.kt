package com.wisp.finance.domain.repository

import com.wisp.finance.domain.model.Mortgage
import kotlinx.coroutines.flow.Flow

interface MortgageRepository {
    fun observeAll(): Flow<List<Mortgage>>
    suspend fun save(item: Mortgage)
    suspend fun deleteById(id: String)
}
