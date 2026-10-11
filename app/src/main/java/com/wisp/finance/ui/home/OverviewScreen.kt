package com.wisp.finance.ui.home

import com.wisp.finance.ui.common.currentAppLocale

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wisp.finance.R
import com.wisp.finance.domain.model.Category
import com.wisp.finance.domain.model.CurrencyCode
import com.wisp.finance.domain.model.Purchase
import com.wisp.finance.domain.model.PurchaseType
import com.wisp.finance.ui.common.formatMinorUnits
import com.wisp.finance.ui.purchases.PurchaseListItem
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun OverviewScreen(
    primaryCurrency: CurrencyCode,
    purchases: List<Purchase>,
    categories: List<Category>,
    onPurchaseClick: (Purchase) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = currentAppLocale()
    val zoneId = ZoneId.systemDefault()
    val now = LocalDate.now(zoneId)
    val monthStart = now.withDayOfMonth(1)
        .atStartOfDay(zoneId)
        .toInstant()
    val nextMonthStart = now
        .plusMonths(1)
        .withDayOfMonth(1)
        .atStartOfDay(zoneId)
        .toInstant()

    val categoriesById = remember(categories) {
        categories.associateBy { it.id }
    }

    val currentMonthPrimaryPurchases = purchases.filter {
        it.amount.currency == primaryCurrency &&
            !it.occurredAt.isBefore(monthStart) &&
            it.occurredAt.isBefore(nextMonthStart)
    }

    val netMinorUnits = currentMonthPrimaryPurchases.fold(0L) { total, purchase ->
        val signed = when (purchase.type) {
            PurchaseType.EXPENSE -> purchase.amount.minorUnits
            PurchaseType.REFUND -> -purchase.amount.minorUnits
        }

        total + signed
    }

    val recentPurchases = purchases.take(5)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.overview_title),
                style = MaterialTheme.typography.headlineMedium,
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Text(
                        text = stringResource(R.string.overview_this_month),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Text(
                        text = formatMinorUnits(
                            minorUnits = netMinorUnits,
                            currency = primaryCurrency,
                            locale = locale,
                        ),
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
            }
        }

        if (recentPurchases.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        WalletEmptyState()
                    }
                }
            }
        } else {
            item {
                Text(
                    text = stringResource(R.string.overview_recent_purchases),
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            items(
                items = recentPurchases,
                key = { it.id },
            ) { purchase ->
                PurchaseListItem(
                    purchase = purchase,
                    category = categoriesById[purchase.categoryId],
                    locale = locale,
                    onClick = { onPurchaseClick(purchase) },
                )
            }
        }

        item {
            Text(
                text = stringResource(
                    R.string.overview_primary_currency,
                    primaryCurrency.value,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WalletEmptyState(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "wallet-empty")
    val alpha by transition.animateFloat(
        initialValue = 0.42f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "wallet-alpha",
    )

    val bodyColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
    val flapColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
    val accentColor = MaterialTheme.colorScheme.primary
    val detailColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.32f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .alpha(alpha),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Canvas(modifier = Modifier.size(width = 176.dp, height = 132.dp)) {
            drawRoundRect(
                color = bodyColor,
                topLeft = Offset(18f, 34f),
                size = Size(size.width - 36f, size.height - 48f),
                cornerRadius = CornerRadius(28f, 28f),
            )
            drawRoundRect(
                color = flapColor,
                topLeft = Offset(34f, 12f),
                size = Size(size.width - 68f, 52f),
                cornerRadius = CornerRadius(22f, 22f),
            )
            drawRoundRect(
                color = accentColor.copy(alpha = 0.15f),
                topLeft = Offset(size.width * 0.58f, 56f),
                size = Size(size.width * 0.28f, 34f),
                cornerRadius = CornerRadius(18f, 18f),
            )
            drawCircle(
                color = accentColor,
                radius = 5f,
                center = Offset(size.width * 0.79f, 73f),
            )
            drawLine(
                color = detailColor,
                start = Offset(size.width * 0.22f, 102f),
                end = Offset(size.width * 0.78f, 102f),
                strokeWidth = 6f,
                cap = StrokeCap.Round,
            )
        }
    }
}
