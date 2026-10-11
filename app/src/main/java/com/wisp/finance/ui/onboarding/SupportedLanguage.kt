package com.wisp.finance.ui.onboarding

import androidx.annotation.StringRes
import com.wisp.finance.R

enum class SupportedLanguage(
    val languageTag: String,
    val nativeLabel: String,
    @StringRes val labelRes: Int,
) {
    ENGLISH("en", "English", R.string.language_english),
    RUSSIAN("ru", "Русский", R.string.language_russian),
    UKRAINIAN("uk", "Українська", R.string.language_ukrainian),
    GERMAN("de", "Deutsch", R.string.language_german),
    SPANISH("es", "Español", R.string.language_spanish),
    FRENCH("fr", "Français", R.string.language_french),
    ITALIAN("it", "Italiano", R.string.language_italian),
    DUTCH("nl", "Nederlands", R.string.language_dutch),
    POLISH("pl", "Polski", R.string.language_polish),
    PORTUGUESE("pt", "Português", R.string.language_portuguese),
}
