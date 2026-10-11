package com.wisp.finance.ui.budgets

import com.wisp.finance.domain.model.Budget
import com.wisp.finance.domain.model.BudgetPeriod
import com.wisp.finance.domain.model.Purchase
import com.wisp.finance.domain.model.PurchaseType
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale

data class BudgetProgress(
    val spentMinorUnits: Long,
    val limitMinorUnits: Long,
) {
    val fraction: Float
        get() = if (limitMinorUnits <= 0L) 0f
        else (spentMinorUnits.toDouble() / limitMinorUnits.toDouble()).toFloat()

    val percent: Int
        get() = (fraction * 100f).toInt()
}

fun calculateBudgetProgress(
    budget: Budget,
    purchases: List<Purchase>,
    locale: Locale,
    zoneId: ZoneId = ZoneId.systemDefault(),
    today: LocalDate = LocalDate.now(zoneId),
): BudgetProgress {
    val (startDate, endDate) = currentPeriodRange(budget.period, today, locale)
    val start = startDate.atStartOfDay(zoneId).toInstant()
    val endExclusive = endDate.plusDays(1).atStartOfDay(zoneId).toInstant()

    val spent = purchases.asSequence()
        .filter { it.amount.currency == budget.amount.currency }
        .filter { budget.categoryId == null || it.categoryId == budget.categoryId }
        .filter { !it.occurredAt.isBefore(start) && it.occurredAt.isBefore(endExclusive) }
        .fold(0L) { total, purchase ->
            total + when (purchase.type) {
                PurchaseType.EXPENSE -> purchase.amount.minorUnits
                PurchaseType.REFUND -> -purchase.amount.minorUnits
            }
        }
        .coerceAtLeast(0L)

    return BudgetProgress(
        spentMinorUnits = spent,
        limitMinorUnits = budget.amount.minorUnits,
    )
}

private fun currentPeriodRange(
    period: BudgetPeriod,
    today: LocalDate,
    locale: Locale,
): Pair<LocalDate, LocalDate> = when (period) {
    BudgetPeriod.DAILY -> today to today
    BudgetPeriod.WEEKLY -> {
        val firstDay = WeekFields.of(locale).firstDayOfWeek
        val start = today.with(TemporalAdjusters.previousOrSame(firstDay))
        start to start.plusDays(6)
    }
    BudgetPeriod.MONTHLY -> today.withDayOfMonth(1) to today.withDayOfMonth(today.lengthOfMonth())
    BudgetPeriod.YEARLY -> today.withDayOfYear(1) to today.withDayOfYear(today.lengthOfYear())
}
