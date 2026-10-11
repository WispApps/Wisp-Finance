package com.wisp.finance.data.local.mapper

import com.wisp.finance.data.local.entity.PurchaseEntity
import com.wisp.finance.domain.model.Money
import com.wisp.finance.domain.model.Purchase

fun PurchaseEntity.toDomain(): Purchase {
    return Purchase(
        id = id,
        amount = Money(
            minorUnits = amountMinorUnits,
            currency = currency,
        ),
        categoryId = categoryId,
        title = title,
        note = note,
        merchant = merchant,
        paymentMethod = paymentMethod,
        occurredAt = occurredAt,
        type = type,
        recurringExpenseId = recurringExpenseId,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun Purchase.toEntity(): PurchaseEntity {
    return PurchaseEntity(
        id = id,
        amountMinorUnits = amount.minorUnits,
        currency = amount.currency,
        categoryId = categoryId,
        title = title,
        note = note,
        merchant = merchant,
        paymentMethod = paymentMethod,
        occurredAt = occurredAt,
        type = type,
        recurringExpenseId = recurringExpenseId,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
