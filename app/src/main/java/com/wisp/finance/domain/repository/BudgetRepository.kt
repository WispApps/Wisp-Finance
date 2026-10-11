package com.wisp.finance.domain.repository

import com.wisp.finance.domain.model.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {

    fun observeAll(): Flow<List<Budget>>

    suspend fun getById(id: String): Budget?

    suspend fun save(budget: Budget)

    suspend fun deleteById(id: String)
}
