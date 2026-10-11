package com.wisp.finance.ui.onboarding

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.wisp.finance.ui.security.RecoverySetupContent
import com.wisp.finance.ui.theme.WispFinanceTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Run on a disposable emulator with :app:connectedDebugAndroidTest. No vault writes. */
class OnboardingFlowTest {
    @get:Rule val compose = createComposeRule()

    @Test fun languageSelectionTranslatesImmediatelyButCommitsOnlyOnContinue() {
        val previousLocale = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        var committed: String? = null
        compose.setContent { WispFinanceTheme { LanguageSelectionScreen(onSelected = { committed = it }) } }
        compose.onNodeWithText("Continue").assertIsNotEnabled()
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Русский"))
        compose.onNodeWithText("Русский").performClick()
        compose.onNodeWithText("Продолжить").assertIsEnabled()
        compose.runOnIdle {
            assertNull(committed)
            assertEquals(previousLocale, AppCompatDelegate.getApplicationLocales().toLanguageTags())
        }
        compose.onNodeWithText("Продолжить").performClick()
        compose.runOnIdle { assertEquals("ru", committed) }
    }

    @Test fun currencyRequiresExplicitSelectionAndConfirmation() {
        var committed: String? = null
        compose.setContent { WispFinanceTheme { OnboardingLocale("en") {
            CurrencySelectionScreen(onCurrencySelected = { committed = it.value })
        } } }
        compose.onNodeWithText("Continue").assertIsNotEnabled()
        compose.onNodeWithText("Search currency").performScrollTo().performTextInput("EUR")
        compose.onNodeWithText("EUR · €").performScrollTo().performClick()
        compose.runOnIdle { assertNull(committed) }
        compose.onNodeWithText("Continue with EUR").performClick()
        compose.runOnIdle { assertEquals("EUR", committed) }
    }

    @Test fun recoveryRequiresSavedAcknowledgmentAndNonEmptyConfirmation() {
        var submissions = 0
        var submitted = ""
        compose.setContent { WispFinanceTheme { OnboardingLocale("en") {
            RecoverySetupContent(words = List(24) { "sample-only" }, onFinished = {},
                onConfirm = { input, imported ->
                    assertFalse(imported); submissions++; submitted = input
                })
        } } }
        compose.onNodeWithText("Continue").assertIsNotEnabled()
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("I saved the phrase separately from the backup file."))
        compose.onNodeWithText("I saved the phrase separately from the backup file.").performClick()
        compose.onNodeWithText("Continue").performClick()
        compose.onNodeWithText("Continue").assertIsNotEnabled()
        compose.runOnIdle { assertEquals(0, submissions) }
        compose.onNodeWithText("Re-enter the displayed phrase").performScrollTo().performTextInput("test input")
        compose.onNodeWithText("Continue").performClick()
        compose.runOnIdle { assertEquals(1, submissions); assertEquals("test input", submitted) }
    }

    @Test fun notificationPreviewFinishesWithoutOpeningPermissionDialog() {
        var finished = false
        compose.setContent { WispFinanceTheme { OnboardingLocale("en") {
            NotificationPermissionScreen(preview = true, onFinished = { finished = true })
        } } }
        compose.onNodeWithText("Allow notifications").performClick()
        compose.runOnIdle { assertTrue(finished) }
    }
}
