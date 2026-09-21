package com.chand.app.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import com.chand.app.data.local.PreferencesManager
import com.chand.app.data.remote.PriceApiService
import com.chand.app.data.repository.PriceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WidgetRefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        withContext(Dispatchers.IO) {
            val pref = PreferencesManager(context)
            val api = PriceApiService()
            val repo = PriceRepository(api, pref)
            repo.refreshPrices()

            // Update all widgets
            ChandSmallWidget().updateAll(context)
            ChandMediumWidget().updateAll(context)
            ChandLargeWidget().updateAll(context)
        }
    }
}
