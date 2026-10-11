package com.wisp.finance.domain.repository

import com.wisp.finance.domain.model.TaxEntry
import kotlinx.coroutines.flow.Flow

interface TaxEntryRepository {
    fun observeAll(): Flow<List<TaxEntry>>
    suspend fun save(item: TaxEntry)
    suspend fun deleteById(id: String)
}
