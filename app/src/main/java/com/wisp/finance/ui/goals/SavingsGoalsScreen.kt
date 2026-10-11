package com.wisp.finance.ui.goals

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
import com.wisp.finance.domain.model.CurrencyCode
import com.wisp.finance.domain.model.Money
import com.wisp.finance.domain.model.SavingsGoal
import com.wisp.finance.ui.common.formatDate
import com.wisp.finance.ui.common.formatMinorUnits
import java.time.LocalDate
import androidx.compose.ui.Alignment

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun SavingsGoalsScreen(
    primaryCurrency: CurrencyCode,
    goals: List<SavingsGoal>,
    onBack: () -> Unit,
    onSave: (SavingsGoal) -> Unit,
    onDelete: (SavingsGoal) -> Unit,
    onDeposit: (SavingsGoal, Money) -> Unit,
    onWithdraw: (SavingsGoal, Money) -> Unit,
    modifier: Modifier = Modifier,
) {
    var creating by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<SavingsGoal?>(null) }
    var amountGoal by remember { mutableStateOf<SavingsGoal?>(null) }
    var depositing by remember { mutableStateOf(true) }
    var showCompleted by remember { mutableStateOf(false) }

    val locale = currentAppLocale()
    val visibleGoals = goals.filter { it.completed == showCompleted }

    if (creating || editing != null) {
        SavingsGoalEditorDialog(
            primaryCurrency = primaryCurrency,
            existing = editing,
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

    amountGoal?.let { goal ->
        GoalAmountDialog(
            goal = goal,
            depositing = depositing,
            onDismiss = { amountGoal = null },
            onConfirm = { money ->
                if (depositing) onDeposit(goal, money) else onWithdraw(goal, money)
                amountGoal = null
            },
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        WispScreenHeader(stringResource(R.string.goals_title), onAction = { creating = true },
            modifier = Modifier.padding(horizontal = 20.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = !showCompleted,
                onClick = { showCompleted = false },
                label = { Text(stringResource(R.string.goal_active)) },
            )
            FilterChip(
                selected = showCompleted,
                onClick = { showCompleted = true },
                label = { Text(stringResource(R.string.goal_completed)) },
            )
        }

        if (visibleGoals.isEmpty()) {
            WispEmptyState(stringResource(if (showCompleted) R.string.goal_no_completed else R.string.goals_empty), if (showCompleted) null else stringResource(R.string.goals_empty_hint), Modifier.padding(horizontal = 20.dp, vertical = 12.dp))
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(visibleGoals, key = SavingsGoal::id) { goal ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(goal.title, style = MaterialTheme.typography.titleMedium)

                        Text(
                            stringResource(
                                R.string.goal_saved_of_target,
                                formatMinorUnits(
                                    goal.savedMinorUnits,
                                    goal.targetAmount.currency,
                                    locale,
                                ),
                                formatMinorUnits(
                                    goal.targetAmount.minorUnits,
                                    goal.targetAmount.currency,
                                    locale,
                                ),
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 8.dp),
                        )

                        LinearProgressIndicator(
                            progress = { goal.progress.coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        )

                        Text(
                            stringResource(
                                R.string.goal_remaining,
                                formatMinorUnits(
                                    goal.remainingMinorUnits,
                                    goal.targetAmount.currency,
                                    locale,
                                ),
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp),
                        )

                        goal.deadline?.let { deadline ->
                            val overdue = deadline.isBefore(LocalDate.now()) && !goal.completed
                            Text(
                                stringResource(
                                    if (overdue) R.string.goal_deadline_overdue
                                    else R.string.goal_deadline_value,
                                    formatDate(deadline, locale),
                                ),
                                color = if (overdue) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }

                        if (!goal.completed) {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                WispPrimaryButton(onClick = {
                                    depositing = true
                                    amountGoal = goal
                                }, label = stringResource(R.string.goal_add_money))
                                if (goal.savedMinorUnits > 0L) {
                                    TextButton(onClick = {
                                        depositing = false
                                        amountGoal = goal
                                    }) {
                                        Text(stringResource(R.string.goal_withdraw_money))
                                    }
                                }
                            }
                        }

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            TextButton(onClick = {
                                onSave(
                                    goal.copy(
                                        completed = !goal.completed,
                                        updatedAt = java.time.Instant.now(),
                                    )
                                )
                            }) {
                                Text(
                                    stringResource(
                                        if (goal.completed) R.string.goal_reopen
                                        else R.string.goal_mark_completed
                                    )
                                )
                            }
                            TextButton(onClick = { editing = goal }) {
                                Text(stringResource(R.string.action_edit))
                            }
                            WispDeleteMenu(onDelete = { onDelete(goal) })
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}
