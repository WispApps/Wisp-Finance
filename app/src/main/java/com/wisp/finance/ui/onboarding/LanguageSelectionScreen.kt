package com.wisp.finance.ui.onboarding

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import com.wisp.finance.R

@Composable
fun LanguageSelectionScreen(
    preview: Boolean = false,
    onClosePreview: () -> Unit = {},
    onSelected: (String) -> Unit = { tag ->
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
    },
) {
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    // English until an explicit choice; selecting a row translates this screen immediately.
    OnboardingLocale(selected ?: "en") {
        Box(Modifier.selectableGroup()) {
            OnboardingLayout(
                step = 1,
                title = stringResource(R.string.onboarding_language_title),
                subtitle = stringResource(R.string.onboarding_language_subtitle),
                preview = preview, onClosePreview = onClosePreview,
                actions = {
                    SetupButton(stringResource(R.string.sec_continue), enabled = selected != null,
                        onClick = { selected?.let(onSelected) })
                },
            ) {
                items(SupportedLanguage.entries, key = { it.languageTag }) { language ->
                    SetupChoice(language.nativeLabel, selected = selected == language.languageTag,
                        onClick = { selected = language.languageTag })
                }
            }
        }
    }
}
