package com.wisp.finance.ui.recurring

import com.wisp.finance.ui.common.currentAppLocale

import com.wisp.finance.ui.common.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wisp.finance.R
import com.wisp.finance.domain.model.Category
import com.wisp.finance.domain.model.CurrencyCode
import com.wisp.finance.domain.model.RecurringExpense
import com.wisp.finance.domain.model.RecurringExpenseKind
import com.wisp.finance.ui.common.categoryDisplayName
import com.wisp.finance.ui.common.formatDate
import com.wisp.finance.ui.common.formatMinorUnits
import java.time.LocalDate
import androidx.compose.ui.Alignment

@Composable
fun RecurringExpensesScreen(
    primaryCurrency: CurrencyCode,
    recurringExpenses: List<RecurringExpense>,
    categories: List<Category>,
    initialKind: RecurringExpenseKind,
    title: String,
    onBack: () -> Unit,
    onSave: (RecurringExpense) -> Unit,
    onDelete: (RecurringExpense) -> Unit,
    onRecordDue: (RecurringExpense) -> Unit,
    modifier: Modifier = Modifier,
) {
    var creating by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<RecurringExpense?>(null) }

    val activeCategories = categories.filterNot(Category::archived)
    val categoriesById = categories.associateBy(Category::id)
    val locale = currentAppLocale()
    val today = LocalDate.now()

    if (creating || editing != null) {
        RecurringExpenseEditorDialog(
            primaryCurrency = primaryCurrency,
            categories = activeCategories,
            existing = editing,
            initialKind = initialKind,
            onDismiss = {
                creating = false
                editing = null
            },
            onSave = {
                onSave(it)
                creating = false
                editing = null
            },
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        WispScreenHeader(title, onAction = { creating = true },
            modifier = Modifier.padding(horizontal = 20.dp))

        if (recurringExpenses.isEmpty()) {
            WispEmptyState(stringResource(R.string.recurring_empty), stringResource(R.string.recurring_empty_hint), Modifier.padding(horizontal = 20.dp, vertical = 12.dp))
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(recurringExpenses, key = { it.id }) { recurring ->
                val due = recurring.enabled && !recurring.nextOccurrence.isAfter(today)

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(recurring.title, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    categoriesById[recurring.categoryId]?.let { categoryDisplayName(it) }.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Switch(
                                checked = recurring.enabled,
                                onCheckedChange = {
                                    onSave(recurring.copy(enabled = it, updatedAt = java.time.Instant.now()))
                                },
                            )
                        }

                        Text(
                            formatMinorUnits(recurring.amount.minorUnits, recurring.amount.currency, locale),
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(top = 10.dp),
                        )
                        Text(
                            stringResource(
                                R.string.recurring_every,
                                recurring.recurrenceRule.interval,
                                recurrenceUnitDisplayName(recurring.recurrenceRule.unit),
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            stringResource(R.string.recurring_next, formatDate(recurring.nextOccurrence, locale)),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (due) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )

                        if (due) {
                            TextButton(onClick = { onRecordDue(recurring) }) {
                                Text(stringResource(R.string.recurring_record_purchase))
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            TextButton(onClick = { editing = recurring }) {
                                Text(stringResource(R.string.action_edit))
                            }
                            WispDeleteMenu(onDelete = { onDelete(recurring) })
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}
