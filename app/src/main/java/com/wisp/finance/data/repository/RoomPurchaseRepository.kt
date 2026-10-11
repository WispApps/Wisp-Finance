package com.wisp.finance.data.repository

import com.wisp.finance.data.local.dao.PurchaseDao
import com.wisp.finance.data.local.mapper.toDomain
import com.wisp.finance.data.local.mapper.toEntity
import com.wisp.finance.domain.model.Purchase
import com.wisp.finance.domain.repository.PurchaseRepository
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomPurchaseRepository(
    private val purchaseDao: PurchaseDao,
) : PurchaseRepository {

    override fun observeAll(): Flow<List<Purchase>> {
        return purchaseDao.observeAll().map { purchases ->
            purchases.map { it.toDomain() }
        }
    }

    override fun observeBetween(
        start: Instant,
        endExclusive: Instant,
    ): Flow<List<Purchase>> {
        return purchaseDao.observeBetween(
            start = start,
            endExclusive = endExclusive,
        ).map { purchases ->
            purchases.map { it.toDomain() }
        }
    }

    override fun observeByCategory(categoryId: String): Flow<List<Purchase>> {
        return purchaseDao.observeByCategory(categoryId).map { purchases ->
            purchases.map { it.toDomain() }
        }
    }

    override suspend fun getById(id: String): Purchase? {
        return purchaseDao.getById(id)?.toDomain()
    }

    override suspend fun save(purchase: Purchase) {
        purchaseDao.upsert(purchase.toEntity())
    }

    override suspend fun deleteById(id: String) {
        purchaseDao.deleteById(id)
    }
}
