package com.wisp.finance.ui.purchases

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wisp.finance.R

@Composable
fun PurchaseSortDialog(
    currentSort: PurchaseSort,
    hasCurrencyFilter: Boolean,
    onDismiss: () -> Unit,
    onSelected: (PurchaseSort) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.sort_title))
        },
        text = {
            Column {
                PurchaseSort.entries.forEach { option ->
                    val amountSort =
                        option == PurchaseSort.AMOUNT_HIGH ||
                            option == PurchaseSort.AMOUNT_LOW

                    val enabled = !amountSort || hasCurrencyFilter

                    Text(
                        text = sortDisplayName(option),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = enabled) {
                                onSelected(option)
                            }
                            .padding(vertical = 14.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (enabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        },
                    )
                }

                if (!hasCurrencyFilter) {
                    Text(
                        text = stringResource(
                            R.string.sort_amount_requires_currency,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
fun sortDisplayName(
    sort: PurchaseSort,
): String {
    return stringResource(
        when (sort) {
            PurchaseSort.NEWEST -> R.string.sort_newest
            PurchaseSort.OLDEST -> R.string.sort_oldest
            PurchaseSort.AMOUNT_HIGH -> R.string.sort_amount_high
            PurchaseSort.AMOUNT_LOW -> R.string.sort_amount_low
            PurchaseSort.TITLE -> R.string.sort_title_name
        },
    )
}
