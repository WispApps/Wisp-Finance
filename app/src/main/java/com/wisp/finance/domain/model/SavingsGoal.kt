package com.wisp.finance.domain.model

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class SavingsGoal(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val targetAmount: Money,
    val savedMinorUnits: Long = 0L,
    val deadline: LocalDate? = null,
    val completed: Boolean = false,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = createdAt,
) {
    init {
        require(id.isNotBlank())
        require(title.isNotBlank())
        require(targetAmount.isPositive)
        require(savedMinorUnits >= 0L)
        require(!updatedAt.isBefore(createdAt))
    }

    val progress: Float
        get() = (savedMinorUnits.toDouble() / targetAmount.minorUnits.toDouble())
            .toFloat()
            .coerceAtLeast(0f)

    val remainingMinorUnits: Long
        get() = (targetAmount.minorUnits - savedMinorUnits).coerceAtLeast(0L)
}
