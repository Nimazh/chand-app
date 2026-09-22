package com.chand.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chand.app.data.local.PreferencesManager
import com.chand.app.data.remote.PriceApiService
import com.chand.app.data.repository.PriceRepository
import com.chand.app.widget.ChandWidgetUpdater

class PriceSyncWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val prefManager = PreferencesManager(context)
            val apiService = PriceApiService()
            val repository = PriceRepository(apiService, prefManager, observeCache = false)

            val refreshResult = repository.refreshPrices()
            if (refreshResult.isFailure) return Result.retry()

            // Update all Glance widgets on the home screen
            if (refreshResult.getOrNull()?.pricesChanged == true) ChandWidgetUpdater.updateAll(context)

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
