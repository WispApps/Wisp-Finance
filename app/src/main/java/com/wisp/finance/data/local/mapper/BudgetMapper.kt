package com.wisp.finance.data.local.mapper

import com.wisp.finance.data.local.entity.BudgetEntity
import com.wisp.finance.domain.model.Budget
import com.wisp.finance.domain.model.Money

fun BudgetEntity.toDomain(): Budget {
    return Budget(
        id = id,
        categoryId = categoryId,
        amount = Money(
            minorUnits = amountMinorUnits,
            currency = currency,
        ),
        period = period,
        enabled = enabled,
    )
}

fun Budget.toEntity(): BudgetEntity {
    return BudgetEntity(
        id = id,
        categoryId = categoryId,
        amountMinorUnits = amount.minorUnits,
        currency = amount.currency,
        period = period,
        enabled = enabled,
    )
}
