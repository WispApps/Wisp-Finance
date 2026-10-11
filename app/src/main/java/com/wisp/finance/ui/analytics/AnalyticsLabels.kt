package com.wisp.finance.ui.analytics

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wisp.finance.R

@Composable
fun analyticsPeriodDisplayName(period: AnalyticsPeriod): String = stringResource(
    when (period) {
        AnalyticsPeriod.MONTH -> R.string.analytics_period_month
        AnalyticsPeriod.THREE_MONTHS -> R.string.analytics_period_three_months
        AnalyticsPeriod.YEAR -> R.string.analytics_period_year
        AnalyticsPeriod.ALL_TIME -> R.string.analytics_period_all_time
    }
)
