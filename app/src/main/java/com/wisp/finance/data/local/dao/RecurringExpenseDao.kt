package com.wisp.finance.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.wisp.finance.data.local.entity.RecurringExpenseEntity
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringExpenseDao {

    @Query(
        """
        SELECT *
        FROM recurring_expenses
        ORDER BY enabled DESC, next_occurrence ASC, title COLLATE NOCASE ASC
        """
    )
    fun observeAll(): Flow<List<RecurringExpenseEntity>>

    @Query(
        """
        SELECT *
        FROM recurring_expenses
        WHERE enabled = 1
          AND next_occurrence <= :date
        ORDER BY next_occurrence ASC
        """
    )
    suspend fun getDueOnOrBefore(date: LocalDate): List<RecurringExpenseEntity>

    @Query("SELECT * FROM recurring_expenses WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): RecurringExpenseEntity?

    @Upsert
    suspend fun upsert(recurringExpense: RecurringExpenseEntity)

    @Query("DELETE FROM recurring_expenses WHERE id = :id")
    suspend fun deleteById(id: String)
}
