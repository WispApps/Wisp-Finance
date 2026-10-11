package com.wisp.finance.domain.repository

import com.wisp.finance.domain.model.CurrencyCode
import com.wisp.finance.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    val preferences: Flow<UserPreferences>

    suspend fun setPrimaryCurrency(currency: CurrencyCode)
}
