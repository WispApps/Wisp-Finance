package com.wisp.finance.domain.model

import java.time.Instant

enum class AssetKind { INVESTMENT, CRYPTO }

data class AssetHolding(
    val id: String,
    val kind: AssetKind,
    val name: String,
    val symbol: String,
    val quantity: Double,
    val averageCostMinorUnits: Long,
    val currentPriceMinorUnits: Long,
    val currency: CurrencyCode,
    val note: String,
    val archived: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    val costBasisMinorUnits: Long
        get() = (quantity * averageCostMinorUnits).toLong()

    val currentValueMinorUnits: Long
        get() = (quantity * currentPriceMinorUnits).toLong()

    val profitLossMinorUnits: Long
        get() = currentValueMinorUnits - costBasisMinorUnits
}
