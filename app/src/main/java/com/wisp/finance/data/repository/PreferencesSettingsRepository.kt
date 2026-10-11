package com.wisp.finance.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wisp.finance.domain.model.CurrencyCode
import com.wisp.finance.domain.model.UserPreferences
import com.wisp.finance.domain.repository.SettingsRepository
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private const val SETTINGS_DATASTORE_NAME = "wisp_finance_settings"

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = SETTINGS_DATASTORE_NAME,
)

class PreferencesSettingsRepository(
    context: Context,
) : SettingsRepository {

    private val dataStore = context.applicationContext.settingsDataStore

    override val preferences: Flow<UserPreferences> = dataStore.data
        .catch { throwable ->
            if (throwable is IOException) {
                emit(emptyPreferences())
            } else {
                throw throwable
            }
        }
        .map { preferences ->
            UserPreferences(
                primaryCurrency = preferences[Keys.PRIMARY_CURRENCY]
                    ?.let(CurrencyCode::fromCode),
            )
        }

    override suspend fun setPrimaryCurrency(currency: CurrencyCode) {
        dataStore.edit { preferences ->
            preferences[Keys.PRIMARY_CURRENCY] = currency.value
        }
    }

    private object Keys {
        val PRIMARY_CURRENCY = stringPreferencesKey("primary_currency")
    }
}
