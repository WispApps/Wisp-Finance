package com.wisp.finance.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.wisp.finance.data.local.dao.BackupDao
import com.wisp.finance.data.local.dao.BudgetDao
import com.wisp.finance.data.local.dao.LoanDao
import com.wisp.finance.data.local.dao.PersonalDebtDao
import com.wisp.finance.data.local.dao.CategoryDao
import com.wisp.finance.data.local.dao.PurchaseDao
import com.wisp.finance.data.local.dao.RecurringExpenseDao
import com.wisp.finance.data.local.dao.SavingsGoalDao
import com.wisp.finance.data.local.entity.BudgetEntity
import com.wisp.finance.data.local.entity.LoanEntity
import com.wisp.finance.data.local.entity.LoanPaymentEntity
import com.wisp.finance.data.local.entity.PersonalDebtEntity
import com.wisp.finance.data.local.entity.PersonalDebtPaymentEntity
import com.wisp.finance.data.local.entity.CategoryEntity
import com.wisp.finance.data.local.entity.PurchaseEntity
import com.wisp.finance.data.local.entity.RecurringExpenseEntity
import com.wisp.finance.data.local.entity.SavingsGoalEntity
import com.wisp.finance.data.local.entity.TaxEntryEntity
import com.wisp.finance.data.local.entity.AssetHoldingEntity
import com.wisp.finance.data.local.entity.MortgageEntity
import com.wisp.finance.data.local.dao.TaxEntryDao
import com.wisp.finance.data.local.dao.AssetHoldingDao
import com.wisp.finance.data.local.dao.MortgageDao

@Database(
    entities = [
        CategoryEntity::class,
        PurchaseEntity::class,
        BudgetEntity::class,
        RecurringExpenseEntity::class,
        SavingsGoalEntity::class,
        LoanEntity::class,
        LoanPaymentEntity::class,
        PersonalDebtEntity::class,
        PersonalDebtPaymentEntity::class,
        MortgageEntity::class,
        AssetHoldingEntity::class,
        TaxEntryEntity::class,
    ],
    version = 6,
    exportSchema = true,
)
@TypeConverters(DatabaseConverters::class)
abstract class WispFinanceDatabase : RoomDatabase() {
    abstract fun backupDao(): BackupDao
    abstract fun categoryDao(): CategoryDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun budgetDao(): BudgetDao
    abstract fun recurringExpenseDao(): RecurringExpenseDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun loanDao(): LoanDao
    abstract fun personalDebtDao(): PersonalDebtDao
    abstract fun mortgageDao(): MortgageDao
    abstract fun assetHoldingDao(): AssetHoldingDao
    abstract fun taxEntryDao(): TaxEntryDao
}
