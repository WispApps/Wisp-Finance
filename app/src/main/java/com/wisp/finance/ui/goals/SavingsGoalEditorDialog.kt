package com.wisp.finance.ui.goals

import com.wisp.finance.ui.common.currentAppLocale

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.wisp.finance.R
import com.wisp.finance.domain.model.CurrencyCode
import com.wisp.finance.domain.model.SavingsGoal
import com.wisp.finance.ui.common.formatDate
import com.wisp.finance.ui.purchases.CurrencySelectionDialog
import com.wisp.finance.ui.purchases.MoneyInputResult
import com.wisp.finance.ui.purchases.parseMoneyInput
import com.wisp.finance.ui.purchases.toEditableAmount
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Composable
fun SavingsGoalEditorDialog(
    primaryCurrency: CurrencyCode,
    existing: SavingsGoal?,
    onDismiss: () -> Unit,
    onSave: (SavingsGoal) -> Unit,
) {
    var title by remember(existing?.id) { mutableStateOf(existing?.title.orEmpty()) }
    var amountText by remember(existing?.id) {
        mutableStateOf(existing?.targetAmount?.toEditableAmount().orEmpty())
    }
    var currencyCode by remember(existing?.id) {
        mutableStateOf(existing?.targetAmount?.currency?.value ?: primaryCurrency.value)
    }
    var deadlineEpochDay by remember(existing?.id) {
        mutableStateOf(existing?.deadline?.toEpochDay())
    }
    var showCurrency by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf(false) }

    val currency = CurrencyCode.requireCode(currencyCode)
    val deadline = deadlineEpochDay?.let(LocalDate::ofEpochDay)
    val context = LocalContext.current
    val locale = currentAppLocale()

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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(if (existing == null) R.string.goal_add else R.string.goal_edit))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; formError = false },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.goal_name)) },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it; formError = false },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.goal_target)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                )
                GoalSelector(
                    label = stringResource(R.string.purchase_currency),
                    value = currency.value,
                ) { showCurrency = true }

                GoalSelector(
                    label = stringResource(R.string.goal_deadline),
                    value = deadline?.let { formatDate(it, locale) }
                        ?: stringResource(R.string.goal_no_deadline),
                ) {
                    val initial = deadline ?: LocalDate.now()
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            deadlineEpochDay = LocalDate.of(year, month + 1, day).toEpochDay()
                        },
                        initial.year,
                        initial.monthValue - 1,
                        initial.dayOfMonth,
                    ).show()
                }

                if (deadline != null) {
                    TextButton(onClick = { deadlineEpochDay = null }) {
                        Text(stringResource(R.string.goal_remove_deadline))
                    }
                }

                if (formError) {
                    Text(
                        stringResource(R.string.goal_form_error),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val money = (parseMoneyInput(amountText, currency) as? MoneyInputResult.Success)?.money
                if (title.trim().isBlank() || money == null) {
                    formError = true
                    return@TextButton
                }

                val now = Instant.now()
                onSave(
                    SavingsGoal(
                        id = existing?.id ?: UUID.randomUUID().toString(),
                        title = title.trim(),
                        targetAmount = money,
                        savedMinorUnits = existing?.savedMinorUnits ?: 0L,
                        deadline = deadline,
                        completed = existing?.completed ?: false,
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
private fun GoalSelector(label: String, value: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 6.dp),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
