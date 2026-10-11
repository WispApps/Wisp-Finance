package com.wisp.finance.domain.model

import java.util.UUID

data class Budget(
    val id: String = UUID.randomUUID().toString(),
    val categoryId: String? = null,
    val amount: Money,
    val period: BudgetPeriod = BudgetPeriod.MONTHLY,
    val enabled: Boolean = true,
) {

    init {
        require(id.isNotBlank()) {
            "Budget ID cannot be blank."
        }

        require(amount.isPositive) {
            "Budget amount must be greater than zero."
        }

        if (categoryId != null) {
            require(categoryId.isNotBlank()) {
                "Budget category ID cannot be blank."
            }
        }
    }
}
