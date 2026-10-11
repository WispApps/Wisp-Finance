package com.wisp.finance.notifications

import android.content.Context

class NotificationPreferences(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    var recurringEnabled: Boolean
        get() = prefs.getBoolean(KEY_RECURRING, true)
        set(value) = prefs.edit().putBoolean(KEY_RECURRING, value).apply()

    var budgetsEnabled: Boolean
        get() = prefs.getBoolean(KEY_BUDGETS, true)
        set(value) = prefs.edit().putBoolean(KEY_BUDGETS, value).apply()

    var goalsEnabled: Boolean
        get() = prefs.getBoolean(KEY_GOALS, true)
        set(value) = prefs.edit().putBoolean(KEY_GOALS, value).apply()

    var liabilitiesEnabled: Boolean
        get() = prefs.getBoolean(KEY_LIABILITIES, true)
        set(value) = prefs.edit().putBoolean(KEY_LIABILITIES, value).apply()

    fun wasSent(key: String, marker: String): Boolean = prefs.getString("sent:$key", null) == marker
    fun markSent(key: String, marker: String) { prefs.edit().putString("sent:$key", marker).apply() }

    companion object {
        private const val FILE = "wisp_finance_notifications"
        private const val KEY_RECURRING = "recurring_enabled"
        private const val KEY_BUDGETS = "budgets_enabled"
        private const val KEY_GOALS = "goals_enabled"
        private const val KEY_LIABILITIES = "liabilities_enabled"
    }
}
