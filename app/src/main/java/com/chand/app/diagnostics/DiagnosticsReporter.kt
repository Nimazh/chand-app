package com.chand.app.diagnostics

import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.util.concurrent.ConcurrentHashMap

/** Sends only allowlisted, non-personal diagnostic codes after explicit consent. */
object DiagnosticsReporter {
    private const val TAG = "DiagnosticsReporter"
    private const val NON_FATAL_GAP_MS = 60 * 60_000L
    private val lastNonFatalAt = ConcurrentHashMap<String, Long>()

    @Volatile
    private var consentGranted = false

    @Synchronized
    fun applyConsent(context: Context, enabled: Boolean): Boolean = runCatching {
        if (!enabled) consentGranted = false
        val appContext = context.applicationContext
        val initialized = FirebaseApp.getApps(appContext)
            .any { it.name == FirebaseApp.DEFAULT_APP_NAME }
        if (!enabled && !initialized) return@runCatching true
        if (enabled && !initialized) {
            checkNotNull(FirebaseApp.initializeApp(appContext)) { "Firebase configuration unavailable" }
        }
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.setCrashlyticsCollectionEnabled(enabled)
        if (!enabled) crashlytics.deleteUnsentReports()
        consentGranted = enabled
        true
    }.getOrElse {
        consentGranted = false
        Log.w(TAG, "Could not update crash reporting consent", it)
        false
    }

    fun log(event: Event, widgetType: String? = null, elapsedMs: Long? = null) {
        if (!consentGranted) return
        runCatching {
            val crashlytics = FirebaseCrashlytics.getInstance()
            widgetType?.let { crashlytics.setCustomKey("last_widget_type", it.take(16)) }
            elapsedMs?.let { crashlytics.setCustomKey("last_widget_update_ms", it.coerceAtLeast(0)) }
            crashlytics.log(event.code)
        }.onFailure { Log.w(TAG, "Could not record diagnostic event", it) }
    }

    fun nonFatal(event: Event, error: Throwable, widgetType: String? = null) {
        if (!consentGranted) return
        val now = SystemClock.elapsedRealtime()
        val key = event.code + ':' + (widgetType ?: "")
        val last = lastNonFatalAt[key]
        if (last != null && now - last in 0 until NON_FATAL_GAP_MS) return
        lastNonFatalAt[key] = now
        runCatching {
            val crashlytics = FirebaseCrashlytics.getInstance()
            widgetType?.let { crashlytics.setCustomKey("last_widget_type", it.take(16)) }
            crashlytics.setCustomKey("last_diagnostic_code", event.code)
            crashlytics.setCustomKey("last_error_class", error.javaClass.simpleName.take(64))
            // Do not transmit an exception message or cause: network errors may contain URLs or IDs.
            val safeError = IllegalStateException(event.code).apply {
                stackTrace = error.stackTrace.filter { it.className.startsWith("com.chand.app.") }.take(12).toTypedArray()
            }
            crashlytics.recordException(safeError)
        }.onFailure { Log.w(TAG, "Could not record non-fatal diagnostic", it) }
    }

    enum class Event(val code: String) {
        PRICE_REFRESH_FAILED("price_refresh_failed"),
        CORE_PRICE_REFRESH_FAILED("core_price_refresh_failed"),
        WIDGET_UPDATE_STARTED("widget_update_started"),
        WIDGET_UPDATE_COMPLETED("widget_update_completed"),
        WIDGET_UPDATE_FAILED("widget_update_failed"),
        BACKGROUND_SYNC_FAILED("background_sync_failed"),
        TEST_REPORT("diagnostic_test_report")
    }
}
