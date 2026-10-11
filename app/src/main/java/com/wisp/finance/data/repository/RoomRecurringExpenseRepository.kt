package com.wisp.finance.data.repository

import com.wisp.finance.data.local.dao.RecurringExpenseDao
import com.wisp.finance.data.local.mapper.toDomain
import com.wisp.finance.data.local.mapper.toEntity
import com.wisp.finance.domain.model.RecurringExpense
import com.wisp.finance.domain.repository.RecurringExpenseRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomRecurringExpenseRepository(
    private val recurringExpenseDao: RecurringExpenseDao,
) : RecurringExpenseRepository {

    override fun observeAll(): Flow<List<RecurringExpense>> {
        return recurringExpenseDao.observeAll().map { expenses ->
            expenses.map { it.toDomain() }
        }
    }

    override suspend fun getDueOnOrBefore(date: LocalDate): List<RecurringExpense> {
        return recurringExpenseDao
            .getDueOnOrBefore(date)
            .map { it.toDomain() }
    }

    override suspend fun getById(id: String): RecurringExpense? {
        return recurringExpenseDao.getById(id)?.toDomain()
    }

    override suspend fun save(recurringExpense: RecurringExpense) {
        recurringExpenseDao.upsert(recurringExpense.toEntity())
    }

    override suspend fun deleteById(id: String) {
        recurringExpenseDao.deleteById(id)
    }
}
