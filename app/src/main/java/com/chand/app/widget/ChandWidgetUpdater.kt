package com.chand.app.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/** Keeps widget rendering on the same data generation that was just persisted. */
object ChandWidgetUpdater {
    suspend fun updateAll(context: Context) = coroutineScope {
        launch { ChandSmallWidget().updateAll(context) }
        launch { ChandMediumWidget().updateAll(context) }
        launch { ChandLargeWidget().updateAll(context) }
    }

    suspend fun updateOne(context: Context, appWidgetId: Int, kind: ChandWidgetType) {
        val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
        when (kind) {
            ChandWidgetType.SMALL -> ChandSmallWidget().update(context, glanceId)
            ChandWidgetType.MEDIUM -> ChandMediumWidget().update(context, glanceId)
            ChandWidgetType.LARGE -> ChandLargeWidget().update(context, glanceId)
        }
    }
}

enum class ChandWidgetType { SMALL, MEDIUM, LARGE }
