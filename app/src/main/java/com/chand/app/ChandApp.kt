package com.chand.app

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.chand.app.receiver.WidgetPriceAlarmReceiver
import com.chand.app.worker.PriceSyncWorker
import java.util.concurrent.TimeUnit

class ChandApp : Application() {

    override fun onCreate() {
        super.onCreate()
        setupBackgroundSync()
        WidgetPriceAlarmReceiver.scheduleNextAlarm(this)
    }

    private fun setupBackgroundSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Schedule periodic sync every 15 minutes (Android system minimum for periodic work)
        val syncRequest = PeriodicWorkRequestBuilder<PriceSyncWorker>(
            15, TimeUnit.MINUTES,
            5, TimeUnit.MINUTES // Flex interval
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "ChandPriceSyncWork",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}
