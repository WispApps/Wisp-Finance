package com.wisp.finance.ui.goals

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.wisp.finance.R
import com.wisp.finance.domain.model.Money
import com.wisp.finance.domain.model.SavingsGoal
import com.wisp.finance.ui.purchases.MoneyInputResult
import com.wisp.finance.ui.purchases.parseMoneyInput

@Composable
fun GoalAmountDialog(
    goal: SavingsGoal,
    depositing: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Money) -> Unit,
) {
    var amountText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(
                    if (depositing) R.string.goal_add_money else R.string.goal_withdraw_money
                )
            )
        },
        text = {
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it; error = false },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.purchase_amount)) },
                suffix = { Text(goal.targetAmount.currency.value) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                isError = error,
            )
        },
        confirmButton = {
            TextButton(onClick = {
                val money = (
                    parseMoneyInput(amountText, goal.targetAmount.currency)
                        as? MoneyInputResult.Success
                    )?.money

                if (money == null) {
                    error = true
                } else {
                    onConfirm(money)
                }
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
