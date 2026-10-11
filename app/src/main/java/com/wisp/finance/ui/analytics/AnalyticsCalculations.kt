package com.wisp.finance.ui.analytics

import com.wisp.finance.domain.model.Category
import com.wisp.finance.domain.model.CurrencyCode
import com.wisp.finance.domain.model.Purchase
import com.wisp.finance.domain.model.PurchaseType
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

enum class AnalyticsPeriod {
    MONTH,
    THREE_MONTHS,
    YEAR,
    ALL_TIME,
}

data class AnalyticsRange(
    val startInclusive: LocalDate?,
    val endInclusive: LocalDate,
)

data class CategorySpending(
    val category: Category?,
    val minorUnits: Long,
    val fraction: Float,
)

data class PeriodComparison(
    val currentMinorUnits: Long,
    val previousMinorUnits: Long,
) {
    val changePercent: Int?
        get() {
            if (previousMinorUnits == 0L) return null
            return (((currentMinorUnits - previousMinorUnits).toDouble() /
                previousMinorUnits.toDouble()) * 100.0).toInt()
        }
}

fun analyticsRange(
    period: AnalyticsPeriod,
    today: LocalDate = LocalDate.now(),
): AnalyticsRange = when (period) {
    AnalyticsPeriod.MONTH -> AnalyticsRange(
        startInclusive = today.withDayOfMonth(1),
        endInclusive = today,
    )
    AnalyticsPeriod.THREE_MONTHS -> AnalyticsRange(
        startInclusive = today.minusMonths(2).withDayOfMonth(1),
        endInclusive = today,
    )
    AnalyticsPeriod.YEAR -> AnalyticsRange(
        startInclusive = today.withDayOfYear(1),
        endInclusive = today,
    )
    AnalyticsPeriod.ALL_TIME -> AnalyticsRange(
        startInclusive = null,
        endInclusive = today,
    )
}

fun filteredExpenses(
    purchases: List<Purchase>,
    currency: CurrencyCode,
    range: AnalyticsRange,
    zoneId: ZoneId = ZoneId.systemDefault(),
): List<Purchase> {
    val start = range.startInclusive?.atStartOfDay(zoneId)?.toInstant()
    val endExclusive = range.endInclusive.plusDays(1).atStartOfDay(zoneId).toInstant()

    return purchases.filter { purchase ->
        purchase.amount.currency == currency &&
            purchase.type == PurchaseType.EXPENSE &&
            (start == null || !purchase.occurredAt.isBefore(start)) &&
            purchase.occurredAt.isBefore(endExclusive)
    }
}

fun totalSpent(purchases: List<Purchase>): Long =
    purchases.sumOf { it.amount.minorUnits }

fun categorySpending(
    purchases: List<Purchase>,
    categories: List<Category>,
): List<CategorySpending> {
    val categoriesById = categories.associateBy(Category::id)
    val totals = purchases
        .groupBy(Purchase::categoryId)
        .mapValues { (_, values) -> values.sumOf { it.amount.minorUnits } }
        .toList()
        .sortedByDescending { it.second }

    val maximum = totals.maxOfOrNull { it.second }?.coerceAtLeast(1L) ?: 1L

    return totals.map { (categoryId, amount) ->
        CategorySpending(
            category = categoriesById[categoryId],
            minorUnits = amount,
            fraction = (amount.toDouble() / maximum.toDouble()).toFloat(),
        )
    }
}

fun monthlySpending(
    purchases: List<Purchase>,
    months: Int,
    zoneId: ZoneId = ZoneId.systemDefault(),
    today: LocalDate = LocalDate.now(zoneId),
): List<Pair<YearMonth, Long>> {
    val currentMonth = YearMonth.from(today)
    val byMonth = purchases.groupBy {
        YearMonth.from(it.occurredAt.atZone(zoneId).toLocalDate())
    }

    return (months - 1 downTo 0).map { offset ->
        val month = currentMonth.minusMonths(offset.toLong())
        month to byMonth[month].orEmpty().sumOf { it.amount.minorUnits }
    }
}

fun compareCurrentMonth(
    purchases: List<Purchase>,
    currency: CurrencyCode,
    zoneId: ZoneId = ZoneId.systemDefault(),
    today: LocalDate = LocalDate.now(zoneId),
): PeriodComparison {
    val currentMonth = YearMonth.from(today)
    val previousMonth = currentMonth.minusMonths(1)

    fun amountFor(month: YearMonth): Long {
        val start = month.atDay(1).atStartOfDay(zoneId).toInstant()
        val end = month.plusMonths(1).atDay(1).atStartOfDay(zoneId).toInstant()
        return purchases.asSequence()
            .filter { it.amount.currency == currency && it.type == PurchaseType.EXPENSE }
            .filter { !it.occurredAt.isBefore(start) && it.occurredAt.isBefore(end) }
            .sumOf { it.amount.minorUnits }
    }

    return PeriodComparison(
        currentMinorUnits = amountFor(currentMonth),
        previousMinorUnits = amountFor(previousMonth),
    )
}
