package com.wisp.finance.domain.model

import java.time.Instant
import java.time.LocalDate

data class Mortgage(
    val id: String,
    val title: String,
    val propertyPriceMinorUnits: Long,
    val downPaymentMinorUnits: Long,
    val principalMinorUnits: Long,
    val currency: CurrencyCode,
    val annualInterestRate: Double,
    val termMonths: Int,
    val startDate: LocalDate,
    val monthlyPaymentMinorUnits: Long,
    val note: String,
    val active: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)
