package com.wisp.finance.ui.liabilities

import com.wisp.finance.ui.common.currentAppLocale

import com.wisp.finance.ui.common.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wisp.finance.R
import com.wisp.finance.domain.model.CurrencyCode
import com.wisp.finance.domain.model.DebtDirection
import com.wisp.finance.domain.model.Loan
import com.wisp.finance.domain.model.LoanPayment
import com.wisp.finance.domain.model.PersonalDebt
import com.wisp.finance.domain.model.PersonalDebtPayment
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.util.Locale
import java.util.UUID
import androidx.compose.ui.Alignment

@Composable
fun LiabilitiesScreen(
    primaryCurrency: CurrencyCode,
    loans: List<Loan>,
    loanPayments: List<LoanPayment>,
    debts: List<PersonalDebt>,
    debtPayments: List<PersonalDebtPayment>,
    onBack: () -> Unit,
    onSaveLoan: (Loan) -> Unit,
    onDeleteLoan: (Loan) -> Unit,
    onRecordLoanPayment: (Loan, Long, LocalDate, String) -> Unit,
    onPayOffLoanEarly: (Loan) -> Unit,
    onSaveDebt: (PersonalDebt) -> Unit,
    onDeleteDebt: (PersonalDebt) -> Unit,
    onRecordDebtPayment: (PersonalDebt, Long, LocalDate, String) -> Unit,
) {
    var tab by remember { mutableIntStateOf(0) }
    var addLoan by remember { mutableStateOf(false) }
    var addDebt by remember { mutableStateOf(false) }
    var loanPaymentFor by remember { mutableStateOf<Loan?>(null) }
    var debtPaymentFor by remember { mutableStateOf<PersonalDebt?>(null) }
    var scheduleFor by remember { mutableStateOf<Loan?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        WispScreenHeader(if (tab == 0) stringResource(R.string.liabilities_loans_title) else stringResource(R.string.liabilities_debts_title), onAction = { if (tab == 0) addLoan = true else addDebt = true })
        ScrollableTabRow(selectedTabIndex = tab, edgePadding = 0.dp) {
            Tab(tab == 0, { tab = 0 }, text = { Text(stringResource(R.string.liabilities_loans_tab), maxLines = 1, softWrap = false) })
            Tab(tab == 1, { tab = 1 }, text = { Text(stringResource(R.string.liabilities_people_tab), maxLines = 1, softWrap = false) })
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (tab == 0) {
                if (loans.isEmpty()) {
                    item { EmptyCard(stringResource(R.string.liabilities_no_loans_title), stringResource(R.string.liabilities_no_loans_body)) }
                }
                items(loans, key = { it.id }) { loan ->
                    LoanCard(
                        loan = loan,
                        payments = loanPayments.filter { it.loanId == loan.id },
                        onAddPayment = { loanPaymentFor = loan },
                        onShowSchedule = { scheduleFor = loan },
                        onPayOff = { onPayOffLoanEarly(loan) },
                        onDelete = { onDeleteLoan(loan) },
                    )
                }
            } else {
                if (debts.isEmpty()) {
                    item { EmptyCard(stringResource(R.string.liabilities_no_debts_title), stringResource(R.string.liabilities_no_debts_body)) }
                }
                items(debts, key = { it.id }) { debt ->
                    DebtCard(
                        debt = debt,
                        payments = debtPayments.filter { it.debtId == debt.id },
                        onAddPayment = { debtPaymentFor = debt },
                        onDelete = { onDeleteDebt(debt) },
                    )
                }
            }
        }
    }

    if (addLoan) {
        LoanDialog(
            currency = primaryCurrency,
            onDismiss = { addLoan = false },
            onSave = {
                onSaveLoan(it)
                addLoan = false
            },
        )
    }
    if (addDebt) {
        DebtDialog(
            currency = primaryCurrency,
            onDismiss = { addDebt = false },
            onSave = {
                onSaveDebt(it)
                addDebt = false
            },
        )
    }
    loanPaymentFor?.let { loan ->
        PaymentDialog(
            title = stringResource(R.string.loan_payment_dialog_title),
            currency = loan.currency,
            maxMinorUnits = loan.remainingMinorUnits,
            onDismiss = { loanPaymentFor = null },
            onSave = { amount, date, note ->
                onRecordLoanPayment(loan, amount, date, note)
                loanPaymentFor = null
            },
        )
    }
    debtPaymentFor?.let { debt ->
        PaymentDialog(
            title = stringResource(R.string.debt_payment_dialog_title),
            currency = debt.currency,
            maxMinorUnits = debt.remainingMinorUnits,
            onDismiss = { debtPaymentFor = null },
            onSave = { amount, date, note ->
                onRecordDebtPayment(debt, amount, date, note)
                debtPaymentFor = null
            },
        )
    }
    scheduleFor?.let { loan ->
        LoanScheduleDialog(
            loan = loan,
            onDismiss = { scheduleFor = null },
        )
    }
}

@Composable
private fun LoanCard(
    loan: Loan,
    payments: List<LoanPayment>,
    onAddPayment: () -> Unit,
    onShowSchedule: () -> Unit,
    onPayOff: () -> Unit,
    onDelete: () -> Unit,
) {
    val locale = currentAppLocale()
    val paid = (loan.principalMinorUnits - loan.remainingMinorUnits).coerceAtLeast(0L)
    val progress = if (loan.principalMinorUnits > 0L) {
        paid.toFloat() / loan.principalMinorUnits.toFloat()
    } else {
        0f
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(loan.title, style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.liabilities_remaining_value, money(loan.remainingMinorUnits, loan.currency, locale)))
            Text(stringResource(R.string.liabilities_paid_of_value, money(paid, loan.currency, locale), money(loan.principalMinorUnits, loan.currency, locale)))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(stringResource(R.string.loan_rate_term_value, loan.annualInterestRate, loan.termMonths), color = MaterialTheme.colorScheme.onSurfaceVariant)
            loan.monthlyPaymentMinorUnits?.let {
                Text(stringResource(R.string.liabilities_estimated_monthly_value, money(it, loan.currency, locale)), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            loan.nextPaymentDate?.let { Text(stringResource(R.string.liabilities_next_payment_value, it.toString()), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            PaymentHistory(payments = payments, currency = loan.currency)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!loan.closed) {
                    TextButton(onClick = onAddPayment) { Text(stringResource(R.string.action_payment)) }
                    TextButton(onClick = onPayOff) { Text(stringResource(R.string.action_pay_off)) }
                }
                TextButton(onClick = onShowSchedule) { Text(stringResource(R.string.action_schedule)) }
                WispDeleteMenu(onDelete = onDelete)
            }
        }
    }
}

@Composable
private fun DebtCard(
    debt: PersonalDebt,
    payments: List<PersonalDebtPayment>,
    onAddPayment: () -> Unit,
    onDelete: () -> Unit,
) {
    val locale = currentAppLocale()
    val paid = (debt.originalMinorUnits - debt.remainingMinorUnits).coerceAtLeast(0L)
    val progress = if (debt.originalMinorUnits > 0L) {
        paid.toFloat() / debt.originalMinorUnits.toFloat()
    } else {
        0f
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(debt.personName, style = MaterialTheme.typography.titleMedium)
            Text(if (debt.direction == DebtDirection.I_OWE) stringResource(R.string.debt_direction_i_owe) else stringResource(R.string.debt_direction_owed_to_me))
            Text(stringResource(R.string.liabilities_remaining_value, money(debt.remainingMinorUnits, debt.currency, locale)))
            Text(stringResource(R.string.liabilities_paid_of_value, money(paid, debt.currency, locale), money(debt.originalMinorUnits, debt.currency, locale)))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
            )
            debt.dueDate?.let { Text(stringResource(R.string.liabilities_due_value, it.toString()), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            if (debt.annualInterestRate > 0.0) {
                val estimatedInterest = estimatedDebtInterest(debt)
                Text(stringResource(R.string.debt_interest_estimated, debt.annualInterestRate, money(estimatedInterest, debt.currency, locale)), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            DebtPaymentHistory(payments = payments, currency = debt.currency)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!debt.closed) {
                    TextButton(onClick = onAddPayment) { Text(stringResource(R.string.action_payment)) }
                }
                WispDeleteMenu(onDelete = onDelete)
            }
        }
    }
}

@Composable
private fun PaymentHistory(payments: List<LoanPayment>, currency: CurrencyCode) {
    val locale = currentAppLocale()
    if (payments.isEmpty()) {
        Text(stringResource(R.string.liabilities_no_payments), color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(stringResource(R.string.liabilities_history), style = MaterialTheme.typography.labelLarge)
        payments.take(5).forEach { payment ->
            Text(
                "${payment.paidAt}: ${money(payment.amountMinorUnits, currency, locale)}${noteSuffix(payment.note)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DebtPaymentHistory(payments: List<PersonalDebtPayment>, currency: CurrencyCode) {
    val locale = currentAppLocale()
    if (payments.isEmpty()) {
        Text(stringResource(R.string.liabilities_no_payments), color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(stringResource(R.string.liabilities_history), style = MaterialTheme.typography.labelLarge)
        payments.take(5).forEach { payment ->
            Text(
                "${payment.paidAt}: ${money(payment.amountMinorUnits, currency, locale)}${noteSuffix(payment.note)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun noteSuffix(note: String): String = if (note.isBlank()) "" else " · $note"

@Composable
private fun EmptyCard(title: String, body: String) {
    WispEmptyState(title, body)
}

@Composable
private fun LoanDialog(
    currency: CurrencyCode,
    onDismiss: () -> Unit,
    onSave: (Loan) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var months by remember { mutableStateOf("") }
    var next by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val principal = minor(amount, currency)
    val annualRate = rate.toDoubleOrNull()
    val termMonths = months.toIntOrNull()
    val nextDate = dateOrNull(next)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.loan_add_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text(stringResource(R.string.field_name)) }, singleLine = true)
                OutlinedTextField(amount, { amount = it }, label = { Text(stringResource(R.string.field_original_amount_currency, currency.value)) }, singleLine = true)
                OutlinedTextField(rate, { rate = it }, label = { Text(stringResource(R.string.field_annual_interest)) }, singleLine = true)
                OutlinedTextField(months, { months = it }, label = { Text(stringResource(R.string.field_term_months)) }, singleLine = true)
                OutlinedTextField(next, { next = it }, label = { Text(stringResource(R.string.field_next_payment_optional)) }, singleLine = true)
                OutlinedTextField(note, { note = it }, label = { Text(stringResource(R.string.field_note)) })
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotBlank() && principal != null && annualRate != null && termMonths != null && (next.isBlank() || nextDate != null),
                onClick = {
                    val now = Instant.now()
                    val monthly = amortizedPayment(principal!!, annualRate!!, termMonths!!)
                    onSave(
                        Loan(
                            id = UUID.randomUUID().toString(),
                            title = title.trim(),
                            principalMinorUnits = principal,
                            remainingMinorUnits = principal,
                            currency = currency,
                            annualInterestRate = annualRate,
                            termMonths = termMonths,
                            startDate = LocalDate.now(),
                            nextPaymentDate = nextDate,
                            monthlyPaymentMinorUnits = monthly,
                            note = note.trim(),
                            closed = false,
                            createdAt = now,
                            updatedAt = now,
                        )
                    )
                },
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}

@Composable
private fun DebtDialog(
    currency: CurrencyCode,
    onDismiss: () -> Unit,
    onSave: (PersonalDebt) -> Unit,
) {
    var person by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var due by remember { mutableStateOf("") }
    var interestRate by remember { mutableStateOf("") }
    var interestStart by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var direction by remember { mutableStateOf(DebtDirection.I_OWE) }
    val original = minor(amount, currency)
    val dueDate = dateOrNull(due)
    val annualInterest = interestRate.toDoubleOrNull() ?: 0.0
    val interestStartDate = dateOrNull(interestStart)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.debt_add_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row {
                    FilterChip(direction == DebtDirection.I_OWE, { direction = DebtDirection.I_OWE }, { Text(stringResource(R.string.debt_direction_i_owe)) })
                    Spacer(Modifier.width(8.dp))
                    FilterChip(direction == DebtDirection.OWED_TO_ME, { direction = DebtDirection.OWED_TO_ME }, { Text(stringResource(R.string.debt_direction_owed_to_me)) })
                }
                OutlinedTextField(person, { person = it }, label = { Text(stringResource(R.string.field_person)) }, singleLine = true)
                OutlinedTextField(amount, { amount = it }, label = { Text(stringResource(R.string.field_amount_currency, currency.value)) }, singleLine = true)
                OutlinedTextField(due, { due = it }, label = { Text(stringResource(R.string.field_due_optional)) }, singleLine = true)
                OutlinedTextField(interestRate, { interestRate = it }, label = { Text(stringResource(R.string.field_annual_interest_optional)) }, singleLine = true)
                OutlinedTextField(interestStart, { interestStart = it }, label = { Text(stringResource(R.string.field_interest_start_optional)) }, singleLine = true)
                OutlinedTextField(note, { note = it }, label = { Text(stringResource(R.string.field_reason_note)) })
            }
        },
        confirmButton = {
            Button(
                enabled = person.isNotBlank() && original != null && (due.isBlank() || dueDate != null) && (interestStart.isBlank() || interestStartDate != null),
                onClick = {
                    val now = Instant.now()
                    onSave(
                        PersonalDebt(
                            id = UUID.randomUUID().toString(),
                            personName = person.trim(),
                            direction = direction,
                            originalMinorUnits = original!!,
                            remainingMinorUnits = original,
                            currency = currency,
                            dueDate = dueDate,
                            annualInterestRate = annualInterest,
                            interestStartDate = interestStartDate,
                            note = note.trim(),
                            closed = false,
                            createdAt = now,
                            updatedAt = now,
                        )
                    )
                },
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}

@Composable
private fun PaymentDialog(
    title: String,
    currency: CurrencyCode,
    maxMinorUnits: Long,
    onDismiss: () -> Unit,
    onSave: (Long, LocalDate, String) -> Unit,
) {
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now().toString()) }
    var note by remember { mutableStateOf("") }
    val parsedAmount = minor(amount, currency)
    val parsedDate = dateOrNull(date)
    val locale = currentAppLocale()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.liabilities_remaining_value, money(maxMinorUnits, currency, locale)))
                OutlinedTextField(amount, { amount = it }, label = { Text(stringResource(R.string.field_payment_amount_currency, currency.value)) }, singleLine = true)
                OutlinedTextField(date, { date = it }, label = { Text(stringResource(R.string.field_date_iso)) }, singleLine = true)
                OutlinedTextField(note, { note = it }, label = { Text(stringResource(R.string.field_note)) })
            }
        },
        confirmButton = {
            Button(
                enabled = parsedAmount != null && parsedAmount in 1..maxMinorUnits && parsedDate != null,
                onClick = { onSave(parsedAmount!!, parsedDate!!, note.trim()) },
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}

@Composable
private fun LoanScheduleDialog(
    loan: Loan,
    onDismiss: () -> Unit,
) {
    val locale = currentAppLocale()
    val rows = remember(loan) { loanScheduleRows(loan).take(24) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.loan_schedule_title)) },
        text = {
            Column(
                modifier = Modifier.height(420.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (rows.isEmpty()) {
                    Text(stringResource(R.string.loan_schedule_empty))
                } else {
                    rows.forEach { row ->
                        Text(
                            "${row.number}. ${row.date}: ${money(row.paymentMinorUnits, loan.currency, locale)} · remaining ${money(row.remainingMinorUnits, loan.currency, locale)}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close)) } },
    )
}

private data class ScheduleRow(
    val number: Int,
    val date: LocalDate,
    val paymentMinorUnits: Long,
    val remainingMinorUnits: Long,
)

private fun loanScheduleRows(loan: Loan): List<ScheduleRow> {
    val payment = loan.monthlyPaymentMinorUnits ?: return emptyList()
    val firstDate = loan.nextPaymentDate ?: return emptyList()
    if (payment <= 0L || loan.remainingMinorUnits <= 0L) return emptyList()
    val rows = mutableListOf<ScheduleRow>()
    var remaining = loan.remainingMinorUnits
    var date = firstDate
    repeat(360) { index ->
        if (remaining <= 0L) return@repeat
        val amount = payment.coerceAtMost(remaining)
        remaining -= amount
        rows += ScheduleRow(
            number = index + 1,
            date = date,
            paymentMinorUnits = amount,
            remainingMinorUnits = remaining,
        )
        date = date.plusMonths(1)
    }
    return rows
}

private fun estimatedDebtInterest(debt: PersonalDebt): Long {
    if (debt.annualInterestRate <= 0.0 || debt.remainingMinorUnits <= 0L) return 0L
    val start = debt.interestStartDate ?: return 0L
    val days = java.time.temporal.ChronoUnit.DAYS.between(start, LocalDate.now()).coerceAtLeast(0L)
    if (days == 0L) return 0L
    return (debt.remainingMinorUnits.toDouble() * (debt.annualInterestRate / 100.0) * days.toDouble() / 365.0)
        .toLong()
        .coerceAtLeast(0L)
}

private fun minor(text: String, currency: CurrencyCode): Long? = runCatching {
    text.trim()
        .replace(',', '.')
        .toBigDecimal()
        .movePointRight(currency.minorUnitDigits)
        .setScale(0, RoundingMode.HALF_UP)
        .longValueExact()
}.getOrNull()?.takeIf { it > 0 }

private fun dateOrNull(text: String): LocalDate? {
    if (text.isBlank()) return null
    return runCatching { LocalDate.parse(text.trim()) }.getOrNull()
}

private fun amortizedPayment(principal: Long, annualPercent: Double, months: Int): Long? {
    if (months <= 0) return null
    if (annualPercent <= 0) return (principal.toDouble() / months).toLong().coerceAtLeast(1L)
    val rate = annualPercent / 100.0 / 12.0
    return (principal * rate / (1 - Math.pow(1 + rate, -months.toDouble()))).toLong().coerceAtLeast(1L)
}

private fun money(value: Long, currency: CurrencyCode, locale: Locale): String {
    val amount = BigDecimal.valueOf(value, currency.minorUnitDigits)
    return NumberFormat.getCurrencyInstance(locale).apply {
        this.currency = currency.toJavaCurrency()
    }.format(amount)
}
