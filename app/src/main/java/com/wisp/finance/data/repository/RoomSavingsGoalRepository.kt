package com.wisp.finance.data.repository

import com.wisp.finance.data.local.dao.SavingsGoalDao
import com.wisp.finance.data.local.mapper.toDomain
import com.wisp.finance.data.local.mapper.toEntity
import com.wisp.finance.domain.model.SavingsGoal
import com.wisp.finance.domain.repository.SavingsGoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomSavingsGoalRepository(
    private val dao: SavingsGoalDao,
) : SavingsGoalRepository {
    override fun observeAll(): Flow<List<SavingsGoal>> =
        dao.observeAll().map { goals -> goals.map { it.toDomain() } }

    override suspend fun save(goal: SavingsGoal) {
        dao.upsert(goal.toEntity())
    }

    override suspend fun deleteById(id: String) {
        dao.deleteById(id)
    }
}
