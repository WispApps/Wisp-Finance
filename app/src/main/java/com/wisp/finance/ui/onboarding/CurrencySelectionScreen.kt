package com.wisp.finance.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.wisp.finance.R
import com.wisp.finance.domain.model.CurrencyCode
import java.util.Locale

@Composable
fun CurrencySelectionScreen(
    onCurrencySelected: (CurrencyCode) -> Unit,
    preview: Boolean = false,
    onClosePreview: () -> Unit = {},
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedCode by rememberSaveable { mutableStateOf<String?>(null) }
    val locale = LocalConfiguration.current.locales[0] ?: Locale.ENGLISH
    // Display names are cached per locale, not calculated while scrolling.
    val allCurrencies = remember(locale.toLanguageTag()) {
        CurrencyCode.available().map { currency ->
            CurrencyDisplayItem(currency, currency.toJavaCurrency().getDisplayName(locale), currency.toJavaCurrency().getSymbol(locale))
        }
    }
    val currencies = remember(query, allCurrencies, locale) {
        val normalized = query.trim().lowercase(locale)
        allCurrencies.filter { normalized.isBlank() || it.currency.value.lowercase(locale).contains(normalized) ||
            it.displayName.lowercase(locale).contains(normalized) || it.symbol.lowercase(locale).contains(normalized) }
    }
    val selected = allCurrencies.firstOrNull { it.currency.value == selectedCode }
    Box(Modifier.selectableGroup()) {
        OnboardingLayout(
            step = 3,
            title = stringResource(R.string.onboarding_currency_title),
            subtitle = stringResource(R.string.onboarding_currency_subtitle),
            preview = preview, onClosePreview = onClosePreview,
            actions = {
                SetupButton(
                    text = if (selected == null) stringResource(R.string.sec_continue)
                        else stringResource(R.string.setup_continue_currency, selected.currency.value),
                    enabled = selected != null,
                    onClick = { selected?.let { onCurrencySelected(it.currency) } },
                )
            },
        ) {
            item(key = "search") {
                OutlinedTextField(value = query, onValueChange = { query = it }, singleLine = true,
                    label = { Text(stringResource(R.string.search_currency)) }, modifier = Modifier.fillMaxWidth())
            }
            if (currencies.isEmpty()) item(key = "empty") {
                Text(stringResource(R.string.no_currency_results), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(currencies, key = { it.currency.value }) { item ->
                SetupChoice("${item.currency.value} · ${item.symbol}", item.displayName,
                    selected = selectedCode == item.currency.value, onClick = { selectedCode = item.currency.value })
            }
        }
    }
}

private data class CurrencyDisplayItem(val currency: CurrencyCode, val displayName: String, val symbol: String)
