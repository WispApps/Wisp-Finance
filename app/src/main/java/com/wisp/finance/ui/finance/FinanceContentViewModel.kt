package com.wisp.finance.ui.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wisp.finance.domain.model.*
import com.wisp.finance.domain.repository.*
import com.wisp.finance.ui.recurring.nextOccurrence
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FinanceContentState(
    val purchases: List<Purchase> = emptyList(),
    val categories: List<Category> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val recurringExpenses: List<RecurringExpense> = emptyList(),
    val savingsGoals: List<SavingsGoal> = emptyList(),
    val loans: List<Loan> = emptyList(),
    val loanPayments: List<LoanPayment> = emptyList(),
    val personalDebts: List<PersonalDebt> = emptyList(),
    val personalDebtPayments: List<PersonalDebtPayment> = emptyList(),
    val mortgages: List<Mortgage> = emptyList(),
    val assetHoldings: List<AssetHolding> = emptyList(),
    val taxEntries: List<TaxEntry> = emptyList(),
) {
    val activeCategories: List<Category>
        get() = categories.filterNot(Category::archived)

    val archivedCategories: List<Category>
        get() = categories.filter(Category::archived)
}

private data class LiabilityState(
    val loans: List<Loan> = emptyList(),
    val loanPayments: List<LoanPayment> = emptyList(),
    val personalDebts: List<PersonalDebt> = emptyList(),
    val personalDebtPayments: List<PersonalDebtPayment> = emptyList(),
)

private data class AdvancedFinanceState(
    val mortgages: List<Mortgage> = emptyList(),
    val assetHoldings: List<AssetHolding> = emptyList(),
    val taxEntries: List<TaxEntry> = emptyList(),
)

class FinanceContentViewModel(
    private val purchaseRepository: PurchaseRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository,
    private val recurringExpenseRepository: RecurringExpenseRepository,
    private val savingsGoalRepository: SavingsGoalRepository,
    private val loanRepository: LoanRepository,
    private val personalDebtRepository: PersonalDebtRepository,
    private val mortgageRepository: MortgageRepository,
    private val assetHoldingRepository: AssetHoldingRepository,
    private val taxEntryRepository: TaxEntryRepository,
) : ViewModel() {

    private val coreState = combine(
        purchaseRepository.observeAll(),
        categoryRepository.observeAll(),
        budgetRepository.observeAll(),
        recurringExpenseRepository.observeAll(),
    ) { purchases, categories, budgets, recurringExpenses ->
        FinanceContentState(
            purchases = purchases,
            categories = categories,
            budgets = budgets,
            recurringExpenses = recurringExpenses,
        )
    }

    private val liabilityState = combine(
        loanRepository.observeAll(),
        loanRepository.observePayments(),
        personalDebtRepository.observeAll(),
        personalDebtRepository.observePayments(),
    ) { loans, loanPayments, debts, debtPayments ->
        LiabilityState(
            loans = loans,
            loanPayments = loanPayments,
            personalDebts = debts,
            personalDebtPayments = debtPayments,
        )
    }


    private val advancedFinanceState = combine(
        mortgageRepository.observeAll(),
        assetHoldingRepository.observeAll(),
        taxEntryRepository.observeAll(),
    ) { mortgages, assetHoldings, taxEntries ->
        AdvancedFinanceState(
            mortgages = mortgages,
            assetHoldings = assetHoldings,
            taxEntries = taxEntries,
        )
    }

    val state: StateFlow<FinanceContentState> = combine(
        coreState,
        savingsGoalRepository.observeAll(),
        liabilityState,
        advancedFinanceState,
    ) { core, goals, liabilities, advanced ->
        core.copy(
            savingsGoals = goals,
            loans = liabilities.loans,
            loanPayments = liabilities.loanPayments,
            personalDebts = liabilities.personalDebts,
            personalDebtPayments = liabilities.personalDebtPayments,
            mortgages = advanced.mortgages,
            assetHoldings = advanced.assetHoldings,
            taxEntries = advanced.taxEntries,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FinanceContentState(),
    )

    fun savePurchase(purchase: Purchase) = viewModelScope.launch {
        purchaseRepository.save(purchase)
    }

    fun deletePurchase(purchase: Purchase) = viewModelScope.launch {
        purchaseRepository.deleteById(purchase.id)
    }

    fun restorePurchase(purchase: Purchase) = viewModelScope.launch {
        purchaseRepository.save(purchase)
    }

    fun saveCategory(category: Category) = viewModelScope.launch {
        categoryRepository.save(category)
    }

    fun setCategoryArchived(category: Category, archived: Boolean) = viewModelScope.launch {
        categoryRepository.setArchived(category.id, archived)
    }

    fun deleteCategory(
        category: Category,
        replacementCategoryId: String? = null,
        onResult: (CategoryDeletionResult) -> Unit,
    ) = viewModelScope.launch {
        onResult(categoryRepository.delete(category.id, replacementCategoryId))
    }

    fun saveBudget(budget: Budget) = viewModelScope.launch {
        budgetRepository.save(budget)
    }

    fun deleteBudget(budget: Budget) = viewModelScope.launch {
        budgetRepository.deleteById(budget.id)
    }

    fun saveRecurringExpense(recurringExpense: RecurringExpense) = viewModelScope.launch {
        recurringExpenseRepository.save(recurringExpense)
    }

    fun deleteRecurringExpense(recurringExpense: RecurringExpense) = viewModelScope.launch {
        recurringExpenseRepository.deleteById(recurringExpense.id)
    }

    fun saveLoan(item: Loan) = viewModelScope.launch { loanRepository.save(item) }

    fun deleteLoan(item: Loan) = viewModelScope.launch { loanRepository.deleteById(item.id) }

    fun recordLoanPayment(
        loan: Loan,
        amountMinorUnits: Long,
        paidAt: LocalDate = LocalDate.now(),
        note: String = "",
    ) = viewModelScope.launch {
        val paymentAmount = amountMinorUnits.coerceIn(1L, loan.remainingMinorUnits)
        val now = Instant.now()
        val remaining = (loan.remainingMinorUnits - paymentAmount).coerceAtLeast(0L)
        loanRepository.savePayment(
            LoanPayment(
                id = UUID.randomUUID().toString(),
                loanId = loan.id,
                amountMinorUnits = paymentAmount,
                paidAt = paidAt,
                note = note,
                createdAt = now,
            )
        )
        loanRepository.save(
            loan.copy(
                remainingMinorUnits = remaining,
                closed = remaining == 0L,
                nextPaymentDate = when {
                    remaining == 0L -> null
                    loan.nextPaymentDate == null -> null
                    else -> loan.nextPaymentDate.plusMonths(1)
                },
                updatedAt = now,
            )
        )
    }

    fun payOffLoanEarly(loan: Loan) {
        if (loan.remainingMinorUnits > 0L) {
            recordLoanPayment(
                loan = loan,
                amountMinorUnits = loan.remainingMinorUnits,
                note = "Early payoff",
            )
        }
    }

    fun savePersonalDebt(item: PersonalDebt) = viewModelScope.launch { personalDebtRepository.save(item) }

    fun deletePersonalDebt(item: PersonalDebt) = viewModelScope.launch { personalDebtRepository.deleteById(item.id) }

    fun recordPersonalDebtPayment(
        debt: PersonalDebt,
        amountMinorUnits: Long,
        paidAt: LocalDate = LocalDate.now(),
        note: String = "",
    ) = viewModelScope.launch {
        val paymentAmount = amountMinorUnits.coerceIn(1L, debt.remainingMinorUnits)
        val now = Instant.now()
        val remaining = (debt.remainingMinorUnits - paymentAmount).coerceAtLeast(0L)
        personalDebtRepository.savePayment(
            PersonalDebtPayment(
                id = UUID.randomUUID().toString(),
                debtId = debt.id,
                amountMinorUnits = paymentAmount,
                paidAt = paidAt,
                note = note,
                createdAt = now,
            )
        )
        personalDebtRepository.save(
            debt.copy(
                remainingMinorUnits = remaining,
                closed = remaining == 0L,
                updatedAt = now,
            )
        )
    }

    fun saveMortgage(item: Mortgage) = viewModelScope.launch { mortgageRepository.save(item.copy(updatedAt = Instant.now())) }

    fun deleteMortgage(item: Mortgage) = viewModelScope.launch { mortgageRepository.deleteById(item.id) }

    fun saveAssetHolding(item: AssetHolding) = viewModelScope.launch { assetHoldingRepository.save(item.copy(updatedAt = Instant.now())) }

    fun deleteAssetHolding(item: AssetHolding) = viewModelScope.launch { assetHoldingRepository.deleteById(item.id) }

    fun saveTaxEntry(item: TaxEntry) = viewModelScope.launch { taxEntryRepository.save(item.copy(updatedAt = Instant.now())) }

    fun deleteTaxEntry(item: TaxEntry) = viewModelScope.launch { taxEntryRepository.deleteById(item.id) }

    fun saveSavingsGoal(goal: SavingsGoal) = viewModelScope.launch {
        savingsGoalRepository.save(goal.copy(updatedAt = Instant.now()))
    }

    fun deleteSavingsGoal(goal: SavingsGoal) = viewModelScope.launch {
        savingsGoalRepository.deleteById(goal.id)
    }

    fun depositToGoal(goal: SavingsGoal, money: Money) = viewModelScope.launch {
        require(money.currency == goal.targetAmount.currency)
        savingsGoalRepository.save(
            goal.copy(
                savedMinorUnits = goal.savedMinorUnits + money.minorUnits,
                updatedAt = Instant.now(),
            )
        )
    }

    fun withdrawFromGoal(goal: SavingsGoal, money: Money) = viewModelScope.launch {
        require(money.currency == goal.targetAmount.currency)
        savingsGoalRepository.save(
            goal.copy(
                savedMinorUnits = (goal.savedMinorUnits - money.minorUnits).coerceAtLeast(0L),
                updatedAt = Instant.now(),
            )
        )
    }

    fun recordRecurringExpense(recurringExpense: RecurringExpense) = viewModelScope.launch {
        val now = Instant.now()
        val zoneId = ZoneId.systemDefault()
        val occurrenceDate = recurringExpense.nextOccurrence
        val occurrenceTime = if (occurrenceDate == LocalDate.now(zoneId)) {
            now.atZone(zoneId).toLocalTime()
        } else {
            java.time.LocalTime.NOON
        }

        purchaseRepository.save(
            Purchase(
                id = UUID.randomUUID().toString(),
                amount = recurringExpense.amount,
                categoryId = recurringExpense.categoryId,
                title = recurringExpense.title,
                note = recurringExpense.note,
                merchant = recurringExpense.merchant,
                paymentMethod = recurringExpense.paymentMethod,
                occurredAt = occurrenceDate.atTime(occurrenceTime).atZone(zoneId).toInstant(),
                type = PurchaseType.EXPENSE,
                recurringExpenseId = recurringExpense.id,
                createdAt = now,
                updatedAt = now,
            )
        )

        recurringExpenseRepository.save(
            recurringExpense.copy(
                nextOccurrence = nextOccurrence(
                    recurringExpense.nextOccurrence,
                    recurringExpense.recurrenceRule,
                ),
                updatedAt = now,
            )
        )
    }

    class Factory(
        private val purchaseRepository: PurchaseRepository,
        private val categoryRepository: CategoryRepository,
        private val budgetRepository: BudgetRepository,
        private val recurringExpenseRepository: RecurringExpenseRepository,
        private val savingsGoalRepository: SavingsGoalRepository,
        private val loanRepository: LoanRepository,
        private val personalDebtRepository: PersonalDebtRepository,
        private val mortgageRepository: MortgageRepository,
        private val assetHoldingRepository: AssetHoldingRepository,
        private val taxEntryRepository: TaxEntryRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FinanceContentViewModel::class.java)) {
                return FinanceContentViewModel(
                    purchaseRepository,
                    categoryRepository,
                    budgetRepository,
                    recurringExpenseRepository,
                    savingsGoalRepository,
                    loanRepository,
                    personalDebtRepository,
                    mortgageRepository,
                    assetHoldingRepository,
                    taxEntryRepository,
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
