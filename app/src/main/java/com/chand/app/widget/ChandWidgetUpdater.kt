package com.chand.app.widget

import android.content.Context
import android.os.SystemClock
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import com.chand.app.diagnostics.DiagnosticsReporter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.launch

/** Serializes each widget size so a slower old render cannot overwrite a newer selection. */
object ChandWidgetUpdater {
    private const val TAG = "ChandWidgetUpdater"
    private val locks = ChandWidgetType.entries.associateWith { Mutex() }

    suspend fun updateAll(context: Context) = supervisorScope {
        ChandWidgetType.entries.forEach { kind ->
            launch { updateType(context, kind) }
        }
    }

    suspend fun updateType(context: Context, kind: ChandWidgetType) {
        locks.getValue(kind).withLock {
            val started = SystemClock.elapsedRealtime()
            DiagnosticsReporter.log(DiagnosticsReporter.Event.WIDGET_UPDATE_STARTED, kind.name)
            try {
                when (kind) {
                    ChandWidgetType.SMALL -> ChandSmallWidget().updateAll(context)
                    ChandWidgetType.MEDIUM -> ChandMediumWidget().updateAll(context)
                    ChandWidgetType.LARGE -> ChandLargeWidget().updateAll(context)
                }
                Log.d(TAG, "Updated $kind widgets in ${SystemClock.elapsedRealtime() - started}ms")
                DiagnosticsReporter.log(
                    DiagnosticsReporter.Event.WIDGET_UPDATE_COMPLETED,
                    kind.name,
                    SystemClock.elapsedRealtime() - started
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Log.e(TAG, "Failed to update $kind widgets", error)
                DiagnosticsReporter.nonFatal(DiagnosticsReporter.Event.WIDGET_UPDATE_FAILED, error, kind.name)
            }
        }
    }

    suspend fun updateOne(context: Context, appWidgetId: Int, kind: ChandWidgetType) {
        locks.getValue(kind).withLock {
            val started = SystemClock.elapsedRealtime()
            DiagnosticsReporter.log(DiagnosticsReporter.Event.WIDGET_UPDATE_STARTED, kind.name)
            try {
                val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
                when (kind) {
                    ChandWidgetType.SMALL -> ChandSmallWidget().update(context, glanceId)
                    ChandWidgetType.MEDIUM -> ChandMediumWidget().update(context, glanceId)
                    ChandWidgetType.LARGE -> ChandLargeWidget().update(context, glanceId)
                }
                Log.d(TAG, "Updated $kind widget $appWidgetId in ${SystemClock.elapsedRealtime() - started}ms")
                DiagnosticsReporter.log(
                    DiagnosticsReporter.Event.WIDGET_UPDATE_COMPLETED,
                    kind.name,
                    SystemClock.elapsedRealtime() - started
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Log.w(TAG, "Direct update failed for $kind widget $appWidgetId; updating size", error)
                DiagnosticsReporter.nonFatal(DiagnosticsReporter.Event.WIDGET_UPDATE_FAILED, error, kind.name)
                when (kind) {
                    ChandWidgetType.SMALL -> ChandSmallWidget().updateAll(context)
                    ChandWidgetType.MEDIUM -> ChandMediumWidget().updateAll(context)
                    ChandWidgetType.LARGE -> ChandLargeWidget().updateAll(context)
                }
            }
        }
    }
}

enum class ChandWidgetType { SMALL, MEDIUM, LARGE }
