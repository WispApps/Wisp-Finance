package com.wisp.finance.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale

/** Read observable app configuration so date and money formatting follows language changes. */
@Composable
internal fun currentAppLocale(): Locale {
    val configuration = LocalConfiguration.current
    return configuration.locales[0] ?: Locale.ENGLISH
}
