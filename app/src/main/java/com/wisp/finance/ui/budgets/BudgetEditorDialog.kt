package com.wisp.finance.ui.budgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.wisp.finance.R
import com.wisp.finance.domain.model.Budget
import com.wisp.finance.domain.model.BudgetPeriod
import com.wisp.finance.domain.model.Category
import com.wisp.finance.domain.model.CurrencyCode
import com.wisp.finance.ui.common.categoryDisplayName
import com.wisp.finance.ui.purchases.CurrencySelectionDialog
import com.wisp.finance.ui.purchases.MoneyInputResult
import com.wisp.finance.ui.purchases.parseMoneyInput
import com.wisp.finance.ui.purchases.toEditableAmount
import java.util.UUID

@Composable
fun BudgetEditorDialog(
    primaryCurrency: CurrencyCode,
    categories: List<Category>,
    existingBudget: Budget?,
    onDismiss: () -> Unit,
    onSave: (Budget) -> Unit,
) {
    var amountText by remember(existingBudget?.id) { mutableStateOf(existingBudget?.amount?.toEditableAmount().orEmpty()) }
    var currencyCode by remember(existingBudget?.id) { mutableStateOf(existingBudget?.amount?.currency?.value ?: primaryCurrency.value) }
    var categoryId by remember(existingBudget?.id) { mutableStateOf(existingBudget?.categoryId) }
    var period by remember(existingBudget?.id) { mutableStateOf(existingBudget?.period ?: BudgetPeriod.MONTHLY) }
    var amountError by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }

    val currency = CurrencyCode.requireCode(currencyCode)
    val selectedCategory = categories.firstOrNull { it.id == categoryId }

    if (showCurrencyDialog) {
        CurrencySelectionDialog(
            onDismiss = { showCurrencyDialog = false },
            onSelected = {
                currencyCode = it.value
                amountText = ""
                amountError = false
                showCurrencyDialog = false
            },
        )
    }

    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text(stringResource(R.string.budget_scope)) },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Text(
                        text = stringResource(R.string.budget_scope_all),
                        modifier = Modifier.fillMaxWidth().clickable {
                            categoryId = null
                            showCategoryDialog = false
                        }.padding(vertical = 14.dp),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    categories.forEach { category ->
                        Text(
                            text = categoryDisplayName(category),
                            modifier = Modifier.fillMaxWidth().clickable {
                                categoryId = category.id
                                showCategoryDialog = false
                            }.padding(vertical = 14.dp),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(if (existingBudget == null) R.string.budget_add else R.string.budget_edit))
        },
        text = {
            Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it; amountError = false },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.purchase_amount)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = amountError,
                )
                BudgetSelector(
                    stringResource(R.string.purchase_currency),
                    currency.value,
                ) { showCurrencyDialog = true }
                BudgetSelector(
                    stringResource(R.string.budget_scope),
                    selectedCategory?.let { categoryDisplayName(it) } ?: stringResource(R.string.budget_scope_all),
                ) { showCategoryDialog = true }

                Text(stringResource(R.string.budget_period), style = MaterialTheme.typography.labelLarge)
                BudgetPeriod.entries.forEach { option ->
                    FilterChip(
                        selected = period == option,
                        onClick = { period = option },
                        label = { Text(budgetPeriodDisplayName(option)) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val money = (parseMoneyInput(amountText, currency) as? MoneyInputResult.Success)?.money
                if (money == null) {
                    amountError = true
                    return@TextButton
                }
                onSave(
                    Budget(
                        id = existingBudget?.id ?: UUID.randomUUID().toString(),
                        categoryId = categoryId,
                        amount = money,
                        period = period,
                        enabled = existingBudget?.enabled ?: true,
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
private fun BudgetSelector(label: String, value: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 6.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 2.dp))
    }
}
