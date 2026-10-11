package com.wisp.finance.ui.recurring

import com.wisp.finance.ui.common.currentAppLocale

import com.wisp.finance.ui.common.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wisp.finance.R
import com.wisp.finance.domain.model.RecurringExpense
import com.wisp.finance.ui.common.formatMinorUnits
import androidx.compose.ui.Alignment

@Composable
fun SubscriptionsScreen(
    subscriptions: List<RecurringExpense>,
    onBack: () -> Unit,
    onManage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = currentAppLocale()

    val totals = subscriptions
        .filter { it.enabled }
        .groupBy { it.amount.currency }
        .mapValues { (_, values) -> values.sumOf(::monthlyEquivalentMinorUnits) }
        .toList()
        .sortedBy { it.first.value }

    Column(modifier = modifier.fillMaxSize()) {
        WispScreenHeader(stringResource(R.string.subscriptions_title), onAction = { onManage() },
            modifier = Modifier.padding(horizontal = 20.dp), actionLabel = stringResource(R.string.action_edit), actionIcon = R.drawable.ic_wisp_edit)

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    stringResource(R.string.subscriptions_monthly_estimate_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (totals.isEmpty()) {
                item {
                    WispEmptyState(stringResource(R.string.subscriptions_empty))
                }
            } else {
                items(totals, key = { it.first.value }) { (currency, total) ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(currency.value, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                formatMinorUnits(total, currency, locale),
                                style = MaterialTheme.typography.headlineMedium,
                            )
                            Text(
                                stringResource(R.string.subscriptions_per_month),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            items(subscriptions, key = { it.id }) { subscription ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(subscription.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            formatMinorUnits(
                                subscription.amount.minorUnits,
                                subscription.amount.currency,
                                locale,
                            ),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            stringResource(
                                R.string.recurring_every,
                                subscription.recurrenceRule.interval,
                                recurrenceUnitDisplayName(subscription.recurrenceRule.unit),
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}
