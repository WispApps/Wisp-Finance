package com.wisp.finance.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalConfiguration
import com.wisp.finance.ui.security.RecoverySetupContent

/** Read-only demo: no vault, preferences, currency writes or system permission requests. */
@Composable
fun SetupPreviewScreen(onClose: () -> Unit) {
    var step by rememberSaveable { mutableIntStateOf(1) }
    val currentTag = LocalConfiguration.current.locales[0]?.toLanguageTag() ?: "en"
    var languageTag by rememberSaveable { mutableStateOf(currentTag) }
    BackHandler { if (step > 1) step-- else onClose() }
    OnboardingLocale(languageTag) {
        when (step) {
            1 -> LanguageSelectionScreen(preview = true, onClosePreview = onClose,
                onSelected = { languageTag = it; step = 2 })
            // Invalid placeholder pairs, deliberately not a usable recovery phrase.
            2 -> RecoverySetupContent(words = List(24) { "sample-only" }, preview = true,
                onClosePreview = onClose, onFinished = { step = 3 }, onConfirm = { _, _ -> step = 3 })
            3 -> CurrencySelectionScreen(onCurrencySelected = { step = 4 }, preview = true, onClosePreview = onClose)
            4 -> NotificationPermissionScreen(onFinished = onClose, preview = true, onClosePreview = onClose)
        }
    }
}
