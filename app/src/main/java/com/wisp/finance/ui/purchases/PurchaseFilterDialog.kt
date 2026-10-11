package com.wisp.finance.ui.purchases

import com.wisp.finance.ui.common.currentAppLocale

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.wisp.finance.R
import com.wisp.finance.domain.model.Category
import com.wisp.finance.domain.model.CurrencyCode
import com.wisp.finance.domain.model.PaymentMethod
import com.wisp.finance.domain.model.PurchaseType
import com.wisp.finance.ui.common.categoryDisplayName
import com.wisp.finance.ui.common.formatDate
import java.math.BigDecimal
import java.time.LocalDate

@Composable
fun PurchaseFilterDialog(
    currentFilters: PurchaseFilters,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onApply: (PurchaseFilters) -> Unit,
) {
    var categoryId by remember(currentFilters) {
        mutableStateOf(currentFilters.categoryId)
    }
    var currencyCode by remember(currentFilters) {
        mutableStateOf(currentFilters.currencyCode)
    }
    var paymentMethod by remember(currentFilters) {
        mutableStateOf(currentFilters.paymentMethod)
    }
    var type by remember(currentFilters) {
        mutableStateOf(currentFilters.type)
    }
    var merchantQuery by remember(currentFilters) {
        mutableStateOf(currentFilters.merchantQuery)
    }
    var minText by remember(currentFilters) {
        mutableStateOf(
            filterAmountToText(
                currentFilters.minMinorUnits,
                currentFilters.currencyCode,
            ),
        )
    }
    var maxText by remember(currentFilters) {
        mutableStateOf(
            filterAmountToText(
                currentFilters.maxMinorUnits,
                currentFilters.currencyCode,
            ),
        )
    }
    var startEpochDay by remember(currentFilters) {
        mutableLongStateOf(currentFilters.startEpochDay ?: Long.MIN_VALUE)
    }
    var endEpochDay by remember(currentFilters) {
        mutableLongStateOf(currentFilters.endEpochDay ?: Long.MIN_VALUE)
    }

    var showCategoryDialog by remember {
        mutableStateOf(false)
    }
    var showCurrencyDialog by remember {
        mutableStateOf(false)
    }
    var amountError by remember {
        mutableStateOf(false)
    }

    val locale = currentAppLocale()
    val context = LocalContext.current
    val selectedCategory = categories.firstOrNull {
        it.id == categoryId
    }

    if (showCategoryDialog) {
        FilterCategorySelectionDialog(
            categories = categories,
            onDismiss = {
                showCategoryDialog = false
            },
            onSelected = {
                categoryId = it?.id
                showCategoryDialog = false
            },
        )
    }

    if (showCurrencyDialog) {
        CurrencySelectionDialog(
            onDismiss = {
                showCurrencyDialog = false
            },
            onSelected = {
                currencyCode = it.value
                minText = ""
                maxText = ""
                amountError = false
                showCurrencyDialog = false
            },
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.filters_title))
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                FilterSelector(
                    label = stringResource(R.string.purchase_category),
                    value = selectedCategory?.let {
                        categoryDisplayName(it)
                    } ?: stringResource(R.string.filter_all),
                    onClick = {
                        showCategoryDialog = true
                    },
                )

                FilterSelector(
                    label = stringResource(R.string.purchase_currency),
                    value = currencyCode ?: stringResource(R.string.filter_all),
                    onClick = {
                        showCurrencyDialog = true
                    },
                )

                if (currencyCode != null) {
                    TextButton(
                        onClick = {
                            currencyCode = null
                            minText = ""
                            maxText = ""
                            amountError = false
                        },
                    ) {
                        Text(stringResource(R.string.filter_clear_currency))
                    }
                }

                OutlinedTextField(
                    value = merchantQuery,
                    onValueChange = {
                        merchantQuery = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(stringResource(R.string.filter_merchant))
                    },
                    singleLine = true,
                )

                Text(
                    text = stringResource(R.string.purchase_type),
                    style = MaterialTheme.typography.labelLarge,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PurchaseType.entries.forEach { option ->
                        FilterChip(
                            selected = type == option,
                            onClick = {
                                type = if (type == option) {
                                    null
                                } else {
                                    option
                                }
                            },
                            label = {
                                Text(purchaseTypeDisplayName(option))
                            },
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.purchase_payment_method),
                    style = MaterialTheme.typography.labelLarge,
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    PaymentMethod.entries.forEach { option ->
                        FilterChip(
                            selected = paymentMethod == option,
                            onClick = {
                                paymentMethod = if (paymentMethod == option) {
                                    null
                                } else {
                                    option
                                }
                            },
                            label = {
                                Text(paymentMethodDisplayName(option))
                            },
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.filter_period),
                    style = MaterialTheme.typography.labelLarge,
                )

                FilterSelector(
                    label = stringResource(R.string.filter_from),
                    value = if (startEpochDay == Long.MIN_VALUE) {
                        stringResource(R.string.filter_not_set)
                    } else {
                        formatDate(
                            LocalDate.ofEpochDay(startEpochDay),
                            locale,
                        )
                    },
                    onClick = {
                        val initial = if (startEpochDay == Long.MIN_VALUE) {
                            LocalDate.now()
                        } else {
                            LocalDate.ofEpochDay(startEpochDay)
                        }

                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                startEpochDay = LocalDate
                                    .of(year, month + 1, day)
                                    .toEpochDay()
                            },
                            initial.year,
                            initial.monthValue - 1,
                            initial.dayOfMonth,
                        ).show()
                    },
                )

                if (startEpochDay != Long.MIN_VALUE) {
                    TextButton(
                        onClick = {
                            startEpochDay = Long.MIN_VALUE
                        },
                    ) {
                        Text(stringResource(R.string.filter_clear_from))
                    }
                }

                FilterSelector(
                    label = stringResource(R.string.filter_to),
                    value = if (endEpochDay == Long.MIN_VALUE) {
                        stringResource(R.string.filter_not_set)
                    } else {
                        formatDate(
                            LocalDate.ofEpochDay(endEpochDay),
                            locale,
                        )
                    },
                    onClick = {
                        val initial = if (endEpochDay == Long.MIN_VALUE) {
                            LocalDate.now()
                        } else {
                            LocalDate.ofEpochDay(endEpochDay)
                        }

                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                endEpochDay = LocalDate
                                    .of(year, month + 1, day)
                                    .toEpochDay()
                            },
                            initial.year,
                            initial.monthValue - 1,
                            initial.dayOfMonth,
                        ).show()
                    },
                )

                if (endEpochDay != Long.MIN_VALUE) {
                    TextButton(
                        onClick = {
                            endEpochDay = Long.MIN_VALUE
                        },
                    ) {
                        Text(stringResource(R.string.filter_clear_to))
                    }
                }

                Text(
                    text = stringResource(R.string.filter_amount_range),
                    style = MaterialTheme.typography.labelLarge,
                )

                if (currencyCode == null) {
                    Text(
                        text = stringResource(R.string.filter_amount_requires_currency),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                OutlinedTextField(
                    value = minText,
                    onValueChange = {
                        minText = it
                        amountError = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = currencyCode != null,
                    label = {
                        Text(stringResource(R.string.filter_min_amount))
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                    ),
                    singleLine = true,
                    isError = amountError,
                )

                OutlinedTextField(
                    value = maxText,
                    onValueChange = {
                        maxText = it
                        amountError = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = currencyCode != null,
                    label = {
                        Text(stringResource(R.string.filter_max_amount))
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                    ),
                    singleLine = true,
                    isError = amountError,
                )

                if (amountError) {
                    Text(
                        text = stringResource(R.string.filter_amount_invalid),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val currency = currencyCode?.let(CurrencyCode::fromCode)
                    val min = parseOptionalFilterAmount(
                        input = minText,
                        currency = currency,
                    )
                    val max = parseOptionalFilterAmount(
                        input = maxText,
                        currency = currency,
                    )

                    val invalidRange =
                        min != null &&
                            max != null &&
                            min > max

                    if (
                        (minText.isNotBlank() && min == null) ||
                        (maxText.isNotBlank() && max == null) ||
                        invalidRange
                    ) {
                        amountError = true
                        return@TextButton
                    }

                    onApply(
                        PurchaseFilters(
                            categoryId = categoryId,
                            currencyCode = currencyCode,
                            paymentMethod = paymentMethod,
                            type = type,
                            merchantQuery = merchantQuery.trim(),
                            minMinorUnits = min,
                            maxMinorUnits = max,
                            startEpochDay = startEpochDay
                                .takeUnless { it == Long.MIN_VALUE },
                            endEpochDay = endEpochDay
                                .takeUnless { it == Long.MIN_VALUE },
                        ),
                    )
                },
            ) {
                Text(stringResource(R.string.filter_apply))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onApply(PurchaseFilters())
                },
            ) {
                Text(stringResource(R.string.filter_reset))
            }
        },
    )
}

@Composable
private fun FilterCategorySelectionDialog(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSelected: (Category?) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.purchase_select_category))
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = stringResource(R.string.filter_all),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelected(null)
                        }
                        .padding(vertical = 14.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )

                categories.forEach { category ->
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
private fun FilterSelector(
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

private fun parseOptionalFilterAmount(
    input: String,
    currency: CurrencyCode?,
): Long? {
    if (input.isBlank()) {
        return null
    }

    currency ?: return null

    return when (
        val result = parseMoneyInput(
            input = input,
            currency = currency,
        )
    ) {
        is MoneyInputResult.Success -> result.money.minorUnits
        is MoneyInputResult.Error -> null
    }
}

private fun filterAmountToText(
    minorUnits: Long?,
    currencyCode: String?,
): String {
    if (minorUnits == null || currencyCode == null) {
        return ""
    }

    val currency = CurrencyCode.fromCode(currencyCode)
        ?: return ""

    return BigDecimal
        .valueOf(minorUnits, currency.minorUnitDigits)
        .toPlainString()
}
