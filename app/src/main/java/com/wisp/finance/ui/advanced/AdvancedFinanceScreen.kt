package com.wisp.finance.ui.advanced

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wisp.finance.R
import com.wisp.finance.domain.model.AssetHolding
import com.wisp.finance.domain.model.AssetKind
import com.wisp.finance.domain.model.CurrencyCode
import com.wisp.finance.domain.model.Mortgage
import com.wisp.finance.domain.model.TaxEntry
import com.wisp.finance.domain.model.TaxEntryType
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import androidx.compose.ui.Alignment

@Composable
fun AdvancedFinanceScreen(
    primaryCurrency: CurrencyCode,
    mortgages: List<Mortgage>,
    assetHoldings: List<AssetHolding>,
    taxEntries: List<TaxEntry>,
    onBack: () -> Unit,
    onSaveMortgage: (Mortgage) -> Unit,
    onDeleteMortgage: (Mortgage) -> Unit,
    onSaveAsset: (AssetHolding) -> Unit,
    onDeleteAsset: (AssetHolding) -> Unit,
    onSaveTaxEntry: (TaxEntry) -> Unit,
    onDeleteTaxEntry: (TaxEntry) -> Unit,
) {
    var tab by remember { mutableIntStateOf(0) }
    var showMortgageDialog by remember { mutableStateOf(false) }
    var assetKindToAdd by remember { mutableStateOf<AssetKind?>(null) }
    var showTaxDialog by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        WispScreenHeader(stringResource(R.string.advanced_title), onAction = { when (tab) {
                0 -> showMortgageDialog = true
                1 -> assetKindToAdd = AssetKind.INVESTMENT
                2 -> assetKindToAdd = AssetKind.CRYPTO
                else -> showTaxDialog = true
            } })

        ScrollableTabRow(selectedTabIndex = tab, edgePadding = 0.dp) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(stringResource(R.string.advanced_tab_mortgage), maxLines = 1, softWrap = false) })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(stringResource(R.string.advanced_tab_invest), maxLines = 1, softWrap = false) })
            Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text(stringResource(R.string.advanced_tab_crypto), maxLines = 1, softWrap = false) })
            Tab(selected = tab == 3, onClick = { tab = 3 }, text = { Text(stringResource(R.string.advanced_tab_taxes), maxLines = 1, softWrap = false) })
        }

        when (tab) {
            0 -> MortgageTab(mortgages, onDeleteMortgage)
            1 -> AssetsTab(assetHoldings.filter { it.kind == AssetKind.INVESTMENT }, onDeleteAsset)
            2 -> AssetsTab(assetHoldings.filter { it.kind == AssetKind.CRYPTO }, onDeleteAsset)
            else -> TaxesTab(taxEntries, onDeleteTaxEntry)
        }
    }

    if (showMortgageDialog) {
        MortgageDialog(primaryCurrency, onDismiss = { showMortgageDialog = false }) {
            onSaveMortgage(it)
            showMortgageDialog = false
        }
    }

    assetKindToAdd?.let { kind ->
        AssetDialog(
            kind = kind,
            primaryCurrency = primaryCurrency,
            onDismiss = { assetKindToAdd = null },
        ) {
            onSaveAsset(it)
            assetKindToAdd = null
        }
    }

    if (showTaxDialog) {
        TaxEntryDialog(primaryCurrency, onDismiss = { showTaxDialog = false }) {
            onSaveTaxEntry(it)
            showTaxDialog = false
        }
    }
}

@Composable
private fun MortgageTab(mortgages: List<Mortgage>, onDelete: (Mortgage) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (mortgages.isEmpty()) {
            item { EmptyCard(stringResource(R.string.mortgage_empty_title), stringResource(R.string.mortgage_empty_body)) }
        }
        items(mortgages, key = { it.id }) { mortgage ->
            val totalPaid = mortgage.monthlyPaymentMinorUnits * mortgage.termMonths
            val totalInterest = (totalPaid - mortgage.principalMinorUnits).coerceAtLeast(0L)
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(mortgage.title, style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.mortgage_monthly_payment_value, money(mortgage.monthlyPaymentMinorUnits, mortgage.currency)))
                    Text(stringResource(R.string.mortgage_principal_rate_term_value, money(mortgage.principalMinorUnits, mortgage.currency), mortgage.annualInterestRate, mortgage.termMonths))
                    Text(stringResource(R.string.mortgage_total_interest_value, money(totalInterest, mortgage.currency)), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    MortgageSchedulePreview(mortgage)
                    WispDeleteMenu(onDelete = { onDelete(mortgage) })
                }
            }
        }
    }
}

@Composable
private fun MortgageSchedulePreview(mortgage: Mortgage) {
    val rows = remember(mortgage) { mortgagePreviewRows(mortgage).take(6) }
    if (rows.isNotEmpty()) {
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.mortgage_first_payments), style = MaterialTheme.typography.labelLarge)
        rows.forEach { row ->
            Text(
                stringResource(R.string.mortgage_schedule_row, row.month, money(row.interest, mortgage.currency), money(row.principal, mortgage.currency), money(row.remaining, mortgage.currency)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AssetsTab(items: List<AssetHolding>, onDelete: (AssetHolding) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (items.isEmpty()) {
            item { EmptyCard(stringResource(R.string.assets_empty_title), stringResource(R.string.assets_empty_body)) }
        }
        items(items, key = { it.id }) { asset ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("${asset.symbol} · ${asset.name}", style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.asset_quantity_value, asset.quantity.toString()))
                    Text(stringResource(R.string.asset_value_cost_value, money(asset.currentValueMinorUnits, asset.currency), money(asset.costBasisMinorUnits, asset.currency)))
                    Text(stringResource(R.string.asset_profit_loss_value, money(asset.profitLossMinorUnits, asset.currency)), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    WispDeleteMenu(onDelete = { onDelete(asset) })
                }
            }
        }
    }
}

@Composable
private fun TaxesTab(items: List<TaxEntry>, onDelete: (TaxEntry) -> Unit) {
    val summaries = remember(items) {
        items.groupBy { it.taxYear to it.currency }.map { (key, entries) ->
            val income = entries.filter { it.type == TaxEntryType.INCOME }.sumOf { it.amountMinorUnits }
            val expense = entries.filter { it.type == TaxEntryType.EXPENSE }.sumOf { it.amountMinorUnits }
            val deduction = entries.filter { it.type == TaxEntryType.DEDUCTION }.sumOf { it.amountMinorUnits }
            TaxSummary(key.first, key.second, income, expense, deduction)
        }.sortedByDescending { it.taxYear }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (items.isEmpty()) {
            item { EmptyCard(stringResource(R.string.taxes_empty_title), stringResource(R.string.taxes_empty_body)) }
        }
        items(summaries, key = { "${it.taxYear}-${it.currency.value}" }) { summary ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("${summary.taxYear} · ${summary.currency.value}", style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.tax_income_value, money(summary.income, summary.currency)))
                    Text(stringResource(R.string.tax_expenses_value, money(summary.expense, summary.currency)))
                    Text(stringResource(R.string.tax_deductions_value, money(summary.deduction, summary.currency)))
                    Text(stringResource(R.string.tax_base_value, money((summary.income - summary.expense - summary.deduction).coerceAtLeast(0L), summary.currency)), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        items(items, key = { it.id }) { entry ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("${entry.title} · ${taxTypeLabel(entry.type)}", style = MaterialTheme.typography.titleMedium)
                    Text("${money(entry.amountMinorUnits, entry.currency)} · ${entry.occurredOn} · ${entry.taxYear}")
                    if (entry.note.isNotBlank()) Text(entry.note, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    WispDeleteMenu(onDelete = { onDelete(entry) })
                }
            }
        }
    }
}

@Composable
private fun MortgageDialog(primaryCurrency: CurrencyCode, onDismiss: () -> Unit, onSave: (Mortgage) -> Unit) {
    var title by remember { mutableStateOf("") }
    var propertyPrice by remember { mutableStateOf("") }
    var downPayment by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var months by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val principal = ((minor(propertyPrice, primaryCurrency) ?: 0L) - (minor(downPayment, primaryCurrency) ?: 0L)).coerceAtLeast(0L)
    val monthly = amortizedPayment(principal, rate.toDoubleOrNull() ?: 0.0, months.toIntOrNull() ?: 0)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.mortgage_add_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text(stringResource(R.string.field_name)) }, singleLine = true)
                OutlinedTextField(propertyPrice, { propertyPrice = it }, label = { Text(stringResource(R.string.field_property_price_currency, primaryCurrency.value)) }, singleLine = true)
                OutlinedTextField(downPayment, { downPayment = it }, label = { Text(stringResource(R.string.field_down_payment_currency, primaryCurrency.value)) }, singleLine = true)
                OutlinedTextField(rate, { rate = it }, label = { Text(stringResource(R.string.field_annual_interest)) }, singleLine = true)
                OutlinedTextField(months, { months = it }, label = { Text(stringResource(R.string.field_term_months)) }, singleLine = true)
                Text(stringResource(R.string.liabilities_estimated_monthly_value, monthly?.let { money(it, primaryCurrency) } ?: "—"))
                OutlinedTextField(note, { note = it }, label = { Text(stringResource(R.string.field_note)) })
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotBlank() && principal > 0 && monthly != null,
                onClick = {
                    val now = Instant.now()
                    onSave(
                        Mortgage(
                            id = UUID.randomUUID().toString(),
                            title = title.trim(),
                            propertyPriceMinorUnits = minor(propertyPrice, primaryCurrency) ?: 0L,
                            downPaymentMinorUnits = minor(downPayment, primaryCurrency) ?: 0L,
                            principalMinorUnits = principal,
                            currency = primaryCurrency,
                            annualInterestRate = rate.toDoubleOrNull() ?: 0.0,
                            termMonths = months.toIntOrNull() ?: 0,
                            startDate = LocalDate.now(),
                            monthlyPaymentMinorUnits = monthly ?: 0L,
                            note = note.trim(),
                            active = true,
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
private fun AssetDialog(kind: AssetKind, primaryCurrency: CurrencyCode, onDismiss: () -> Unit, onSave: (AssetHolding) -> Unit) {
    var name by remember { mutableStateOf("") }
    var symbol by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var averageCost by remember { mutableStateOf("") }
    var currentPrice by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (kind == AssetKind.CRYPTO) stringResource(R.string.asset_add_crypto_title) else stringResource(R.string.asset_add_investment_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.field_name)) }, singleLine = true)
                OutlinedTextField(symbol, { symbol = it.uppercase() }, label = { Text(stringResource(R.string.field_ticker_symbol)) }, singleLine = true)
                OutlinedTextField(quantity, { quantity = it }, label = { Text(stringResource(R.string.field_quantity)) }, singleLine = true)
                OutlinedTextField(averageCost, { averageCost = it }, label = { Text(stringResource(R.string.field_average_cost_currency, primaryCurrency.value)) }, singleLine = true)
                OutlinedTextField(currentPrice, { currentPrice = it }, label = { Text(stringResource(R.string.field_current_price_currency, primaryCurrency.value)) }, singleLine = true)
                OutlinedTextField(note, { note = it }, label = { Text(stringResource(R.string.field_note)) })
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank() && symbol.isNotBlank() && quantity.toDoubleOrNull() != null && minor(averageCost, primaryCurrency) != null && minor(currentPrice, primaryCurrency) != null,
                onClick = {
                    val now = Instant.now()
                    onSave(
                        AssetHolding(
                            id = UUID.randomUUID().toString(),
                            kind = kind,
                            name = name.trim(),
                            symbol = symbol.trim().uppercase(),
                            quantity = quantity.toDoubleOrNull() ?: 0.0,
                            averageCostMinorUnits = minor(averageCost, primaryCurrency) ?: 0L,
                            currentPriceMinorUnits = minor(currentPrice, primaryCurrency) ?: 0L,
                            currency = primaryCurrency,
                            note = note.trim(),
                            archived = false,
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
private fun TaxEntryDialog(primaryCurrency: CurrencyCode, onDismiss: () -> Unit, onSave: (TaxEntry) -> Unit) {
    var type by remember { mutableStateOf(TaxEntryType.INCOME) }
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var year by remember { mutableStateOf(LocalDate.now().year.toString()) }
    var date by remember { mutableStateOf(LocalDate.now().toString()) }
    var note by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.tax_add_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row {
                    FilterChip(type == TaxEntryType.INCOME, { type = TaxEntryType.INCOME }, { Text(stringResource(R.string.tax_type_income)) })
                    Spacer(Modifier.width(8.dp))
                    FilterChip(type == TaxEntryType.EXPENSE, { type = TaxEntryType.EXPENSE }, { Text(stringResource(R.string.tax_type_expense)) })
                }
                FilterChip(type == TaxEntryType.DEDUCTION, { type = TaxEntryType.DEDUCTION }, { Text(stringResource(R.string.tax_type_deduction)) })
                OutlinedTextField(title, { title = it }, label = { Text(stringResource(R.string.field_title)) }, singleLine = true)
                OutlinedTextField(amount, { amount = it }, label = { Text(stringResource(R.string.field_amount_currency, primaryCurrency.value)) }, singleLine = true)
                OutlinedTextField(year, { year = it }, label = { Text(stringResource(R.string.field_tax_year)) }, singleLine = true)
                OutlinedTextField(date, { date = it }, label = { Text(stringResource(R.string.field_date_iso)) }, singleLine = true)
                OutlinedTextField(note, { note = it }, label = { Text(stringResource(R.string.field_note)) })
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotBlank() && minor(amount, primaryCurrency) != null && year.toIntOrNull() != null && runCatching { LocalDate.parse(date) }.isSuccess,
                onClick = {
                    val now = Instant.now()
                    onSave(
                        TaxEntry(
                            id = UUID.randomUUID().toString(),
                            type = type,
                            title = title.trim(),
                            amountMinorUnits = minor(amount, primaryCurrency) ?: 0L,
                            currency = primaryCurrency,
                            taxYear = year.toIntOrNull() ?: LocalDate.now().year,
                            occurredOn = runCatching { LocalDate.parse(date) }.getOrDefault(LocalDate.now()),
                            note = note.trim(),
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
private fun EmptyCard(title: String, body: String) {
    WispEmptyState(title, body)
}

private data class MortgageRow(val month: Int, val interest: Long, val principal: Long, val remaining: Long)
@Composable
private fun taxTypeLabel(type: TaxEntryType): String = when (type) {
    TaxEntryType.INCOME -> stringResource(R.string.tax_type_income)
    TaxEntryType.EXPENSE -> stringResource(R.string.tax_type_expense)
    TaxEntryType.DEDUCTION -> stringResource(R.string.tax_type_deduction)
}

private data class TaxSummary(val taxYear: Int, val currency: CurrencyCode, val income: Long, val expense: Long, val deduction: Long)

private fun mortgagePreviewRows(mortgage: Mortgage): List<MortgageRow> {
    val monthlyRate = mortgage.annualInterestRate / 100.0 / 12.0
    var remaining = mortgage.principalMinorUnits
    return (1..mortgage.termMonths.coerceAtMost(12)).map { month ->
        val interest = (remaining * monthlyRate).toLong().coerceAtLeast(0L)
        val principal = (mortgage.monthlyPaymentMinorUnits - interest).coerceIn(0L, remaining)
        remaining = (remaining - principal).coerceAtLeast(0L)
        MortgageRow(month, interest, principal, remaining)
    }
}

private fun minor(text: String, currency: CurrencyCode): Long? = runCatching {
    text.trim().replace(',', '.').toBigDecimal().movePointRight(currency.minorUnitDigits).setScale(0, RoundingMode.HALF_UP).longValueExact()
}.getOrNull()?.takeIf { it >= 0L }

private fun amortizedPayment(principal: Long, annualPercent: Double, months: Int): Long? {
    if (principal <= 0L || months <= 0) return null
    if (annualPercent <= 0.0) return (principal.toDouble() / months).toLong()
    val monthlyRate = annualPercent / 100.0 / 12.0
    return (principal * monthlyRate / (1 - Math.pow(1 + monthlyRate, -months.toDouble()))).toLong()
}

private fun money(value: Long, currency: CurrencyCode): String =
    BigDecimal.valueOf(value).movePointLeft(currency.minorUnitDigits).toPlainString() + " " + currency.value
