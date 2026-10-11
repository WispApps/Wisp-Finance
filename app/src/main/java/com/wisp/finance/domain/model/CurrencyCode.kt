package com.wisp.finance.domain.model

import java.util.Currency
import java.util.Locale

data class CurrencyCode private constructor(
    val value: String,
) {

    val minorUnitDigits: Int
        get() = toJavaCurrency().defaultFractionDigits

    fun toJavaCurrency(): Currency {
        return Currency.getInstance(value)
    }

    override fun toString(): String = value

    companion object {

        fun fromCode(code: String): CurrencyCode? {
            val normalized = code
                .trim()
                .uppercase(Locale.ROOT)

            if (normalized.isBlank()) {
                return null
            }

            return runCatching {
                val currency = Currency.getInstance(normalized)

                if (currency.defaultFractionDigits < 0 || currency.currencyCode == "XXX") {
                    null
                } else {
                    CurrencyCode(currency.currencyCode)
                }
            }.getOrNull()
        }

        fun requireCode(code: String): CurrencyCode {
            return requireNotNull(fromCode(code)) {
                "Unsupported ISO 4217 currency code: $code"
            }
        }

        fun available(): List<CurrencyCode> {
            return Currency
                .getAvailableCurrencies()
                .asSequence()
                .filter { it.defaultFractionDigits >= 0 }
                .filterNot { it.currencyCode == "XXX" }
                .map { CurrencyCode(it.currencyCode) }
                .distinct()
                .sortedBy { it.value }
                .toList()
        }
    }
}
