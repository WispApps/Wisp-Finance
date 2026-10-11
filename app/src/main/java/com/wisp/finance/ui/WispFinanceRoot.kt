package com.wisp.finance.ui

import com.wisp.finance.ui.security.RecoverySetupScreen
import androidx.compose.ui.platform.LocalConfiguration
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wisp.finance.data.backup.WispBackupManager
import com.wisp.finance.ui.finance.FinanceApp
import com.wisp.finance.ui.finance.FinanceContentViewModel
import com.wisp.finance.ui.onboarding.*

@Composable
fun WispFinanceRoot(
    viewModel: WispFinanceViewModel,
    financeViewModel: FinanceContentViewModel,
    backupManager: WispBackupManager,
) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val currentPreferences = preferences
    val context = LocalContext.current
    val notificationStore = remember {
        NotificationOnboardingStore(context)
    }
    var notificationStepCompleted by remember {
        mutableStateOf(notificationStore.isCompleted())
    }

    if (currentPreferences == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val configuration = LocalConfiguration.current
    var securityReady by remember { mutableStateOf(backupManager.vault.isReady()) }
    val hasExplicitLanguage = !AppCompatDelegate.getApplicationLocales().isEmpty

    when {
        !hasExplicitLanguage -> LanguageSelectionScreen()

        !securityReady -> RecoverySetupScreen(backupManager.vault, onFinished = { securityReady = true })

        currentPreferences.primaryCurrency == null -> CurrencySelectionScreen(
            onCurrencySelected = viewModel::selectPrimaryCurrency,
        )

        !notificationStepCompleted -> NotificationPermissionScreen(
            onFinished = {
                notificationStore.markCompleted()
                notificationStepCompleted = true
            },
        )

        else -> FinanceApp(
            primaryCurrency = currentPreferences.primaryCurrency,
            viewModel = financeViewModel,
            onPrimaryCurrencySelected = viewModel::selectPrimaryCurrency,
            backupManager = backupManager,
        )
    }
}
