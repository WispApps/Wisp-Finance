package com.wisp.finance.domain.repository

import com.wisp.finance.domain.model.RecurringExpense
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface RecurringExpenseRepository {

    fun observeAll(): Flow<List<RecurringExpense>>

    suspend fun getDueOnOrBefore(date: LocalDate): List<RecurringExpense>

    suspend fun getById(id: String): RecurringExpense?

    suspend fun save(recurringExpense: RecurringExpense)

    suspend fun deleteById(id: String)
}
