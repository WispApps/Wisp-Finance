package com.wisp.finance.data.repository

import com.wisp.finance.data.local.dao.MortgageDao
import com.wisp.finance.data.local.entity.MortgageEntity
import com.wisp.finance.domain.model.Mortgage
import com.wisp.finance.domain.repository.MortgageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomMortgageRepository(private val dao: MortgageDao) : MortgageRepository {
    override fun observeAll(): Flow<List<Mortgage>> = dao.observeAll().map { items ->
        items.map { it.toDomain() }
    }

    override suspend fun save(item: Mortgage) = dao.upsert(item.toEntity())

    override suspend fun deleteById(id: String) = dao.deleteById(id)
}

private fun MortgageEntity.toDomain(): Mortgage = Mortgage(
    id = id,
    title = title,
    propertyPriceMinorUnits = propertyPriceMinorUnits,
    downPaymentMinorUnits = downPaymentMinorUnits,
    principalMinorUnits = principalMinorUnits,
    currency = currency,
    annualInterestRate = annualInterestRate,
    termMonths = termMonths,
    startDate = startDate,
    monthlyPaymentMinorUnits = monthlyPaymentMinorUnits,
    note = note,
    active = active,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

private fun Mortgage.toEntity(): MortgageEntity = MortgageEntity(
    id = id,
    title = title,
    propertyPriceMinorUnits = propertyPriceMinorUnits,
    downPaymentMinorUnits = downPaymentMinorUnits,
    principalMinorUnits = principalMinorUnits,
    currency = currency,
    annualInterestRate = annualInterestRate,
    termMonths = termMonths,
    startDate = startDate,
    monthlyPaymentMinorUnits = monthlyPaymentMinorUnits,
    note = note,
    active = active,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
