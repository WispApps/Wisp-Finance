package com.wisp.finance.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.wisp.finance.data.local.entity.LoanEntity
import com.wisp.finance.data.local.entity.LoanPaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {
    @Query("SELECT * FROM loans ORDER BY closed ASC, updated_at DESC")
    fun observeAll(): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loan_payments ORDER BY paid_at DESC, created_at DESC")
    fun observeAllPayments(): Flow<List<LoanPaymentEntity>>

    @Upsert
    suspend fun upsert(item: LoanEntity)

    @Upsert
    suspend fun upsertPayment(item: LoanPaymentEntity)

    @Query("DELETE FROM loans WHERE id = :id")
    suspend fun deleteById(id: String)
}
