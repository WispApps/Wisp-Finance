package com.wisp.finance.ui.purchases

import com.wisp.finance.ui.common.currentAppLocale

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wisp.finance.R
import com.wisp.finance.domain.model.Category
import com.wisp.finance.domain.model.CurrencyCode
import com.wisp.finance.domain.model.PaymentMethod
import com.wisp.finance.ui.common.categoryDisplayName
import java.util.Currency

@Composable
fun CategorySelectionDialog(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSelected: (Category) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.purchase_select_category))
        },
        text = {
            if (categories.isEmpty()) {
                Text(stringResource(R.string.purchase_no_categories))
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 420.dp),
                ) {
                    items(
                        items = categories,
                        key = { it.id },
                    ) { category ->
                        Text(
                            text = categoryDisplayName(category),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelected(category)
                                }
                                .padding(vertical = 14.dp),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
fun CurrencySelectionDialog(
    onDismiss: () -> Unit,
    onSelected: (CurrencyCode) -> Unit,
) {
    var query by remember {
        mutableStateOf("")
    }

    val locale = currentAppLocale()
    val currencies = remember(query, locale) {
        val normalized = query.trim().lowercase(locale)

        CurrencyCode.available()
            .map { code ->
                val currency = code.toJavaCurrency()
                CurrencyRow(
                    code = code,
                    displayName = currency.getDisplayName(locale),
                    symbol = currency.getSymbol(locale),
                )
            }
            .filter { row ->
                normalized.isBlank() ||
                    row.code.value.lowercase(locale).contains(normalized) ||
                    row.displayName.lowercase(locale).contains(normalized) ||
                    row.symbol.lowercase(locale).contains(normalized)
            }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.purchase_select_currency))
        },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(stringResource(R.string.search_currency))
                    },
                    singleLine = true,
                )

                LazyColumn(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .heightIn(max = 420.dp),
                ) {
                    items(
                        items = currencies,
                        key = { it.code.value },
                    ) { row ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelected(row.code)
                                }
                                .padding(vertical = 12.dp),
                        ) {
                            Text(
                                text = "${row.code.value} · ${row.symbol}",
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                text = row.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
fun PaymentMethodSelectionDialog(
    onDismiss: () -> Unit,
    onSelected: (PaymentMethod) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.purchase_payment_method))
        },
        text = {
            Column {
                PaymentMethod.entries.forEach { paymentMethod ->
                    Text(
                        text = paymentMethodDisplayName(paymentMethod),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelected(paymentMethod)
                            }
                            .padding(vertical = 14.dp),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

private data class CurrencyRow(
    val code: CurrencyCode,
    val displayName: String,
    val symbol: String,
)
