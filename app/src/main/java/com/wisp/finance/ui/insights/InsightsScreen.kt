package com.wisp.finance.ui.insights

import com.wisp.finance.ui.common.currentAppLocale

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wisp.finance.R
import com.wisp.finance.domain.model.Budget
import com.wisp.finance.domain.model.Category
import com.wisp.finance.domain.model.CurrencyCode
import com.wisp.finance.domain.model.Purchase
import com.wisp.finance.ui.common.categoryDisplayName
import androidx.compose.ui.Alignment

@Composable
fun InsightsScreen(
    primaryCurrency: CurrencyCode,
    purchases: List<Purchase>,
    budgets: List<Budget>,
    categories: List<Category>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val insights = buildFinanceInsights(
        purchases = purchases,
        budgets = budgets,
        categories = categories,
        primaryCurrency = primaryCurrency,
        locale = currentAppLocale(),
    )

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.insights_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    stringResource(R.string.insights_local_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (insights.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                stringResource(R.string.insights_all_good),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                stringResource(R.string.insights_all_good_hint),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(insights) { index, insight ->
                    InsightCard(insight = insight, keyIndex = index)
                }
            }
        }
    }
}

@Composable
private fun InsightCard(insight: FinanceInsight, keyIndex: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            when (insight) {
                is FinanceInsight.SpendingIncrease -> {
                    Text(
                        stringResource(R.string.insight_spending_up_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        stringResource(R.string.insight_spending_up_body, insight.percent),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }

                is FinanceInsight.BudgetNearLimit -> {
                    Text(
                        stringResource(R.string.insight_budget_near_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        stringResource(
                            R.string.insight_budget_near_body,
                            insight.category?.let { categoryDisplayName(it) }
                                ?: stringResource(R.string.budget_scope_all),
                            insight.percent,
                        ),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }

                is FinanceInsight.BudgetExceeded -> {
                    Text(
                        stringResource(R.string.insight_budget_over_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Text(
                        stringResource(
                            R.string.insight_budget_over_body,
                            insight.category?.let { categoryDisplayName(it) }
                                ?: stringResource(R.string.budget_scope_all),
                            insight.percent,
                        ),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}
