package com.wisp.finance.data.repository

import com.wisp.finance.data.local.dao.TaxEntryDao
import com.wisp.finance.data.local.entity.TaxEntryEntity
import com.wisp.finance.domain.model.TaxEntry
import com.wisp.finance.domain.repository.TaxEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomTaxEntryRepository(private val dao: TaxEntryDao) : TaxEntryRepository {
    override fun observeAll(): Flow<List<TaxEntry>> = dao.observeAll().map { items ->
        items.map { it.toDomain() }
    }

    override suspend fun save(item: TaxEntry) = dao.upsert(item.toEntity())

    override suspend fun deleteById(id: String) = dao.deleteById(id)
}

private fun TaxEntryEntity.toDomain(): TaxEntry = TaxEntry(
    id = id,
    type = type,
    title = title,
    amountMinorUnits = amountMinorUnits,
    currency = currency,
    taxYear = taxYear,
    occurredOn = occurredOn,
    note = note,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

private fun TaxEntry.toEntity(): TaxEntryEntity = TaxEntryEntity(
    id = id,
    type = type,
    title = title,
    amountMinorUnits = amountMinorUnits,
    currency = currency,
    taxYear = taxYear,
    occurredOn = occurredOn,
    note = note,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
