package com.wisp.finance.data.local.mapper

import com.wisp.finance.data.local.entity.SavingsGoalEntity
import com.wisp.finance.domain.model.Money
import com.wisp.finance.domain.model.SavingsGoal

fun SavingsGoalEntity.toDomain(): SavingsGoal = SavingsGoal(
    id = id,
    title = title,
    targetAmount = Money(targetMinorUnits, currency),
    savedMinorUnits = savedMinorUnits,
    deadline = deadline,
    completed = completed,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun SavingsGoal.toEntity(): SavingsGoalEntity = SavingsGoalEntity(
    id = id,
    title = title,
    targetMinorUnits = targetAmount.minorUnits,
    currency = targetAmount.currency,
    savedMinorUnits = savedMinorUnits,
    deadline = deadline,
    completed = completed,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
