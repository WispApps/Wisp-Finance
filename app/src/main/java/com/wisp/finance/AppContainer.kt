package com.wisp.finance

import android.content.Context
import com.wisp.finance.data.backup.WispBackupManager
import com.wisp.finance.data.local.database.WispFinanceDatabaseProvider
import com.wisp.finance.data.repository.PreferencesSettingsRepository
import com.wisp.finance.data.repository.RoomBudgetRepository
import com.wisp.finance.data.repository.RoomCategoryRepository
import com.wisp.finance.data.repository.RoomPurchaseRepository
import com.wisp.finance.data.repository.RoomRecurringExpenseRepository
import com.wisp.finance.data.repository.RoomSavingsGoalRepository
import com.wisp.finance.data.repository.RoomTaxEntryRepository
import com.wisp.finance.data.repository.RoomAssetHoldingRepository
import com.wisp.finance.data.repository.RoomMortgageRepository
import com.wisp.finance.data.repository.RoomLoanRepository
import com.wisp.finance.data.repository.RoomPersonalDebtRepository
import com.wisp.finance.domain.repository.BudgetRepository
import com.wisp.finance.domain.repository.CategoryRepository
import com.wisp.finance.domain.repository.PurchaseRepository
import com.wisp.finance.domain.repository.RecurringExpenseRepository
import com.wisp.finance.domain.repository.SavingsGoalRepository
import com.wisp.finance.domain.repository.LoanRepository
import com.wisp.finance.domain.repository.TaxEntryRepository
import com.wisp.finance.domain.repository.AssetHoldingRepository
import com.wisp.finance.domain.repository.MortgageRepository
import com.wisp.finance.domain.repository.PersonalDebtRepository
import com.wisp.finance.domain.repository.SettingsRepository

class AppContainer(context: Context) {
    private val database = WispFinanceDatabaseProvider.get(context)

    val settingsRepository: SettingsRepository =
        PreferencesSettingsRepository(context)

    val categoryRepository: CategoryRepository =
        RoomCategoryRepository(database.categoryDao())

    val purchaseRepository: PurchaseRepository =
        RoomPurchaseRepository(database.purchaseDao())

    val budgetRepository: BudgetRepository =
        RoomBudgetRepository(database.budgetDao())

    val recurringExpenseRepository: RecurringExpenseRepository =
        RoomRecurringExpenseRepository(database.recurringExpenseDao())

    val savingsGoalRepository: SavingsGoalRepository =
        RoomSavingsGoalRepository(database.savingsGoalDao())

    val loanRepository: LoanRepository = RoomLoanRepository(database.loanDao())

    val personalDebtRepository: PersonalDebtRepository = RoomPersonalDebtRepository(database.personalDebtDao())

    val mortgageRepository: MortgageRepository = RoomMortgageRepository(database.mortgageDao())

    val assetHoldingRepository: AssetHoldingRepository = RoomAssetHoldingRepository(database.assetHoldingDao())

    val taxEntryRepository: TaxEntryRepository = RoomTaxEntryRepository(database.taxEntryDao())


    val backupManager = WispBackupManager(
        context = context.applicationContext,
        database = database,
        settingsRepository = settingsRepository,
    )
}
