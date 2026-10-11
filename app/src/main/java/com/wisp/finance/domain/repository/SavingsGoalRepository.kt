package com.wisp.finance.domain.repository

import com.wisp.finance.domain.model.SavingsGoal
import kotlinx.coroutines.flow.Flow

interface SavingsGoalRepository {
    fun observeAll(): Flow<List<SavingsGoal>>
    suspend fun save(goal: SavingsGoal)
    suspend fun deleteById(id: String)
}
