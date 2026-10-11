package com.wisp.finance.domain.model

import java.time.Instant
import java.util.UUID

data class Purchase(
    val id: String = UUID.randomUUID().toString(),
    val amount: Money,
    val categoryId: String,
    val title: String = "",
    val note: String = "",
    val merchant: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.CARD,
    val occurredAt: Instant = Instant.now(),
    val type: PurchaseType = PurchaseType.EXPENSE,
    val recurringExpenseId: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = createdAt,
) {

    init {
        require(id.isNotBlank()) {
            "Purchase ID cannot be blank."
        }

        require(amount.isPositive) {
            "Purchase amount must be greater than zero."
        }

        require(categoryId.isNotBlank()) {
            "Purchase category ID cannot be blank."
        }

        if (recurringExpenseId != null) {
            require(recurringExpenseId.isNotBlank()) {
                "Recurring expense ID cannot be blank."
            }
        }

        require(!updatedAt.isBefore(createdAt)) {
            "Purchase update time cannot be before creation time."
        }
    }
}
