package com.wisp.finance.data.repository

import com.wisp.finance.data.local.dao.LoanDao
import com.wisp.finance.data.local.entity.LoanEntity
import com.wisp.finance.data.local.entity.LoanPaymentEntity
import com.wisp.finance.domain.model.Loan
import com.wisp.finance.domain.model.LoanPayment
import com.wisp.finance.domain.repository.LoanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomLoanRepository(
    private val dao: LoanDao,
) : LoanRepository {
    override fun observeAll(): Flow<List<Loan>> = dao.observeAll().map { items ->
        items.map { it.toDomain() }
    }

    override fun observePayments(): Flow<List<LoanPayment>> = dao.observeAllPayments().map { items ->
        items.map { it.toDomain() }
    }

    override suspend fun save(item: Loan) {
        dao.upsert(item.toEntity())
    }

    override suspend fun savePayment(item: LoanPayment) {
        dao.upsertPayment(item.toEntity())
    }

    override suspend fun deleteById(id: String) {
        dao.deleteById(id)
    }
}

private fun LoanEntity.toDomain(): Loan = Loan(
    id = id,
    title = title,
    principalMinorUnits = principalMinorUnits,
    remainingMinorUnits = remainingMinorUnits,
    currency = currency,
    annualInterestRate = annualInterestRate,
    termMonths = termMonths,
    startDate = startDate,
    nextPaymentDate = nextPaymentDate,
    monthlyPaymentMinorUnits = monthlyPaymentMinorUnits,
    note = note,
    closed = closed,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

private fun Loan.toEntity(): LoanEntity = LoanEntity(
    id = id,
    title = title,
    principalMinorUnits = principalMinorUnits,
    remainingMinorUnits = remainingMinorUnits,
    currency = currency,
    annualInterestRate = annualInterestRate,
    termMonths = termMonths,
    startDate = startDate,
    nextPaymentDate = nextPaymentDate,
    monthlyPaymentMinorUnits = monthlyPaymentMinorUnits,
    note = note,
    closed = closed,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

private fun LoanPaymentEntity.toDomain(): LoanPayment = LoanPayment(
    id = id,
    loanId = loanId,
    amountMinorUnits = amountMinorUnits,
    paidAt = paidAt,
    note = note,
    createdAt = createdAt,
)

private fun LoanPayment.toEntity(): LoanPaymentEntity = LoanPaymentEntity(
    id = id,
    loanId = loanId,
    amountMinorUnits = amountMinorUnits,
    paidAt = paidAt,
    note = note,
    createdAt = createdAt,
)
