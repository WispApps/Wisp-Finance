package com.wisp.finance.ui.finance

import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.activity.compose.BackHandler
import com.wisp.finance.ui.common.WispPrimaryButton
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wisp.finance.R
import com.wisp.finance.data.backup.WispBackupManager
import com.wisp.finance.domain.model.CurrencyCode
import com.wisp.finance.domain.model.RecurringExpenseKind
import com.wisp.finance.ui.analytics.AnalyticsScreen
import com.wisp.finance.ui.advanced.AdvancedFinanceScreen
import com.wisp.finance.ui.budgets.BudgetsScreen
import com.wisp.finance.ui.calendar.PurchaseCalendarScreen
import com.wisp.finance.ui.categories.CategoriesScreen
import com.wisp.finance.ui.goals.SavingsGoalsScreen
import com.wisp.finance.ui.home.OverviewScreen
import com.wisp.finance.ui.insights.InsightsScreen
import com.wisp.finance.ui.liabilities.LiabilitiesScreen
import com.wisp.finance.ui.more.MoreScreen
import com.wisp.finance.ui.purchases.PurchaseEditorScreen
import com.wisp.finance.ui.purchases.PurchasesScreen
import com.wisp.finance.ui.recurring.RecurringExpensesScreen
import com.wisp.finance.ui.recurring.SubscriptionsScreen
import com.wisp.finance.ui.settings.BackupRestoreScreen
import com.wisp.finance.ui.settings.PrivacyScreen
import com.wisp.finance.ui.settings.SettingsScreen
import kotlinx.coroutines.launch

private enum class MoreDestination {
    NONE,
    LIABILITIES,
    ADVANCED,
    CATEGORIES,
    BUDGETS,
    RECURRING,
    SUBSCRIPTIONS,
    SUBSCRIPTIONS_MANAGE,
    CALENDAR,
    GOALS,
    INSIGHTS,
    SETTINGS,
    PRIVACY,
    BACKUP,
}

@Composable
fun FinanceApp(
    primaryCurrency: CurrencyCode,
    viewModel: FinanceContentViewModel,
    onPrimaryCurrencySelected: (CurrencyCode) -> Unit,
    backupManager: WispBackupManager,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
    ) {
        FinanceAppContent(
            primaryCurrency = primaryCurrency,
            viewModel = viewModel,
            onPrimaryCurrencySelected = onPrimaryCurrencySelected,
            backupManager = backupManager,
        )
    }
}

@Composable
private fun FinanceAppContent(
    primaryCurrency: CurrencyCode,
    viewModel: FinanceContentViewModel,
    onPrimaryCurrencySelected: (CurrencyCode) -> Unit,
    backupManager: WispBackupManager,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var selectedSectionName by rememberSaveable { mutableStateOf(FinanceSection.OVERVIEW.name) }
    var showCreateEditor by rememberSaveable { mutableStateOf(false) }
    var editingPurchaseId by rememberSaveable { mutableStateOf<String?>(null) }
    var moreDestinationName by rememberSaveable { mutableStateOf(MoreDestination.NONE.name) }

    val selectedSection = runCatching {
        FinanceSection.valueOf(selectedSectionName)
    }.getOrDefault(FinanceSection.OVERVIEW)

    val moreDestination = runCatching {
        MoreDestination.valueOf(moreDestinationName)
    }.getOrDefault(MoreDestination.NONE)

    val editingPurchase = state.purchases.firstOrNull { it.id == editingPurchaseId }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val savedMessage = stringResource(R.string.purchase_saved)
    val deletedMessage = stringResource(R.string.purchase_deleted)
    val undoLabel = stringResource(R.string.action_undo)

    val closeEditor = {
        showCreateEditor = false
        editingPurchaseId = null
    }

    // Android system Back follows the visible Wisp Finance hierarchy instead of
    // immediately finishing MainActivity.
    BackHandler(
        enabled = showCreateEditor ||
            editingPurchaseId != null ||
            moreDestination != MoreDestination.NONE ||
            selectedSection != FinanceSection.OVERVIEW,
    ) {
        when {
            showCreateEditor || editingPurchaseId != null -> closeEditor()
            moreDestination == MoreDestination.SUBSCRIPTIONS_MANAGE ->
                moreDestinationName = MoreDestination.SUBSCRIPTIONS.name
            moreDestination == MoreDestination.PRIVACY ||
                moreDestination == MoreDestination.BACKUP ->
                moreDestinationName = MoreDestination.SETTINGS.name
            moreDestination != MoreDestination.NONE ->
                moreDestinationName = MoreDestination.NONE.name
            selectedSection != FinanceSection.OVERVIEW ->
                selectedSectionName = FinanceSection.OVERVIEW.name
        }
    }

    when (moreDestination) {
        MoreDestination.LIABILITIES -> {
            LiabilitiesScreen(
                primaryCurrency = primaryCurrency,
                loans = state.loans,
                loanPayments = state.loanPayments,
                debts = state.personalDebts,
                debtPayments = state.personalDebtPayments,
                onBack = { moreDestinationName = MoreDestination.NONE.name },
                onSaveLoan = viewModel::saveLoan,
                onDeleteLoan = viewModel::deleteLoan,
                onRecordLoanPayment = viewModel::recordLoanPayment,
                onPayOffLoanEarly = viewModel::payOffLoanEarly,
                onSaveDebt = viewModel::savePersonalDebt,
                onDeleteDebt = viewModel::deletePersonalDebt,
                onRecordDebtPayment = viewModel::recordPersonalDebtPayment,
            )
            return
        }


        MoreDestination.ADVANCED -> {
            AdvancedFinanceScreen(
                primaryCurrency = primaryCurrency,
                mortgages = state.mortgages,
                assetHoldings = state.assetHoldings,
                taxEntries = state.taxEntries,
                onBack = { moreDestinationName = MoreDestination.NONE.name },
                onSaveMortgage = viewModel::saveMortgage,
                onDeleteMortgage = viewModel::deleteMortgage,
                onSaveAsset = viewModel::saveAssetHolding,
                onDeleteAsset = viewModel::deleteAssetHolding,
                onSaveTaxEntry = viewModel::saveTaxEntry,
                onDeleteTaxEntry = viewModel::deleteTaxEntry,
            )
            return
        }

        MoreDestination.CATEGORIES -> {
            CategoriesScreen(
                categories = state.categories,
                onBack = { moreDestinationName = MoreDestination.NONE.name },
                onSave = viewModel::saveCategory,
                onSetArchived = viewModel::setCategoryArchived,
                onDelete = viewModel::deleteCategory,
            )
            return
        }

        MoreDestination.BUDGETS -> {
            BudgetsScreen(
                primaryCurrency = primaryCurrency,
                budgets = state.budgets,
                purchases = state.purchases,
                categories = state.categories,
                onBack = { moreDestinationName = MoreDestination.NONE.name },
                onSave = viewModel::saveBudget,
                onDelete = viewModel::deleteBudget,
            )
            return
        }

        MoreDestination.RECURRING -> {
            RecurringExpensesScreen(
                primaryCurrency = primaryCurrency,
                recurringExpenses = state.recurringExpenses.filter {
                    it.kind == RecurringExpenseKind.REGULAR
                },
                categories = state.categories,
                initialKind = RecurringExpenseKind.REGULAR,
                title = stringResource(R.string.recurring_title_section),
                onBack = { moreDestinationName = MoreDestination.NONE.name },
                onSave = viewModel::saveRecurringExpense,
                onDelete = viewModel::deleteRecurringExpense,
                onRecordDue = viewModel::recordRecurringExpense,
            )
            return
        }

        MoreDestination.SUBSCRIPTIONS_MANAGE -> {
            RecurringExpensesScreen(
                primaryCurrency = primaryCurrency,
                recurringExpenses = state.recurringExpenses.filter {
                    it.kind == RecurringExpenseKind.SUBSCRIPTION
                },
                categories = state.categories,
                initialKind = RecurringExpenseKind.SUBSCRIPTION,
                title = stringResource(R.string.subscriptions_title),
                onBack = { moreDestinationName = MoreDestination.SUBSCRIPTIONS.name },
                onSave = viewModel::saveRecurringExpense,
                onDelete = viewModel::deleteRecurringExpense,
                onRecordDue = viewModel::recordRecurringExpense,
            )
            return
        }

        MoreDestination.SUBSCRIPTIONS -> {
            SubscriptionsScreen(
                subscriptions = state.recurringExpenses.filter {
                    it.kind == RecurringExpenseKind.SUBSCRIPTION
                },
                onBack = { moreDestinationName = MoreDestination.NONE.name },
                onManage = { moreDestinationName = MoreDestination.SUBSCRIPTIONS_MANAGE.name },
            )
            return
        }


        MoreDestination.CALENDAR -> {
            PurchaseCalendarScreen(
                primaryCurrency = primaryCurrency,
                purchases = state.purchases,
                categories = state.categories,
                onBack = { moreDestinationName = MoreDestination.NONE.name },
                onPurchaseClick = { editingPurchaseId = it.id },
            )
            if (editingPurchaseId == null) {
                return
            }
        }


        MoreDestination.GOALS -> {
            SavingsGoalsScreen(
                primaryCurrency = primaryCurrency,
                goals = state.savingsGoals,
                onBack = { moreDestinationName = MoreDestination.NONE.name },
                onSave = viewModel::saveSavingsGoal,
                onDelete = viewModel::deleteSavingsGoal,
                onDeposit = viewModel::depositToGoal,
                onWithdraw = viewModel::withdrawFromGoal,
            )
            return
        }

        MoreDestination.INSIGHTS -> {
            InsightsScreen(
                primaryCurrency = primaryCurrency,
                purchases = state.purchases,
                budgets = state.budgets,
                categories = state.categories,
                onBack = { moreDestinationName = MoreDestination.NONE.name },
            )
            return
        }


        MoreDestination.SETTINGS -> {
            SettingsScreen(
                primaryCurrency = primaryCurrency,
                onBack = { moreDestinationName = MoreDestination.NONE.name },
                onCurrencySelected = onPrimaryCurrencySelected,
                onPrivacyClick = { moreDestinationName = MoreDestination.PRIVACY.name },
                onBackupClick = { moreDestinationName = MoreDestination.BACKUP.name },
            )
            return
        }

        MoreDestination.BACKUP -> {
            BackupRestoreScreen(
                backupManager = backupManager,
                onBack = { moreDestinationName = MoreDestination.SETTINGS.name },
            )
            return
        }

        MoreDestination.PRIVACY -> {
            PrivacyScreen(
                onBack = { moreDestinationName = MoreDestination.SETTINGS.name },
            )
            return
        }

        MoreDestination.NONE -> Unit
    }

    if (showCreateEditor || editingPurchase != null) {
        PurchaseEditorScreen(
            primaryCurrency = primaryCurrency,
            categories = state.activeCategories,
            existingPurchase = editingPurchase,
            onCancel = closeEditor,
            onSave = { purchase ->
                viewModel.savePurchase(purchase)
                closeEditor()
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(savedMessage)
                }
            },
            onDelete = editingPurchase?.let { purchase ->
                {
                    viewModel.deletePurchase(purchase)
                    closeEditor()
                    coroutineScope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = deletedMessage,
                            actionLabel = undoLabel,
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.restorePurchase(purchase)
                        }
                    }
                }
            },
        )
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Box(Modifier.navigationBarsPadding()) {
                FinanceNavigationBar(
                    selectedSection = selectedSection,
                    onSectionSelected = { selectedSectionName = it.name },
                )
            }
        },
        floatingActionButton = {
            if (selectedSection == FinanceSection.OVERVIEW ||
                selectedSection == FinanceSection.PURCHASES
            ) {
                WispPrimaryButton(onClick = { showCreateEditor = true })
            }
        },
    ) { innerPadding ->
        when (selectedSection) {
            FinanceSection.OVERVIEW -> OverviewScreen(
                primaryCurrency = primaryCurrency,
                purchases = state.purchases,
                categories = state.categories,
                onPurchaseClick = { editingPurchaseId = it.id },
                modifier = Modifier.padding(innerPadding),
            )

            FinanceSection.PURCHASES -> PurchasesScreen(
                purchases = state.purchases,
                categories = state.categories,
                onPurchaseClick = { editingPurchaseId = it.id },
                modifier = Modifier.padding(innerPadding),
            )

            FinanceSection.ANALYTICS -> AnalyticsScreen(
                primaryCurrency = primaryCurrency,
                purchases = state.purchases,
                categories = state.categories,
                onOpenCalendar = {
                    moreDestinationName = MoreDestination.CALENDAR.name
                },
                modifier = Modifier.padding(innerPadding),
            )

            FinanceSection.MORE -> MoreScreen(
                onLiabilitiesClick = { moreDestinationName = MoreDestination.LIABILITIES.name },
                onAdvancedClick = { moreDestinationName = MoreDestination.ADVANCED.name },
                onCategoriesClick = { moreDestinationName = MoreDestination.CATEGORIES.name },
                onBudgetsClick = { moreDestinationName = MoreDestination.BUDGETS.name },
                onRecurringClick = { moreDestinationName = MoreDestination.RECURRING.name },
                onSubscriptionsClick = { moreDestinationName = MoreDestination.SUBSCRIPTIONS.name },
                onGoalsClick = { moreDestinationName = MoreDestination.GOALS.name },
                onInsightsClick = { moreDestinationName = MoreDestination.INSIGHTS.name },
                onSettingsClick = { moreDestinationName = MoreDestination.SETTINGS.name },
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun FinanceNavigationBar(
    selectedSection: FinanceSection,
    onSectionSelected: (FinanceSection) -> Unit,
) {
    data class Item(
        val section: FinanceSection,
        val iconRes: Int,
        val label: String,
    )

    val items = listOf(
        Item(FinanceSection.OVERVIEW, R.drawable.ic_wisp_home, stringResource(R.string.nav_overview)),
        Item(FinanceSection.PURCHASES, R.drawable.ic_wisp_list, stringResource(R.string.nav_purchases)),
        Item(FinanceSection.ANALYTICS, R.drawable.ic_wisp_chart, stringResource(R.string.nav_analytics)),
        Item(FinanceSection.MORE, R.drawable.ic_wisp_other, stringResource(R.string.nav_more)),
    )

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = item.section == selectedSection,
                onClick = { onSectionSelected(item.section) },
                icon = { Icon(painterResource(item.iconRes), contentDescription = null, modifier = Modifier.size(26.dp)) },
                label = { Text(item.label, fontSize = 13.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}
