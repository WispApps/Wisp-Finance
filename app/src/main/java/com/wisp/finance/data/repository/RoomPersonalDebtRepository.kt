package com.wisp.finance.data.repository

import com.wisp.finance.data.local.dao.PersonalDebtDao
import com.wisp.finance.data.local.entity.PersonalDebtEntity
import com.wisp.finance.data.local.entity.PersonalDebtPaymentEntity
import com.wisp.finance.domain.model.PersonalDebt
import com.wisp.finance.domain.model.PersonalDebtPayment
import com.wisp.finance.domain.repository.PersonalDebtRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomPersonalDebtRepository(
    private val dao: PersonalDebtDao,
) : PersonalDebtRepository {
    override fun observeAll(): Flow<List<PersonalDebt>> = dao.observeAll().map { items ->
        items.map { it.toDomain() }
    }

    override fun observePayments(): Flow<List<PersonalDebtPayment>> = dao.observeAllPayments().map { items ->
        items.map { it.toDomain() }
    }

    override suspend fun save(item: PersonalDebt) {
        dao.upsert(item.toEntity())
    }

    override suspend fun savePayment(item: PersonalDebtPayment) {
        dao.upsertPayment(item.toEntity())
    }

    override suspend fun deleteById(id: String) {
        dao.deleteById(id)
    }
}

private fun PersonalDebtEntity.toDomain(): PersonalDebt = PersonalDebt(
    id = id,
    personName = personName,
    direction = direction,
    originalMinorUnits = originalMinorUnits,
    remainingMinorUnits = remainingMinorUnits,
    currency = currency,
    dueDate = dueDate,
    annualInterestRate = annualInterestRate,
    interestStartDate = interestStartDate,
    note = note,
    closed = closed,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

private fun PersonalDebt.toEntity(): PersonalDebtEntity = PersonalDebtEntity(
    id = id,
    personName = personName,
    direction = direction,
    originalMinorUnits = originalMinorUnits,
    remainingMinorUnits = remainingMinorUnits,
    currency = currency,
    dueDate = dueDate,
    annualInterestRate = annualInterestRate,
    interestStartDate = interestStartDate,
    note = note,
    closed = closed,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

private fun PersonalDebtPaymentEntity.toDomain(): PersonalDebtPayment = PersonalDebtPayment(
    id = id,
    debtId = debtId,
    amountMinorUnits = amountMinorUnits,
    paidAt = paidAt,
    note = note,
    createdAt = createdAt,
)

private fun PersonalDebtPayment.toEntity(): PersonalDebtPaymentEntity = PersonalDebtPaymentEntity(
    id = id,
    debtId = debtId,
    amountMinorUnits = amountMinorUnits,
    paidAt = paidAt,
    note = note,
    createdAt = createdAt,
)
