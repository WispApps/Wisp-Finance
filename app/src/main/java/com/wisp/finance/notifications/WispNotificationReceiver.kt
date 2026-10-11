package com.wisp.finance.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.wisp.finance.MainActivity
import com.wisp.finance.R
import com.wisp.finance.WispFinanceApplication
import com.wisp.finance.ui.budgets.calculateBudgetProgress
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WispNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                checkAndNotify(context.applicationContext)
            } finally {
                WispNotificationScheduler.schedule(context)
                pending.finish()
            }
        }
    }

    private suspend fun checkAndNotify(context: Context) {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        createChannels(context)
        val app = context as WispFinanceApplication
        val prefs = NotificationPreferences(context)
        val today = LocalDate.now()

        if (prefs.recurringEnabled) {
            app.container.recurringExpenseRepository.getDueOnOrBefore(today.plusDays(1))
                .filter { it.enabled }
                .forEach { expense ->
                    val days = ChronoUnit.DAYS.between(today, expense.nextOccurrence)
                    val marker = "${expense.nextOccurrence}:$days"
                    val key = "recurring:${expense.id}"
                    if (!prefs.wasSent(key, marker)) {
                        val text = if (days <= 0) {
                            context.getString(R.string.notification_recurring_today, expense.title)
                        } else {
                            context.getString(R.string.notification_recurring_tomorrow, expense.title)
                        }
                        notify(context, CHANNEL_PAYMENTS, key.hashCode(), context.getString(R.string.notification_payments_title), text)
                        prefs.markSent(key, marker)
                    }
                }
        }

        if (prefs.budgetsEnabled) {
            val budgets = app.container.budgetRepository.observeAll().first().filter { it.enabled }
            val purchases = app.container.purchaseRepository.observeAll().first()
            budgets.forEach { budget ->
                val progress = calculateBudgetProgress(budget, purchases, Locale.getDefault())
                val level = when {
                    progress.percent >= 100 -> 100
                    progress.percent >= 80 -> 80
                    else -> 0
                }
                if (level > 0) {
                    val marker = "$today:$level"
                    val key = "budget:${budget.id}"
                    if (!prefs.wasSent(key, marker)) {
                        val text = if (level == 100) {
                            context.getString(R.string.notification_budget_exceeded, progress.percent)
                        } else {
                            context.getString(R.string.notification_budget_near, progress.percent)
                        }
                        notify(context, CHANNEL_BUDGETS, key.hashCode(), context.getString(R.string.notification_budgets_title), text)
                        prefs.markSent(key, marker)
                    }
                }
            }
        }

        if (prefs.goalsEnabled) {
            app.container.savingsGoalRepository.observeAll().first()
                .filter { !it.completed && it.deadline != null }
                .forEach { goal ->
                    val days = ChronoUnit.DAYS.between(today, goal.deadline)
                    if (days in 0..7) {
                        val marker = "$today:$days"
                        val key = "goal:${goal.id}"
                        if (!prefs.wasSent(key, marker)) {
                            val text = context.resources.getQuantityString(
                                R.plurals.notification_goal_due,
                                days.toInt(),
                                goal.title,
                                days.toInt(),
                            )
                            notify(context, CHANNEL_GOALS, key.hashCode(), context.getString(R.string.notification_goals_title), text)
                            prefs.markSent(key, marker)
                        }
                    }
                }
        }

        if (prefs.liabilitiesEnabled) {
            app.container.loanRepository.observeAll().first()
                .filter { !it.closed && it.nextPaymentDate != null }
                .forEach { loan ->
                    val dueDate = loan.nextPaymentDate ?: return@forEach
                    val days = ChronoUnit.DAYS.between(today, dueDate)
                    if (days <= 1) {
                        val marker = "$dueDate:$days"
                        val key = "loan:${loan.id}"
                        if (!prefs.wasSent(key, marker)) {
                            val text = when {
                                days < 0 -> context.getString(R.string.notification_loan_overdue, loan.title)
                                days == 0L -> context.getString(R.string.notification_loan_due_today, loan.title)
                                else -> context.getString(R.string.notification_loan_due_tomorrow, loan.title)
                            }
                            notify(context, CHANNEL_LIABILITIES, key.hashCode(), context.getString(R.string.notification_liabilities_title), text)
                            prefs.markSent(key, marker)
                        }
                    }
                }

            app.container.personalDebtRepository.observeAll().first()
                .filter { !it.closed && it.dueDate != null }
                .forEach { debt ->
                    val dueDate = debt.dueDate ?: return@forEach
                    val days = ChronoUnit.DAYS.between(today, dueDate)
                    if (days <= 1) {
                        val marker = "$dueDate:$days"
                        val key = "debt:${debt.id}"
                        if (!prefs.wasSent(key, marker)) {
                            val text = when {
                                days < 0 -> context.getString(R.string.notification_debt_overdue, debt.personName)
                                days == 0L -> context.getString(R.string.notification_debt_due_today, debt.personName)
                                else -> context.getString(R.string.notification_debt_due_tomorrow, debt.personName)
                            }
                            notify(context, CHANNEL_LIABILITIES, key.hashCode(), context.getString(R.string.notification_liabilities_title), text)
                            prefs.markSent(key, marker)
                        }
                    }
                }
        }
    }

    private fun notify(context: Context, channel: String, id: Int, title: String, text: String) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val openApp = PendingIntent.getActivity(
            context,
            id,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        manager.notify(
            id,
            NotificationCompat.Builder(context, channel)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(openApp)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build(),
        )
    }

    companion object {
        const val CHANNEL_PAYMENTS = "payments"
        const val CHANNEL_BUDGETS = "budgets"
        const val CHANNEL_GOALS = "goals"
        const val CHANNEL_LIABILITIES = "liabilities"

        fun createChannels(context: Context) {
            if (Build.VERSION.SDK_INT < 26) return
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannels(
                listOf(
                    NotificationChannel(CHANNEL_PAYMENTS, context.getString(R.string.notification_channel_payments), NotificationManager.IMPORTANCE_DEFAULT),
                    NotificationChannel(CHANNEL_BUDGETS, context.getString(R.string.notification_channel_budgets), NotificationManager.IMPORTANCE_DEFAULT),
                    NotificationChannel(CHANNEL_GOALS, context.getString(R.string.notification_channel_goals), NotificationManager.IMPORTANCE_DEFAULT),
                    NotificationChannel(CHANNEL_LIABILITIES, context.getString(R.string.notification_channel_liabilities), NotificationManager.IMPORTANCE_DEFAULT),
                )
            )
        }
    }
}


