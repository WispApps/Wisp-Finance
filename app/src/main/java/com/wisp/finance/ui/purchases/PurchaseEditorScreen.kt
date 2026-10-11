package com.wisp.finance.ui.purchases

import com.wisp.finance.ui.common.currentAppLocale

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wisp.finance.R
import com.wisp.finance.domain.model.Category
import com.wisp.finance.domain.model.CurrencyCode
import com.wisp.finance.domain.model.PaymentMethod
import com.wisp.finance.domain.model.Purchase
import com.wisp.finance.domain.model.PurchaseType
import com.wisp.finance.ui.common.categoryDisplayName
import com.wisp.finance.ui.common.formatDate
import com.wisp.finance.ui.common.formatTime
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun PurchaseEditorScreen(
    primaryCurrency: CurrencyCode,
    categories: List<Category>,
    existingPurchase: Purchase?,
    onCancel: () -> Unit,
    onSave: (Purchase) -> Unit,
    onDelete: (() -> Unit)?,
) {
    val initialZoneDateTime = remember(existingPurchase?.id) {
        (existingPurchase?.occurredAt ?: Instant.now()).atZone(ZoneId.systemDefault())
    }

    var amountText by rememberSaveable(existingPurchase?.id) { mutableStateOf(existingPurchase?.amount?.toEditableAmount().orEmpty()) }
    var currencyCode by rememberSaveable(existingPurchase?.id) { mutableStateOf(existingPurchase?.amount?.currency?.value ?: primaryCurrency.value) }
    var categoryId by rememberSaveable(existingPurchase?.id) { mutableStateOf(existingPurchase?.categoryId.orEmpty()) }
    var title by rememberSaveable(existingPurchase?.id) { mutableStateOf(existingPurchase?.title.orEmpty()) }
    var merchant by rememberSaveable(existingPurchase?.id) { mutableStateOf(existingPurchase?.merchant.orEmpty()) }
    var note by rememberSaveable(existingPurchase?.id) { mutableStateOf(existingPurchase?.note.orEmpty()) }
    var paymentMethodName by rememberSaveable(existingPurchase?.id) { mutableStateOf((existingPurchase?.paymentMethod ?: PaymentMethod.CARD).name) }
    var purchaseTypeName by rememberSaveable(existingPurchase?.id) { mutableStateOf((existingPurchase?.type ?: PurchaseType.EXPENSE).name) }
    var dateEpochDay by rememberSaveable(existingPurchase?.id) { mutableLongStateOf(initialZoneDateTime.toLocalDate().toEpochDay()) }
    var hour by rememberSaveable(existingPurchase?.id) { mutableIntStateOf(initialZoneDateTime.hour) }
    var minute by rememberSaveable(existingPurchase?.id) { mutableIntStateOf(initialZoneDateTime.minute) }

    var moneyError by rememberSaveable { mutableStateOf<MoneyInputError?>(null) }
    var categoryError by rememberSaveable { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showPaymentMethodDialog by remember { mutableStateOf(false) }

    val currency = CurrencyCode.requireCode(currencyCode)
    val paymentMethod = PaymentMethod.valueOf(paymentMethodName)
    val purchaseType = PurchaseType.valueOf(purchaseTypeName)
    val selectedCategory = categories.firstOrNull { it.id == categoryId }
    val selectedDate = LocalDate.ofEpochDay(dateEpochDay)
    val locale = currentAppLocale()
    val context = LocalContext.current

    if (showCategoryDialog) {
        CategorySelectionDialog(
            categories = categories,
            onDismiss = { showCategoryDialog = false },
            onSelected = {
                categoryId = it.id
                categoryError = false
                showCategoryDialog = false
            },
        )
    }

    if (showCurrencyDialog) {
        CurrencySelectionDialog(
            onDismiss = { showCurrencyDialog = false },
            onSelected = {
                currencyCode = it.value
                moneyError = null
                showCurrencyDialog = false
            },
        )
    }

    if (showPaymentMethodDialog) {
        PaymentMethodSelectionDialog(
            onDismiss = { showPaymentMethodDialog = false },
            onSelected = {
                paymentMethodName = it.name
                showPaymentMethodDialog = false
            },
        )
    }

    fun savePurchase() {
        val result = parseMoneyInput(input = amountText, currency = currency)
        moneyError = (result as? MoneyInputResult.Error)?.reason
        categoryError = categoryId.isBlank()
        val money = (result as? MoneyInputResult.Success)?.money

        if (money != null && !categoryError) {
            val now = Instant.now()
            val occurredAt = selectedDate.atTime(hour, minute).atZone(ZoneId.systemDefault()).toInstant()
            onSave(
                Purchase(
                    id = existingPurchase?.id ?: java.util.UUID.randomUUID().toString(),
                    amount = money,
                    categoryId = categoryId,
                    title = title.trim(),
                    note = note.trim(),
                    merchant = merchant.trim(),
                    paymentMethod = paymentMethod,
                    occurredAt = occurredAt,
                    type = purchaseType,
                    recurringExpenseId = existingPurchase?.recurringExpenseId,
                    createdAt = existingPurchase?.createdAt ?: now,
                    updatedAt = now,
                ),
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            
            
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(onClick = onCancel) { Text(stringResource(R.string.action_cancel)) }
            Text(
                text = stringResource(if (existingPurchase == null) R.string.purchase_add_title else R.string.purchase_edit_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            TextButton(onClick = { savePurchase() }) { Text(stringResource(R.string.action_save)) }
        }

        PurchaseTypeSegmentedControl(
            selected = purchaseType,
            onSelected = { purchaseTypeName = it.name },
        )

        SectionCard {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilledInput(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        moneyError = null
                    },
                    label = stringResource(R.string.purchase_amount),
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Decimal,
                    isError = moneyError != null,
                    large = true,
                )

                SelectorTile(
                    label = stringResource(R.string.purchase_currency),
                    value = currency.value,
                    onClick = { showCurrencyDialog = true },
                    modifier = Modifier.width(112.dp),
                )
            }

            moneyError?.let {
                Text(
                    text = stringResource(
                        when (it) {
                            MoneyInputError.EMPTY -> R.string.purchase_amount_error_empty
                            MoneyInputError.INVALID -> R.string.purchase_amount_error_invalid
                            MoneyInputError.TOO_PRECISE -> R.string.purchase_amount_error_precision
                            MoneyInputError.NON_POSITIVE -> R.string.purchase_amount_error_positive
                            MoneyInputError.TOO_LARGE -> R.string.purchase_amount_error_large
                        },
                    ),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        SectionCard {
            SelectorTile(
                label = stringResource(R.string.purchase_category),
                value = selectedCategory?.let { categoryDisplayName(it) } ?: stringResource(R.string.purchase_select_category),
                isError = categoryError,
                onClick = { showCategoryDialog = true },
            )
            if (categoryError) {
                Text(
                    text = stringResource(R.string.purchase_category_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        SectionCard {
            FilledInput(
                value = title,
                onValueChange = { title = it },
                label = stringResource(R.string.purchase_title_optional),
                singleLine = true,
            )
            Spacer(Modifier.height(12.dp))
            FilledInput(
                value = merchant,
                onValueChange = { merchant = it },
                label = stringResource(R.string.purchase_merchant_optional),
                singleLine = true,
            )
            Spacer(Modifier.height(12.dp))
            FilledInput(
                value = note,
                onValueChange = { note = it },
                label = stringResource(R.string.purchase_note_optional),
                minLines = 3,
            )
        }

        SectionCard {
            SelectorTile(
                label = stringResource(R.string.purchase_payment_method),
                value = paymentMethodDisplayName(paymentMethod),
                onClick = { showPaymentMethodDialog = true },
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SelectorTile(
                    label = stringResource(R.string.purchase_date),
                    value = formatDate(selectedDate, locale),
                    onClick = {
                        DatePickerDialog(
                            context,
                            { _, year, month, day -> dateEpochDay = LocalDate.of(year, month + 1, day).toEpochDay() },
                            selectedDate.year,
                            selectedDate.monthValue - 1,
                            selectedDate.dayOfMonth,
                        ).show()
                    },
                    modifier = Modifier.weight(1f),
                )
                SelectorTile(
                    label = stringResource(R.string.purchase_time),
                    value = formatTime(hour, minute, locale),
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, selectedHour, selectedMinute ->
                                hour = selectedHour
                                minute = selectedMinute
                            },
                            hour,
                            minute,
                            android.text.format.DateFormat.is24HourFormat(context),
                        ).show()
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        onDelete?.let {
            Button(
                onClick = it,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            ) {
                Text(stringResource(R.string.action_delete))
            }
        }
    }
}

@Composable
private fun PurchaseTypeSegmentedControl(
    selected: PurchaseType,
    onSelected: (PurchaseType) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(modifier = Modifier.padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            PurchaseType.entries.forEach { type ->
                val isSelected = selected == type
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clickable { onSelected(type) },
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(purchaseTypeDisplayName(type), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp), content = content)
    }
}

@Composable
private fun FilledInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    singleLine: Boolean = false,
    minLines: Int = 1,
    large: Boolean = false,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        textStyle = if (large) MaterialTheme.typography.headlineMedium.copy(fontSize = 30.sp) else MaterialTheme.typography.bodyLarge,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = singleLine,
        minLines = minLines,
        isError = isError,
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            errorContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    )
}

@Composable
private fun SelectorTile(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}
