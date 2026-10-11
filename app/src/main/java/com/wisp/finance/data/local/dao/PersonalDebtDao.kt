package com.wisp.finance.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.wisp.finance.data.local.entity.PersonalDebtEntity
import com.wisp.finance.data.local.entity.PersonalDebtPaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalDebtDao {
    @Query("SELECT * FROM personal_debts ORDER BY closed ASC, updated_at DESC")
    fun observeAll(): Flow<List<PersonalDebtEntity>>

    @Query("SELECT * FROM personal_debt_payments ORDER BY paid_at DESC, created_at DESC")
    fun observeAllPayments(): Flow<List<PersonalDebtPaymentEntity>>

    @Upsert
    suspend fun upsert(item: PersonalDebtEntity)

    @Upsert
    suspend fun upsertPayment(item: PersonalDebtPaymentEntity)

    @Query("DELETE FROM personal_debts WHERE id = :id")
    suspend fun deleteById(id: String)
}
