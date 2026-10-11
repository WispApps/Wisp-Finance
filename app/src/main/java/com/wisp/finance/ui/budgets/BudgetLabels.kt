package com.wisp.finance.ui.budgets

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wisp.finance.R
import com.wisp.finance.domain.model.BudgetPeriod

@Composable
fun budgetPeriodDisplayName(period: BudgetPeriod): String = stringResource(
    when (period) {
        BudgetPeriod.DAILY -> R.string.budget_period_daily
        BudgetPeriod.WEEKLY -> R.string.budget_period_weekly
        BudgetPeriod.MONTHLY -> R.string.budget_period_monthly
        BudgetPeriod.YEARLY -> R.string.budget_period_yearly
    }
)
