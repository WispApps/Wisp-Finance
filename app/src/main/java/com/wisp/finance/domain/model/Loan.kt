package com.wisp.finance.domain.model
import java.time.Instant
import java.time.LocalDate

data class Loan(
    val id: String,
    val title: String,
    val principalMinorUnits: Long,
    val remainingMinorUnits: Long,
    val currency: CurrencyCode,
    val annualInterestRate: Double,
    val termMonths: Int,
    val startDate: LocalDate,
    val nextPaymentDate: LocalDate?,
    val monthlyPaymentMinorUnits: Long?,
    val note: String,
    val closed: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)
