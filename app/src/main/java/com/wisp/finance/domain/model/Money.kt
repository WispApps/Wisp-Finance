package com.wisp.finance.domain.model

data class Money(
    val minorUnits: Long,
    val currency: CurrencyCode,
) : Comparable<Money> {

    init {
        require(minorUnits >= 0) {
            "Money cannot contain negative minor units."
        }
    }

    val isZero: Boolean
        get() = minorUnits == 0L

    val isPositive: Boolean
        get() = minorUnits > 0L

    operator fun plus(other: Money): Money {
        requireSameCurrency(other)

        return copy(
            minorUnits = Math.addExact(minorUnits, other.minorUnits),
        )
    }

    override fun compareTo(other: Money): Int {
        requireSameCurrency(other)
        return minorUnits.compareTo(other.minorUnits)
    }

    private fun requireSameCurrency(other: Money) {
        require(currency == other.currency) {
            "Cannot operate on money values with different currencies."
        }
    }

    companion object {
        fun zero(currency: CurrencyCode): Money {
            return Money(
                minorUnits = 0L,
                currency = currency,
            )
        }
    }
}
