package com.wisp.finance.domain.repository

import com.wisp.finance.domain.model.PersonalDebt
import com.wisp.finance.domain.model.PersonalDebtPayment
import kotlinx.coroutines.flow.Flow

interface PersonalDebtRepository {
    fun observeAll(): Flow<List<PersonalDebt>>
    fun observePayments(): Flow<List<PersonalDebtPayment>>
    suspend fun save(item: PersonalDebt)
    suspend fun savePayment(item: PersonalDebtPayment)
    suspend fun deleteById(id: String)
}
