package com.wisp.finance.ui.recurring

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wisp.finance.R
import com.wisp.finance.domain.model.RecurrenceUnit
import com.wisp.finance.domain.model.RecurringExpenseKind

@Composable
fun recurrenceUnitDisplayName(unit: RecurrenceUnit): String = stringResource(
    when (unit) {
        RecurrenceUnit.DAY -> R.string.recurrence_day
        RecurrenceUnit.WEEK -> R.string.recurrence_week
        RecurrenceUnit.MONTH -> R.string.recurrence_month
        RecurrenceUnit.YEAR -> R.string.recurrence_year
    }
)

@Composable
fun recurringKindDisplayName(kind: RecurringExpenseKind): String = stringResource(
    when (kind) {
        RecurringExpenseKind.REGULAR -> R.string.recurring_kind_regular
        RecurringExpenseKind.SUBSCRIPTION -> R.string.recurring_kind_subscription
    }
)
