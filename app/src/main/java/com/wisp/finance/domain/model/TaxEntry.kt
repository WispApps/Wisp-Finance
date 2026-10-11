package com.wisp.finance.domain.model

import java.time.Instant
import java.time.LocalDate

enum class TaxEntryType { INCOME, EXPENSE, DEDUCTION }

data class TaxEntry(
    val id: String,
    val type: TaxEntryType,
    val title: String,
    val amountMinorUnits: Long,
    val currency: CurrencyCode,
    val taxYear: Int,
    val occurredOn: LocalDate,
    val note: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)
