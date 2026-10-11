package com.wisp.finance

import android.app.Application
import com.wisp.finance.data.local.database.WispFinanceDatabaseProvider
import com.wisp.finance.notifications.WispNotificationReceiver
import com.wisp.finance.notifications.WispNotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WispFinanceApplication : Application() {

    val container: AppContainer by lazy {
        AppContainer(this)
    }

    private val applicationScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO,
    )

    override fun onCreate() {
        super.onCreate()

        WispNotificationReceiver.createChannels(this)
        WispNotificationScheduler.schedule(this)

        applicationScope.launch {
            WispFinanceDatabaseProvider.initialize(this@WispFinanceApplication)
        }
    }
}
