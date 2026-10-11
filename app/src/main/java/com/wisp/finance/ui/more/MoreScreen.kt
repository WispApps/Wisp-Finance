package com.wisp.finance.ui.more

import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wisp.finance.R

@Composable
fun MoreScreen(
    onLiabilitiesClick: () -> Unit,
    onAdvancedClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    onBudgetsClick: () -> Unit,
    onRecurringClick: () -> Unit,
    onSubscriptionsClick: () -> Unit,
    onGoalsClick: () -> Unit,
    onInsightsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            
            ,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.more_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        item {
            MoreSection(
                title = stringResource(R.string.more_section_finance_management),
                items = listOf(
                    MoreItemSpec(R.drawable.ic_wisp_grid, stringResource(R.string.categories_title), onCategoriesClick),
                    MoreItemSpec(R.drawable.ic_wisp_budget, stringResource(R.string.budgets_title), onBudgetsClick),
                    MoreItemSpec(R.drawable.ic_wisp_goal, stringResource(R.string.goals_title), onGoalsClick),
                    MoreItemSpec(R.drawable.ic_wisp_loans, stringResource(R.string.more_liabilities_title), onLiabilitiesClick),
                    MoreItemSpec(R.drawable.ic_wisp_chart, stringResource(R.string.more_advanced_title), onAdvancedClick),
                ),
            )
        }

        item {
            MoreSection(
                title = stringResource(R.string.more_section_regular_operations),
                items = listOf(
                    MoreItemSpec(R.drawable.ic_wisp_card, stringResource(R.string.subscriptions_title), onSubscriptionsClick),
                    MoreItemSpec(R.drawable.ic_wisp_subscriptions, stringResource(R.string.recurring_title_section), onRecurringClick),
                ),
            )
        }

        item {
            MoreSection(
                title = stringResource(R.string.more_section_application),
                items = listOf(
                    MoreItemSpec(R.drawable.ic_wisp_chart, stringResource(R.string.insights_title), onInsightsClick),
                    MoreItemSpec(R.drawable.ic_wisp_settings, stringResource(R.string.settings_title), onSettingsClick),
                ),
            )
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

private data class MoreItemSpec(
    val icon: Int,
    val title: String,
    val onClick: () -> Unit,
)

@Composable
private fun MoreSection(
    title: String,
    items: List<MoreItemSpec>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.8.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    MoreRow(item = item)
                    if (index != items.lastIndex) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(start = 78.dp)
                                .height(1.dp)
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.75f),
                            ) {}
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoreRow(item: MoreItemSpec) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = item.onClick)
            .padding(horizontal = 18.dp, vertical = 17.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(15.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(painterResource(item.icon), contentDescription = null, modifier = Modifier.size(24.dp))
            }
        }

        Text(
            text = item.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )

        Icon(painterResource(R.drawable.ic_wisp_chevron), null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
