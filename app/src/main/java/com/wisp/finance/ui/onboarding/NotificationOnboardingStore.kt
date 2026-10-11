package com.wisp.finance.ui.onboarding

import android.content.Context

class NotificationOnboardingStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "wisp_finance_onboarding",
        Context.MODE_PRIVATE,
    )

    fun isCompleted(): Boolean =
        preferences.getBoolean(KEY_COMPLETED, false)

    fun markCompleted() {
        preferences.edit().putBoolean(KEY_COMPLETED, true).apply()
    }

    private companion object {
        const val KEY_COMPLETED = "notification_permission_step_completed"
    }
}
