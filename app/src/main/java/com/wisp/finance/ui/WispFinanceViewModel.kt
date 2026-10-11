package com.wisp.finance.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wisp.finance.domain.model.CurrencyCode
import com.wisp.finance.domain.model.UserPreferences
import com.wisp.finance.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WispFinanceViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val preferences: StateFlow<UserPreferences?> =
        settingsRepository.preferences.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    fun selectPrimaryCurrency(currency: CurrencyCode) {
        viewModelScope.launch {
            settingsRepository.setPrimaryCurrency(currency)
        }
    }

    class Factory(
        private val settingsRepository: SettingsRepository,
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WispFinanceViewModel::class.java)) {
                return WispFinanceViewModel(
                    settingsRepository = settingsRepository,
                ) as T
            }

            throw IllegalArgumentException(
                "Unknown ViewModel class: ${modelClass.name}",
            )
        }
    }
}
