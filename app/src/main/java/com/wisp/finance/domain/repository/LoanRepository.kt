package com.wisp.finance.domain.repository

import com.wisp.finance.domain.model.Loan
import com.wisp.finance.domain.model.LoanPayment
import kotlinx.coroutines.flow.Flow

interface LoanRepository {
    fun observeAll(): Flow<List<Loan>>
    fun observePayments(): Flow<List<LoanPayment>>
    suspend fun save(item: Loan)
    suspend fun savePayment(item: LoanPayment)
    suspend fun deleteById(id: String)
}
