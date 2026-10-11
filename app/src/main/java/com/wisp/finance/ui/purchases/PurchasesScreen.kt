package com.wisp.finance.ui.purchases

import com.wisp.finance.ui.common.currentAppLocale

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wisp.finance.R
import com.wisp.finance.domain.model.Category
import com.wisp.finance.domain.model.Purchase
import com.wisp.finance.ui.common.categoryDisplayName
import com.wisp.finance.ui.common.formatDate
import com.wisp.finance.ui.common.toLocalDate

@Composable
fun PurchasesScreen(
    purchases: List<Purchase>,
    categories: List<Category>,
    onPurchaseClick: (Purchase) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = currentAppLocale()
    val categoriesById = remember(categories) {
        categories.associateBy { it.id }
    }

    val categoryNamesById = linkedMapOf<String, String>()
    for (category in categories) {
        categoryNamesById[category.id] = categoryDisplayName(category)
    }

    var searchQuery by rememberSaveable {
        mutableStateOf("")
    }
    var filters by remember {
        mutableStateOf(PurchaseFilters())
    }
    var sort by rememberSaveable {
        mutableStateOf(PurchaseSort.NEWEST)
    }
    var showFilters by remember {
        mutableStateOf(false)
    }
    var showSort by remember {
        mutableStateOf(false)
    }

    if (showFilters) {
        PurchaseFilterDialog(
            currentFilters = filters,
            categories = categories.filterNot(Category::archived),
            onDismiss = {
                showFilters = false
            },
            onApply = {
                filters = it
                if (
                    filters.currencyCode == null &&
                    (
                        sort == PurchaseSort.AMOUNT_HIGH ||
                            sort == PurchaseSort.AMOUNT_LOW
                    )
                ) {
                    sort = PurchaseSort.NEWEST
                }
                showFilters = false
            },
        )
    }

    if (showSort) {
        PurchaseSortDialog(
            currentSort = sort,
            hasCurrencyFilter = filters.currencyCode != null,
            onDismiss = {
                showSort = false
            },
            onSelected = {
                sort = it
                showSort = false
            },
        )
    }

    val visiblePurchases = filterAndSortPurchases(
        purchases = purchases,
        searchQuery = searchQuery,
        filters = filters,
        sort = sort,
        categoryNamesById = categoryNamesById,
        locale = locale,
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = stringResource(R.string.purchases_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(
                top = 20.dp,
                bottom = 12.dp,
            ),
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(stringResource(R.string.search_purchases))
            },
            singleLine = true,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AssistChip(
                onClick = {
                    showFilters = true
                },
                label = {
                    Text(
                        if (filters == PurchaseFilters()) {
                            stringResource(R.string.filters_title)
                        } else {
                            stringResource(R.string.filters_active)
                        },
                    )
                },
            )

            AssistChip(
                onClick = {
                    showSort = true
                },
                label = {
                    Text(sortDisplayName(sort))
                },
            )
        }

        when {
            purchases.isEmpty() -> {
                EmptyPurchasesMessage(
                    title = stringResource(R.string.purchases_empty),
                    message = stringResource(R.string.purchases_empty_hint),
                )
            }

            visiblePurchases.isEmpty() -> {
                EmptyPurchasesMessage(
                    title = stringResource(R.string.search_no_results),
                    message = stringResource(R.string.search_no_results_hint),
                )
            }

            else -> {
                val grouped = visiblePurchases.groupBy {
                    it.occurredAt.toLocalDate()
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    grouped.forEach { (date, datePurchases) ->
                        item(
                            key = "header:$date",
                        ) {
                            Text(
                                text = formatDate(
                                    date = date,
                                    locale = locale,
                                ),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(
                                    top = 10.dp,
                                    bottom = 2.dp,
                                ),
                            )
                        }

                        items(
                            items = datePurchases,
                            key = { it.id },
                        ) { purchase ->
                            PurchaseListItem(
                                purchase = purchase,
                                category = categoriesById[purchase.categoryId],
                                locale = locale,
                                onClick = {
                                    onPurchaseClick(purchase)
                                },
                            )
                        }
                    }

                    item {
                        Text(
                            text = "",
                            modifier = Modifier.padding(bottom = 80.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyPurchasesMessage(
    title: String,
    message: String,
) {
    Column(
        modifier = Modifier.padding(top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
