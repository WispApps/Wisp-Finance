package com.wisp.finance.ui.purchases

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wisp.finance.R
import com.wisp.finance.domain.model.PaymentMethod
import com.wisp.finance.domain.model.PurchaseType

@Composable
fun paymentMethodDisplayName(
    paymentMethod: PaymentMethod,
): String {
    return when (paymentMethod) {
        PaymentMethod.CASH -> stringResource(R.string.payment_cash)
        PaymentMethod.CARD -> stringResource(R.string.payment_card)
        PaymentMethod.BANK_TRANSFER -> stringResource(R.string.payment_bank_transfer)
        PaymentMethod.OTHER -> stringResource(R.string.payment_other)
    }
}

@Composable
fun purchaseTypeDisplayName(
    type: PurchaseType,
): String {
    return when (type) {
        PurchaseType.EXPENSE -> stringResource(R.string.purchase_type_expense)
        PurchaseType.REFUND -> stringResource(R.string.purchase_type_refund)
    }
}
