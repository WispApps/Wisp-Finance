package com.wisp.finance.ui.recurring

import com.wisp.finance.ui.common.currentAppLocale

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.wisp.finance.R
import com.wisp.finance.domain.model.*
import com.wisp.finance.ui.common.categoryDisplayName
import com.wisp.finance.ui.common.formatDate
import com.wisp.finance.ui.purchases.*
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Composable
fun RecurringExpenseEditorDialog(
    primaryCurrency: CurrencyCode,
    categories: List<Category>,
    existing: RecurringExpense?,
    initialKind: RecurringExpenseKind,
    onDismiss: () -> Unit,
    onSave: (RecurringExpense) -> Unit,
) {
    var title by remember(existing?.id) { mutableStateOf(existing?.title.orEmpty()) }
    var amountText by remember(existing?.id) { mutableStateOf(existing?.amount?.toEditableAmount().orEmpty()) }
    var currencyCode by remember(existing?.id) {
        mutableStateOf(existing?.amount?.currency?.value ?: primaryCurrency.value)
    }
    var categoryId by remember(existing?.id) { mutableStateOf(existing?.categoryId.orEmpty()) }
    var note by remember(existing?.id) { mutableStateOf(existing?.note.orEmpty()) }
    var merchant by remember(existing?.id) { mutableStateOf(existing?.merchant.orEmpty()) }
    var paymentMethod by remember(existing?.id) {
        mutableStateOf(existing?.paymentMethod ?: PaymentMethod.CARD)
    }
    var interval by remember(existing?.id) { mutableIntStateOf(existing?.recurrenceRule?.interval ?: 1) }
    var unit by remember(existing?.id) { mutableStateOf(existing?.recurrenceRule?.unit ?: RecurrenceUnit.MONTH) }
    var nextEpochDay by remember(existing?.id) {
        mutableLongStateOf((existing?.nextOccurrence ?: LocalDate.now()).toEpochDay())
    }
    var kind by remember(existing?.id) { mutableStateOf(existing?.kind ?: initialKind) }

    var showCurrency by remember { mutableStateOf(false) }
    var showCategory by remember { mutableStateOf(false) }
    var showPayment by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf(false) }

    val currency = CurrencyCode.requireCode(currencyCode)
    val nextDate = LocalDate.ofEpochDay(nextEpochDay)
    val context = LocalContext.current
    val locale = currentAppLocale()
    val selectedCategory = categories.firstOrNull { it.id == categoryId }

    if (showCurrency) {
        CurrencySelectionDialog(
            onDismiss = { showCurrency = false },
            onSelected = {
                currencyCode = it.value
                amountText = ""
                formError = false
                showCurrency = false
            },
        )
    }

    if (showPayment) {
        PaymentMethodSelectionDialog(
            onDismiss = { showPayment = false },
            onSelected = {
                paymentMethod = it
                showPayment = false
            },
        )
    }

    if (showCategory) {
        AlertDialog(
            onDismissRequest = { showCategory = false },
            title = { Text(stringResource(R.string.purchase_select_category)) },
            text = {
                Column(
                    modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                ) {
                    categories.forEach { category ->
                        Text(
                            text = categoryDisplayName(category),
                            modifier = Modifier.fillMaxWidth().clickable {
                                categoryId = category.id
                                showCategory = false
                            }.padding(vertical = 14.dp),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategory = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(if (existing == null) R.string.recurring_add else R.string.recurring_edit))
        },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 600.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; formError = false },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.recurring_title)) },
                    singleLine = true,
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it; formError = false },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.purchase_amount)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                )

                RecurringSelector(stringResource(R.string.purchase_currency), currency.value) {
                    showCurrency = true
                }
                RecurringSelector(
                    stringResource(R.string.purchase_category),
                    selectedCategory?.let { categoryDisplayName(it) }
                        ?: stringResource(R.string.purchase_select_category),
                ) { showCategory = true }

                Text(stringResource(R.string.recurring_kind), style = MaterialTheme.typography.labelLarge)
                RecurringExpenseKind.entries.forEach { option ->
                    FilterChip(
                        selected = kind == option,
                        onClick = { kind = option },
                        label = { Text(recurringKindDisplayName(option)) },
                    )
                }

                OutlinedTextField(
                    value = interval.toString(),
                    onValueChange = { interval = it.toIntOrNull()?.coerceIn(1, 999) ?: 1 },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.recurring_interval)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )

                Text(stringResource(R.string.recurring_unit), style = MaterialTheme.typography.labelLarge)
                RecurrenceUnit.entries.forEach { option ->
                    FilterChip(
                        selected = unit == option,
                        onClick = { unit = option },
                        label = { Text(recurrenceUnitDisplayName(option)) },
                    )
                }

                RecurringSelector(
                    stringResource(R.string.recurring_next_date),
                    formatDate(nextDate, locale),
                ) {
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            nextEpochDay = LocalDate.of(year, month + 1, day).toEpochDay()
                        },
                        nextDate.year,
                        nextDate.monthValue - 1,
                        nextDate.dayOfMonth,
                    ).show()
                }

                OutlinedTextField(
                    value = merchant,
                    onValueChange = { merchant = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.purchase_merchant_optional)) },
                    singleLine = true,
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.purchase_note_optional)) },
                    minLines = 2,
                )

                RecurringSelector(
                    stringResource(R.string.purchase_payment_method),
                    paymentMethodDisplayName(paymentMethod),
                ) { showPayment = true }

                if (formError) {
                    Text(
                        text = stringResource(R.string.recurring_form_error),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val money = (parseMoneyInput(amountText, currency) as? MoneyInputResult.Success)?.money
                if (title.trim().isBlank() || categoryId.isBlank() || money == null) {
                    formError = true
                    return@TextButton
                }

                val now = Instant.now()
                onSave(
                    RecurringExpense(
                        id = existing?.id ?: UUID.randomUUID().toString(),
                        amount = money,
                        categoryId = categoryId,
                        title = title.trim(),
                        note = note.trim(),
                        merchant = merchant.trim(),
                        paymentMethod = paymentMethod,
                        recurrenceRule = RecurrenceRule(interval, unit),
                        nextOccurrence = nextDate,
                        kind = kind,
                        enabled = existing?.enabled ?: true,
                        createdAt = existing?.createdAt ?: now,
                        updatedAt = now,
                    )
                )
            }) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
private fun RecurringSelector(label: String, value: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 6.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 2.dp))
    }
}
