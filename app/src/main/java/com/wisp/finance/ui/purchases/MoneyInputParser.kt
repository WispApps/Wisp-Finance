package com.wisp.finance.ui.purchases

import com.wisp.finance.domain.model.CurrencyCode
import com.wisp.finance.domain.model.Money
import java.math.BigDecimal

enum class MoneyInputError {
    EMPTY,
    INVALID,
    TOO_PRECISE,
    NON_POSITIVE,
    TOO_LARGE,
}

sealed interface MoneyInputResult {

    data class Success(
        val money: Money,
    ) : MoneyInputResult

    data class Error(
        val reason: MoneyInputError,
    ) : MoneyInputResult
}

fun parseMoneyInput(
    input: String,
    currency: CurrencyCode,
): MoneyInputResult {
    val normalized = input
        .trim()
        .replace(" ", "")
        .replace("\u00A0", "")
        .replace(',', '.')

    if (normalized.isBlank()) {
        return MoneyInputResult.Error(MoneyInputError.EMPTY)
    }

    val decimal = runCatching {
        BigDecimal(normalized)
    }.getOrElse {
        return MoneyInputResult.Error(MoneyInputError.INVALID)
    }

    if (decimal.signum() <= 0) {
        return MoneyInputResult.Error(MoneyInputError.NON_POSITIVE)
    }

    val normalizedScale = decimal
        .stripTrailingZeros()
        .scale()
        .coerceAtLeast(0)

    if (normalizedScale > currency.minorUnitDigits) {
        return MoneyInputResult.Error(MoneyInputError.TOO_PRECISE)
    }

    val minorUnits = runCatching {
        decimal
            .movePointRight(currency.minorUnitDigits)
            .longValueExact()
    }.getOrElse {
        return MoneyInputResult.Error(MoneyInputError.TOO_LARGE)
    }

    return MoneyInputResult.Success(
        money = Money(
            minorUnits = minorUnits,
            currency = currency,
        ),
    )
}

fun Money.toEditableAmount(): String {
    return BigDecimal
        .valueOf(minorUnits, currency.minorUnitDigits)
        .toPlainString()
}
