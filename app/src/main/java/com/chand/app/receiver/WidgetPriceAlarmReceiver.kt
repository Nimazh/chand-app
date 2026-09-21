package com.chand.app.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.glance.appwidget.updateAll
import com.chand.app.data.local.PreferencesManager
import com.chand.app.data.remote.PriceApiService
import com.chand.app.data.repository.PriceRepository
import com.chand.app.widget.ChandLargeWidget
import com.chand.app.widget.ChandMediumWidget
import com.chand.app.widget.ChandSmallWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WidgetPriceAlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "WidgetPriceAlarm"
        const val ACTION_SYNC_WIDGET_PRICES = "com.chand.app.ACTION_SYNC_WIDGET_PRICES"
        private const val INTERVAL_MILLIS = 2 * 60 * 1000L // 2 Minutes

        fun scheduleNextAlarm(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, WidgetPriceAlarmReceiver::class.java).apply {
                action = ACTION_SYNC_WIDGET_PRICES
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                1001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val triggerTime = System.currentTimeMillis() + INTERVAL_MILLIS

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
                Log.d(TAG, "Next 2-minute price sync scheduled for +2min")
            } catch (e: SecurityException) {
                Log.w(TAG, "Exact alarm permission fallback: ${e.message}")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received alarm trigger: ${intent.action}")

        // Immediately schedule the next alarm for 2 minutes from now to maintain the chain
        scheduleNextAlarm(context)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefManager = PreferencesManager(context)
                val apiService = PriceApiService()
                val repository = PriceRepository(apiService, prefManager)

                val result = repository.refreshPrices()
                if (result.isSuccess) {
                    ChandSmallWidget().updateAll(context)
                    ChandMediumWidget().updateAll(context)
                    ChandLargeWidget().updateAll(context)
                    Log.d(TAG, "Widgets updated successfully from 2-minute alarm")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in 2-minute widget sync: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
