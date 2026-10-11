package com.wisp.finance.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.wisp.finance.data.local.entity.*

@Dao
interface BackupDao {
    @Query("SELECT * FROM categories")
    suspend fun categories(): List<CategoryEntity>

    @Query("SELECT * FROM purchases")
    suspend fun purchases(): List<PurchaseEntity>

    @Query("SELECT * FROM budgets")
    suspend fun budgets(): List<BudgetEntity>

    @Query("SELECT * FROM recurring_expenses")
    suspend fun recurringExpenses(): List<RecurringExpenseEntity>

    @Query("SELECT * FROM savings_goals")
    suspend fun savingsGoals(): List<SavingsGoalEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCategories(items: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPurchases(items: List<PurchaseEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBudgets(items: List<BudgetEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRecurringExpenses(items: List<RecurringExpenseEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSavingsGoals(items: List<SavingsGoalEntity>)

    @Query("DELETE FROM purchases")
    suspend fun clearPurchases()

    @Query("DELETE FROM budgets")
    suspend fun clearBudgets()

    @Query("DELETE FROM recurring_expenses")
    suspend fun clearRecurringExpenses()

    @Query("DELETE FROM savings_goals")
    suspend fun clearSavingsGoals()

    @Query("DELETE FROM categories")
    suspend fun clearCategories()

    @Transaction
    suspend fun replaceAll(
        categories: List<CategoryEntity>,
        purchases: List<PurchaseEntity>,
        budgets: List<BudgetEntity>,
        recurringExpenses: List<RecurringExpenseEntity>,
        savingsGoals: List<SavingsGoalEntity>,
    ) {
        clearPurchases()
        clearBudgets()
        clearRecurringExpenses()
        clearSavingsGoals()
        clearCategories()

        insertCategories(categories)
        insertRecurringExpenses(recurringExpenses)
        insertPurchases(purchases)
        insertBudgets(budgets)
        insertSavingsGoals(savingsGoals)
    }
    @Query("SELECT * FROM loans")
    suspend fun loans(): List<LoanEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertLoans(items: List<LoanEntity>)

    @Query("DELETE FROM loans")
    suspend fun clearLoans()

    @Query("SELECT * FROM loan_payments")
    suspend fun loanPayments(): List<LoanPaymentEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertLoanPayments(items: List<LoanPaymentEntity>)

    @Query("DELETE FROM loan_payments")
    suspend fun clearLoanPayments()

    @Query("SELECT * FROM personal_debts")
    suspend fun personalDebts(): List<PersonalDebtEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPersonalDebts(items: List<PersonalDebtEntity>)

    @Query("DELETE FROM personal_debts")
    suspend fun clearPersonalDebts()

    @Query("SELECT * FROM personal_debt_payments")
    suspend fun personalDebtPayments(): List<PersonalDebtPaymentEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPersonalDebtPayments(items: List<PersonalDebtPaymentEntity>)

    @Query("DELETE FROM personal_debt_payments")
    suspend fun clearPersonalDebtPayments()

    @Query("SELECT * FROM mortgages")
    suspend fun mortgages(): List<MortgageEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMortgages(items: List<MortgageEntity>)

    @Query("DELETE FROM mortgages")
    suspend fun clearMortgages()

    @Query("SELECT * FROM asset_holdings")
    suspend fun assetHoldings(): List<AssetHoldingEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAssetHoldings(items: List<AssetHoldingEntity>)

    @Query("DELETE FROM asset_holdings")
    suspend fun clearAssetHoldings()

    @Query("SELECT * FROM tax_entries")
    suspend fun taxEntries(): List<TaxEntryEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTaxEntries(items: List<TaxEntryEntity>)

    @Query("DELETE FROM tax_entries")
    suspend fun clearTaxEntries()

    @Transaction
    suspend fun replaceSnapshot(snapshot: com.wisp.finance.data.backup.FinanceSnapshot) {
        clearTaxEntries()
        clearAssetHoldings()
        clearMortgages()
        clearPersonalDebtPayments()
        clearPersonalDebts()
        clearLoanPayments()
        clearLoans()
        clearSavingsGoals()
        clearBudgets()
        clearPurchases()
        clearRecurringExpenses()
        clearCategories()
        insertCategories(snapshot.categories)
        insertRecurringExpenses(snapshot.recurringExpenses)
        insertPurchases(snapshot.purchases)
        insertBudgets(snapshot.budgets)
        insertSavingsGoals(snapshot.savingsGoals)
        insertLoans(snapshot.loans)
        insertLoanPayments(snapshot.loanPayments)
        insertPersonalDebts(snapshot.personalDebts)
        insertPersonalDebtPayments(snapshot.personalDebtPayments)
        insertMortgages(snapshot.mortgages)
        insertAssetHoldings(snapshot.assetHoldings)
        insertTaxEntries(snapshot.taxEntries)
    }
}
