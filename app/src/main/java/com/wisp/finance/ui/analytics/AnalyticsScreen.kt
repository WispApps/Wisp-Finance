package com.wisp.finance.ui.analytics

import com.wisp.finance.ui.common.currentAppLocale

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wisp.finance.R
import com.wisp.finance.domain.model.Category
import com.wisp.finance.domain.model.CurrencyCode
import com.wisp.finance.domain.model.Purchase
import com.wisp.finance.ui.common.categoryDisplayName
import com.wisp.finance.ui.common.formatMinorUnits
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun AnalyticsScreen(
    primaryCurrency: CurrencyCode,
    purchases: List<Purchase>,
    categories: List<Category>,
    onOpenCalendar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var periodName by remember { mutableStateOf(AnalyticsPeriod.MONTH.name) }
    val period = runCatching { AnalyticsPeriod.valueOf(periodName) }
        .getOrDefault(AnalyticsPeriod.MONTH)

    val locale = currentAppLocale()
    val range = analyticsRange(period)
    val expenses = remember(purchases, primaryCurrency, period) {
        filteredExpenses(purchases, primaryCurrency, range)
    }
    val total = remember(expenses) { totalSpent(expenses) }
    val byCategory = remember(expenses, categories) {
        categorySpending(expenses, categories)
    }
    val comparison = remember(purchases, primaryCurrency) {
        compareCurrentMonth(purchases, primaryCurrency)
    }
    val monthly = remember(purchases, primaryCurrency) {
        val allExpenses = filteredExpenses(
            purchases = purchases,
            currency = primaryCurrency,
            range = AnalyticsRange(null, java.time.LocalDate.now()),
        )
        monthlySpending(allExpenses, months = 6)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        stringResource(R.string.analytics_title),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        primaryCurrency.value,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                TextButton(onClick = onOpenCalendar) {
                    Text(stringResource(R.string.calendar_title))
                }
            }
        }

        item {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                AnalyticsPeriod.entries.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = period == option,
                        onClick = { periodName = option.name },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = AnalyticsPeriod.entries.size,
                        ),
                        label = { Text(analyticsPeriodDisplayName(option)) },
                    )
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        stringResource(R.string.analytics_total_spent),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        formatMinorUnits(total, primaryCurrency, locale),
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Text(
                        text = if (range.startInclusive == null) {
                            stringResource(R.string.analytics_period_all_time)
                        } else {
                            val formatter = DateTimeFormatter
                                .ofLocalizedDate(FormatStyle.MEDIUM)
                                .withLocale(locale)
                            stringResource(
                                R.string.analytics_range,
                                formatter.format(range.startInclusive),
                                formatter.format(range.endInclusive),
                            )
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        stringResource(R.string.analytics_month_comparison),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        formatMinorUnits(
                            comparison.currentMinorUnits,
                            primaryCurrency,
                            locale,
                        ),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    val change = comparison.changePercent
                    Text(
                        text = when {
                            change == null -> stringResource(R.string.analytics_no_previous_data)
                            change > 0 -> stringResource(R.string.analytics_more_than_previous, change)
                            change < 0 -> stringResource(R.string.analytics_less_than_previous, -change)
                            else -> stringResource(R.string.analytics_same_as_previous)
                        },
                        color = if (change != null && change > 0) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        }

        item {
            Text(
                stringResource(R.string.analytics_categories),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        if (byCategory.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.analytics_no_data),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(byCategory, key = { it.category?.id ?: "unknown" }) { item ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            item.category?.let { categoryDisplayName(it) }
                                ?: stringResource(R.string.analytics_unknown_category),
                            modifier = Modifier.weight(1f),
                        )
                        Text(formatMinorUnits(item.minorUnits, primaryCurrency, locale))
                    }
                    LinearProgressIndicator(
                        progress = { item.fraction.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    )
                }
            }
        }

        item {
            Text(
                stringResource(R.string.analytics_six_months),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        val maxMonth = monthly.maxOfOrNull { it.second }?.coerceAtLeast(1L) ?: 1L
        items(monthly, key = { it.first.toString() }) { (month, amount) ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        month.format(
                            DateTimeFormatter.ofPattern("MMM yyyy", locale)
                        )
                    )
                    Text(formatMinorUnits(amount, primaryCurrency, locale))
                }
                LinearProgressIndicator(
                    progress = {
                        (amount.toDouble() / maxMonth.toDouble())
                            .toFloat()
                            .coerceIn(0f, 1f)
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                )
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}
