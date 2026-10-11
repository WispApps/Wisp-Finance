package com.wisp.finance.ui.recurring

import com.wisp.finance.domain.model.RecurrenceRule
import com.wisp.finance.domain.model.RecurrenceUnit
import com.wisp.finance.domain.model.RecurringExpense
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

fun nextOccurrence(current: LocalDate, rule: RecurrenceRule): LocalDate = when (rule.unit) {
    RecurrenceUnit.DAY -> current.plusDays(rule.interval.toLong())
    RecurrenceUnit.WEEK -> current.plusWeeks(rule.interval.toLong())
    RecurrenceUnit.MONTH -> current.plusMonths(rule.interval.toLong())
    RecurrenceUnit.YEAR -> current.plusYears(rule.interval.toLong())
}

fun monthlyEquivalentMinorUnits(recurringExpense: RecurringExpense): Long {
    val interval = BigDecimal(recurringExpense.recurrenceRule.interval)
    val amount = BigDecimal(recurringExpense.amount.minorUnits)

    val multiplier = when (recurringExpense.recurrenceRule.unit) {
        RecurrenceUnit.DAY -> BigDecimal("365.2425")
            .divide(BigDecimal("12"), 12, RoundingMode.HALF_UP)
            .divide(interval, 12, RoundingMode.HALF_UP)

        RecurrenceUnit.WEEK -> BigDecimal("365.2425")
            .divide(BigDecimal("7"), 12, RoundingMode.HALF_UP)
            .divide(BigDecimal("12"), 12, RoundingMode.HALF_UP)
            .divide(interval, 12, RoundingMode.HALF_UP)

        RecurrenceUnit.MONTH -> BigDecimal.ONE
            .divide(interval, 12, RoundingMode.HALF_UP)

        RecurrenceUnit.YEAR -> BigDecimal.ONE
            .divide(BigDecimal("12").multiply(interval), 12, RoundingMode.HALF_UP)
    }

    return amount.multiply(multiplier)
        .setScale(0, RoundingMode.HALF_UP)
        .longValueExact()
}
