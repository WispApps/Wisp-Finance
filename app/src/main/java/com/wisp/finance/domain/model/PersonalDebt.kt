package com.wisp.finance.domain.model
import java.time.Instant
import java.time.LocalDate

enum class DebtDirection { I_OWE, OWED_TO_ME }

data class PersonalDebt(
    val id: String,
    val personName: String,
    val direction: DebtDirection,
    val originalMinorUnits: Long,
    val remainingMinorUnits: Long,
    val currency: CurrencyCode,
    val dueDate: LocalDate?,
    val annualInterestRate: Double,
    val interestStartDate: LocalDate?,
    val note: String,
    val closed: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)
