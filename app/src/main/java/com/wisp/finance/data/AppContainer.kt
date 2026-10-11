package com.wisp.finance.data

import android.content.Context
import com.wisp.finance.data.local.database.WispFinanceDatabaseProvider
import com.wisp.finance.data.repository.PreferencesSettingsRepository
import com.wisp.finance.data.repository.RoomBudgetRepository
import com.wisp.finance.data.repository.RoomCategoryRepository
import com.wisp.finance.data.repository.RoomPurchaseRepository
import com.wisp.finance.data.repository.RoomRecurringExpenseRepository
import com.wisp.finance.data.repository.RoomSavingsGoalRepository
import com.wisp.finance.domain.repository.BudgetRepository
import com.wisp.finance.domain.repository.CategoryRepository
import com.wisp.finance.domain.repository.PurchaseRepository
import com.wisp.finance.domain.repository.RecurringExpenseRepository
import com.wisp.finance.domain.repository.SavingsGoalRepository
import com.wisp.finance.domain.repository.SettingsRepository

@Deprecated("Use com.wisp.finance.AppContainer")
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
}
