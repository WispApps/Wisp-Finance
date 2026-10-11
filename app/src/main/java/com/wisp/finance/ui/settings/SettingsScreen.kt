package com.wisp.finance.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.wisp.finance.R
import com.wisp.finance.domain.model.CurrencyCode
import com.wisp.finance.notifications.NotificationPreferences
import com.wisp.finance.notifications.WispNotificationScheduler
import com.wisp.finance.ui.onboarding.SetupPreviewScreen
import com.wisp.finance.ui.onboarding.SupportedLanguage
import com.wisp.finance.ui.purchases.CurrencySelectionDialog
import androidx.compose.ui.Alignment

@Composable
fun SettingsScreen(
    primaryCurrency: CurrencyCode,
    onBack: () -> Unit,
    onCurrencySelected: (CurrencyCode) -> Unit,
    onPrivacyClick: () -> Unit,
    onBackupClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSetupPreview by remember { mutableStateOf(false) }
    if (showSetupPreview) {
        SetupPreviewScreen(onClose = { showSetupPreview = false })
        return
    }
    var showCurrency by remember { mutableStateOf(false) }
    var showLanguage by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val notificationPreferences = remember { NotificationPreferences(context) }
    var recurringNotifications by remember { mutableStateOf(notificationPreferences.recurringEnabled) }
    var budgetNotifications by remember { mutableStateOf(notificationPreferences.budgetsEnabled) }
    var goalNotifications by remember { mutableStateOf(notificationPreferences.goalsEnabled) }
    var liabilityNotifications by remember { mutableStateOf(notificationPreferences.liabilitiesEnabled) }

    if (showCurrency) {
        CurrencySelectionDialog(
            onDismiss = { showCurrency = false },
            onSelected = {
                onCurrencySelected(it)
                showCurrency = false
            },
        )
    }

    if (showLanguage) {
        LanguageDialog(onDismiss = { showLanguage = false })
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        Text(
            stringResource(R.string.settings_general),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 18.dp, bottom = 8.dp),
        )

        SettingsRow(
            title = stringResource(R.string.settings_language),
            value = currentLanguageName(),
            onClick = { showLanguage = true },
        )
        SettingsRow(
            title = stringResource(R.string.settings_primary_currency),
            value = primaryCurrency.value,
            onClick = { showCurrency = true },
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        Text(
            stringResource(R.string.settings_notifications),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        NotificationToggleRow(
            title = stringResource(R.string.settings_notifications_recurring),
            checked = recurringNotifications,
            onCheckedChange = {
                recurringNotifications = it
                notificationPreferences.recurringEnabled = it
                WispNotificationScheduler.schedule(context)
            },
        )
        NotificationToggleRow(
            title = stringResource(R.string.settings_notifications_budgets),
            checked = budgetNotifications,
            onCheckedChange = {
                budgetNotifications = it
                notificationPreferences.budgetsEnabled = it
                WispNotificationScheduler.schedule(context)
            },
        )
        NotificationToggleRow(
            title = stringResource(R.string.settings_notifications_goals),
            checked = goalNotifications,
            onCheckedChange = {
                goalNotifications = it
                notificationPreferences.goalsEnabled = it
                WispNotificationScheduler.schedule(context)
            },
        )
        NotificationToggleRow(
            title = stringResource(R.string.settings_notifications_liabilities),
            checked = liabilityNotifications,
            onCheckedChange = {
                liabilityNotifications = it
                notificationPreferences.liabilitiesEnabled = it
                WispNotificationScheduler.schedule(context)
            },
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        SettingsRow(
            title = stringResource(R.string.backup_title),
            value = stringResource(R.string.backup_settings_description),
            onClick = onBackupClick,
        )

        SettingsRow(
            title = stringResource(R.string.privacy_title),
            value = stringResource(R.string.privacy_short_description),
            onClick = onPrivacyClick,
        )

        SettingsRow(
            title = stringResource(R.string.setup_preview_title),
            value = stringResource(R.string.setup_preview_description),
            onClick = { showSetupPreview = true },
        )

        Text(
            stringResource(R.string.settings_currency_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 18.dp),
        )
    }
}

@Composable
private fun SettingsRow(title: String, value: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).sizeIn(minHeight = 84.dp).clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
    }
}


@Composable
private fun NotificationToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).sizeIn(minHeight = 72.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f).padding(end = 12.dp, top = 8.dp),
            )
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun LanguageDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_language)) },
        text = {
            Column {
                SupportedLanguage.entries.forEach { language ->
                    TextButton(
                        onClick = {
                            AppCompatDelegate.setApplicationLocales(
                                LocaleListCompat.forLanguageTags(language.languageTag)
                            )
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            stringResource(language.labelRes),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
private fun currentLanguageName(): String {
    val tag = AppCompatDelegate.getApplicationLocales()[0]?.language
    val language = SupportedLanguage.entries.firstOrNull { it.languageTag == tag }
    return language?.let { stringResource(it.labelRes) }
        ?: stringResource(R.string.settings_language_system)
}
