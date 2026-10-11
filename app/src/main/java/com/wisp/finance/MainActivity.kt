package com.wisp.finance

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wisp.finance.ui.WispFinanceRoot
import com.wisp.finance.ui.WispFinanceViewModel
import com.wisp.finance.ui.finance.FinanceContentViewModel
import com.wisp.finance.ui.theme.WispFinanceTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                Color.TRANSPARENT,
                Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.auto(
                Color.TRANSPARENT,
                Color.TRANSPARENT,
            ),
        )
        super.onCreate(savedInstanceState)
        val app = application as WispFinanceApplication

        setContent {
            WispFinanceTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val onboardingViewModel: WispFinanceViewModel = viewModel(
                        factory = WispFinanceViewModel.Factory(
                            settingsRepository = app.container.settingsRepository,
                        ),
                    )
                    val financeViewModel: FinanceContentViewModel = viewModel(
                        factory = FinanceContentViewModel.Factory(
                            purchaseRepository = app.container.purchaseRepository,
                            categoryRepository = app.container.categoryRepository,
                            budgetRepository = app.container.budgetRepository,
                            recurringExpenseRepository = app.container.recurringExpenseRepository,
                            savingsGoalRepository = app.container.savingsGoalRepository,
                            loanRepository = app.container.loanRepository,
                            personalDebtRepository = app.container.personalDebtRepository,
                            mortgageRepository = app.container.mortgageRepository,
                            assetHoldingRepository = app.container.assetHoldingRepository,
                            taxEntryRepository = app.container.taxEntryRepository,
                        ),
                    )

                    WispFinanceRoot(
                        viewModel = onboardingViewModel,
                        financeViewModel = financeViewModel,
                        backupManager = app.container.backupManager,
                    )
                }
            }
        }
    }
}
