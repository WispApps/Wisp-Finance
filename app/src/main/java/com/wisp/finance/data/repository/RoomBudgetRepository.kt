package com.wisp.finance.data.repository

import com.wisp.finance.data.local.dao.BudgetDao
import com.wisp.finance.data.local.mapper.toDomain
import com.wisp.finance.data.local.mapper.toEntity
import com.wisp.finance.domain.model.Budget
import com.wisp.finance.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomBudgetRepository(
    private val budgetDao: BudgetDao,
) : BudgetRepository {

    override fun observeAll(): Flow<List<Budget>> {
        return budgetDao.observeAll().map { budgets ->
            budgets.map { it.toDomain() }
        }
    }

    override suspend fun getById(id: String): Budget? {
        return budgetDao.getById(id)?.toDomain()
    }

    override suspend fun save(budget: Budget) {
        budgetDao.upsert(budget.toEntity())
    }

    override suspend fun deleteById(id: String) {
        budgetDao.deleteById(id)
    }
}
