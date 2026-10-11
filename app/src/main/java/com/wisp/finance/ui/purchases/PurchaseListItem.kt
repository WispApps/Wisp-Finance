package com.wisp.finance.ui.purchases

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wisp.finance.domain.model.Category
import com.wisp.finance.domain.model.Purchase
import com.wisp.finance.domain.model.PurchaseType
import com.wisp.finance.ui.common.categoryDisplayName
import com.wisp.finance.ui.common.formatMinorUnits
import java.util.Locale

@Composable
fun PurchaseListItem(
    purchase: Purchase,
    category: Category?,
    locale: Locale,
    onClick: () -> Unit,
) {
    val categoryName = category?.let {
        categoryDisplayName(it)
    }.orEmpty()

    val displayTitle = purchase.title
        .ifBlank { purchase.merchant }
        .ifBlank { categoryName }

    val formattedAmount = formatMinorUnits(
        minorUnits = purchase.amount.minorUnits,
        currency = purchase.amount.currency,
        locale = locale,
    )

    val signedAmount = when (purchase.type) {
        PurchaseType.EXPENSE -> "−$formattedAmount"
        PurchaseType.REFUND -> "+$formattedAmount"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.titleMedium,
                )

                if (categoryName.isNotBlank() && categoryName != displayTitle) {
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }

            Text(
                text = signedAmount,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
    }
}
