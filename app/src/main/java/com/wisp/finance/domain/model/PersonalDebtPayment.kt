package com.wisp.finance.domain.model

import java.time.Instant
import java.time.LocalDate

data class PersonalDebtPayment(
    val id: String,
    val debtId: String,
    val amountMinorUnits: Long,
    val paidAt: LocalDate,
    val note: String,
    val createdAt: Instant,
)
