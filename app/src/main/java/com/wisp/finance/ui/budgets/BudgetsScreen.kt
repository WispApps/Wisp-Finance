package com.wisp.finance.ui.budgets

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
import com.wisp.finance.domain.model.Budget
import com.wisp.finance.domain.model.Category
import com.wisp.finance.domain.model.CurrencyCode
import com.wisp.finance.domain.model.Purchase
import com.wisp.finance.ui.common.categoryDisplayName
import com.wisp.finance.ui.common.formatMinorUnits
import androidx.compose.ui.Alignment

@Composable
fun BudgetsScreen(
    primaryCurrency: CurrencyCode,
    budgets: List<Budget>,
    purchases: List<Purchase>,
    categories: List<Category>,
    onBack: () -> Unit,
    onSave: (Budget) -> Unit,
    onDelete: (Budget) -> Unit,
    modifier: Modifier = Modifier,
) {
    var editingBudget by remember { mutableStateOf<Budget?>(null) }
    var creatingBudget by remember { mutableStateOf(false) }

    val activeCategories = categories.filterNot(Category::archived)
    val categoriesById = categories.associateBy(Category::id)
    val locale = currentAppLocale()

    if (creatingBudget || editingBudget != null) {
        BudgetEditorDialog(
            primaryCurrency = primaryCurrency,
            categories = activeCategories,
            existingBudget = editingBudget,
            onDismiss = {
                creatingBudget = false
                editingBudget = null
            },
            onSave = {
                onSave(it)
                creatingBudget = false
                editingBudget = null
            },
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        WispScreenHeader(stringResource(R.string.budgets_title), onAction = { creatingBudget = true },
            modifier = Modifier.padding(horizontal = 20.dp))

        if (budgets.isEmpty()) {
            WispEmptyState(stringResource(R.string.budgets_empty), stringResource(R.string.budgets_empty_hint), Modifier.padding(horizontal = 20.dp, vertical = 12.dp))
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(budgets, key = { it.id }) { budget ->
                val progress = calculateBudgetProgress(budget, purchases, locale)

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = budget.categoryId?.let { id ->
                                        categoriesById[id]?.let { categoryDisplayName(it) }
                                    } ?: stringResource(R.string.budget_scope_all),
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    budgetPeriodDisplayName(budget.period),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Switch(
                                checked = budget.enabled,
                                onCheckedChange = { onSave(budget.copy(enabled = it)) },
                            )
                        }

                        Text(
                            text = stringResource(
                                R.string.budget_progress,
                                formatMinorUnits(progress.spentMinorUnits, budget.amount.currency, locale),
                                formatMinorUnits(progress.limitMinorUnits, budget.amount.currency, locale),
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 12.dp),
                        )

                        LinearProgressIndicator(
                            progress = { progress.fraction.coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        )

                        Text(
                            text = stringResource(
                                if (progress.percent >= 100) R.string.budget_exceeded else R.string.budget_used_percent,
                                progress.percent,
                            ),
                            color = if (progress.percent >= 100) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 6.dp),
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            TextButton(onClick = { editingBudget = budget }) {
                                Text(stringResource(R.string.action_edit))
                            }
                            WispDeleteMenu(onDelete = { onDelete(budget) })
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}
