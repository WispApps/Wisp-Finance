package com.wisp.finance.domain.model

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class RecurringExpense(
    val id: String = UUID.randomUUID().toString(),
    val amount: Money,
    val categoryId: String,
    val title: String,
    val note: String = "",
    val merchant: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.CARD,
    val recurrenceRule: RecurrenceRule,
    val nextOccurrence: LocalDate,
    val kind: RecurringExpenseKind = RecurringExpenseKind.REGULAR,
    val enabled: Boolean = true,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = createdAt,
) {
    init {
        require(id.isNotBlank())
        require(amount.isPositive)
        require(categoryId.isNotBlank())
        require(title.isNotBlank())
        require(!updatedAt.isBefore(createdAt))
    }
}
