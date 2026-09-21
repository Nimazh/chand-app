package com.chand.app.worker

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chand.app.data.local.PreferencesManager
import com.chand.app.data.remote.PriceApiService
import com.chand.app.data.repository.PriceRepository
import com.chand.app.widget.ChandLargeWidget
import com.chand.app.widget.ChandMediumWidget
import com.chand.app.widget.ChandSmallWidget

class PriceSyncWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val prefManager = PreferencesManager(context)
            val apiService = PriceApiService()
            val repository = PriceRepository(apiService, prefManager)

            // Refresh prices
            repository.refreshPrices()

            // Update all Glance widgets on the home screen
            ChandSmallWidget().updateAll(context)
            ChandMediumWidget().updateAll(context)
            ChandLargeWidget().updateAll(context)

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
