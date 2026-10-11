package com.wisp.finance.ui.common

import com.wisp.finance.domain.model.CurrencyCode
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

fun formatMinorUnits(
    minorUnits: Long,
    currency: CurrencyCode,
    locale: Locale,
): String {
    val amount = BigDecimal.valueOf(
        minorUnits,
        currency.minorUnitDigits,
    )

    return NumberFormat
        .getCurrencyInstance(locale)
        .apply {
            this.currency = currency.toJavaCurrency()
        }
        .format(amount)
}

fun formatDate(
    date: LocalDate,
    locale: Locale,
): String {
    return DateTimeFormatter
        .ofLocalizedDate(FormatStyle.MEDIUM)
        .withLocale(locale)
        .format(date)
}

fun formatTime(
    hour: Int,
    minute: Int,
    locale: Locale,
): String {
    return java.time.LocalTime
        .of(hour, minute)
        .format(
            DateTimeFormatter
                .ofLocalizedTime(FormatStyle.SHORT)
                .withLocale(locale),
        )
}

fun Instant.toLocalDate(
    zoneId: ZoneId = ZoneId.systemDefault(),
): LocalDate {
    return atZone(zoneId).toLocalDate()
}
