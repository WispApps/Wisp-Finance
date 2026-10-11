package com.wisp.finance.data.repository

import com.wisp.finance.data.local.dao.AssetHoldingDao
import com.wisp.finance.data.local.entity.AssetHoldingEntity
import com.wisp.finance.domain.model.AssetHolding
import com.wisp.finance.domain.repository.AssetHoldingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomAssetHoldingRepository(private val dao: AssetHoldingDao) : AssetHoldingRepository {
    override fun observeAll(): Flow<List<AssetHolding>> = dao.observeAll().map { items ->
        items.map { it.toDomain() }
    }

    override suspend fun save(item: AssetHolding) = dao.upsert(item.toEntity())

    override suspend fun deleteById(id: String) = dao.deleteById(id)
}

private fun AssetHoldingEntity.toDomain(): AssetHolding = AssetHolding(
    id = id,
    kind = kind,
    name = name,
    symbol = symbol,
    quantity = quantity,
    averageCostMinorUnits = averageCostMinorUnits,
    currentPriceMinorUnits = currentPriceMinorUnits,
    currency = currency,
    note = note,
    archived = archived,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

private fun AssetHolding.toEntity(): AssetHoldingEntity = AssetHoldingEntity(
    id = id,
    kind = kind,
    name = name,
    symbol = symbol,
    quantity = quantity,
    averageCostMinorUnits = averageCostMinorUnits,
    currentPriceMinorUnits = currentPriceMinorUnits,
    currency = currency,
    note = note,
    archived = archived,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
