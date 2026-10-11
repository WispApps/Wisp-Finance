package com.wisp.finance.data.local.mapper

import com.wisp.finance.data.local.entity.RecurringExpenseEntity
import com.wisp.finance.domain.model.Money
import com.wisp.finance.domain.model.RecurrenceRule
import com.wisp.finance.domain.model.RecurringExpense

fun RecurringExpenseEntity.toDomain(): RecurringExpense = RecurringExpense(
    id = id,
    amount = Money(amountMinorUnits, currency),
    categoryId = categoryId,
    title = title,
    note = note,
    merchant = merchant,
    paymentMethod = paymentMethod,
    recurrenceRule = RecurrenceRule(recurrenceInterval, recurrenceUnit),
    nextOccurrence = nextOccurrence,
    kind = kind,
    enabled = enabled,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun RecurringExpense.toEntity(): RecurringExpenseEntity = RecurringExpenseEntity(
    id = id,
    amountMinorUnits = amount.minorUnits,
    currency = amount.currency,
    categoryId = categoryId,
    title = title,
    note = note,
    merchant = merchant,
    paymentMethod = paymentMethod,
    recurrenceInterval = recurrenceRule.interval,
    recurrenceUnit = recurrenceRule.unit,
    nextOccurrence = nextOccurrence,
    kind = kind,
    enabled = enabled,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
