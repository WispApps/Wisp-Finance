package com.wisp.finance.ui.insights

import com.wisp.finance.domain.model.Budget
import com.wisp.finance.domain.model.Category
import com.wisp.finance.domain.model.CurrencyCode
import com.wisp.finance.domain.model.Purchase
import com.wisp.finance.ui.analytics.compareCurrentMonth
import com.wisp.finance.ui.budgets.calculateBudgetProgress
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

sealed interface FinanceInsight {
    data class SpendingIncrease(val percent: Int) : FinanceInsight
    data class BudgetNearLimit(
        val budget: Budget,
        val category: Category?,
        val percent: Int,
    ) : FinanceInsight
    data class BudgetExceeded(
        val budget: Budget,
        val category: Category?,
        val percent: Int,
    ) : FinanceInsight
}

fun buildFinanceInsights(
    purchases: List<Purchase>,
    budgets: List<Budget>,
    categories: List<Category>,
    primaryCurrency: CurrencyCode,
    locale: Locale,
    zoneId: ZoneId = ZoneId.systemDefault(),
    today: LocalDate = LocalDate.now(zoneId),
): List<FinanceInsight> {
    val insights = mutableListOf<FinanceInsight>()
    val categoriesById = categories.associateBy(Category::id)

    val comparison = compareCurrentMonth(
        purchases = purchases,
        currency = primaryCurrency,
        zoneId = zoneId,
        today = today,
    )
    val change = comparison.changePercent
    if (change != null && change >= 20) {
        insights += FinanceInsight.SpendingIncrease(change)
    }

    budgets.asSequence()
        .filter(Budget::enabled)
        .forEach { budget ->
            val progress = calculateBudgetProgress(
                budget = budget,
                purchases = purchases,
                locale = locale,
                zoneId = zoneId,
                today = today,
            )
            when {
                progress.percent >= 100 -> insights += FinanceInsight.BudgetExceeded(
                    budget = budget,
                    category = budget.categoryId?.let(categoriesById::get),
                    percent = progress.percent,
                )
                progress.percent >= 80 -> insights += FinanceInsight.BudgetNearLimit(
                    budget = budget,
                    category = budget.categoryId?.let(categoriesById::get),
                    percent = progress.percent,
                )
            }
        }

    return insights.take(8)
}
