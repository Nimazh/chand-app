package com.chand.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.chand.app.data.model.PriceItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "chand_settings")

data class WidgetSnapshot(
    val prices: List<PriceItem>,
    val favorites: Set<String>,
    val smallItemId: String,
    val mediumItemIds: List<String>,
    val largeItemIds: List<String>,
    val themeId: String,
    val opacity: Int,
    val cornerRadius: Int
)

class PreferencesManager(private val context: Context) {
    companion object {
        private val KEY_FAVORITES = stringSetPreferencesKey("favorites")
        private val KEY_WIDGET_SMALL_ITEM = stringPreferencesKey("widget_small_item")
        private const val WIDGET_SMALL_ITEM_PREFIX = "widget_small_item_"
        private val KEY_WIDGET_MEDIUM_ITEMS = stringPreferencesKey("widget_medium_items_order")
        private const val WIDGET_MEDIUM_ITEMS_PREFIX = "widget_medium_items_order_"
        private const val WIDGET_LARGE_ITEMS_PREFIX = "widget_large_items_order_"
        private val KEY_FOREGROUND_REFRESH_MINUTES = intPreferencesKey("foreground_refresh_minutes")
        private val KEY_LAST_UPDATE_TIME = longPreferencesKey("last_update_time")
        private val KEY_APP_THEME_MODE = stringPreferencesKey("app_theme_mode")
        private val KEY_CACHED_PRICES_JSON = stringPreferencesKey("cached_prices_json")
        private val KEY_WIDGET_THEME = stringPreferencesKey("widget_theme")
        private val KEY_WIDGET_OPACITY = intPreferencesKey("widget_opacity")
        private val KEY_WIDGET_CORNER_RADIUS = intPreferencesKey("widget_corner_radius")
        private val KEY_DIAGNOSTICS_CONSENT = booleanPreferencesKey("diagnostics_consent")

        val DEFAULT_FAVORITES = setOf("usd", "gold18", "emami", "usdt", "btc")
        val DEFAULT_MEDIUM_ITEMS = listOf("usd", "gold18", "emami", "usdt")
        const val DEFAULT_WIDGET_THEME = "apple_white"
        const val DEFAULT_WIDGET_OPACITY = 100
        const val DEFAULT_WIDGET_CORNER_RADIUS = 22
        const val DEFAULT_APP_THEME_MODE = "system"
        const val DEFAULT_FOREGROUND_REFRESH_MINUTES = 5
        private val ALLOWED_FOREGROUND_REFRESH_MINUTES = setOf(5, 10, 15)
    }

    private val gson = Gson()

    val favoritesFlow: Flow<Set<String>> = context.dataStore.data.map { it[KEY_FAVORITES] ?: DEFAULT_FAVORITES }
    val widgetThemeFlow: Flow<String> = context.dataStore.data.map { it[KEY_WIDGET_THEME] ?: DEFAULT_WIDGET_THEME }
    val widgetOpacityFlow: Flow<Int> = context.dataStore.data.map { it[KEY_WIDGET_OPACITY] ?: DEFAULT_WIDGET_OPACITY }
    val widgetCornerRadiusFlow: Flow<Int> = context.dataStore.data.map { it[KEY_WIDGET_CORNER_RADIUS] ?: DEFAULT_WIDGET_CORNER_RADIUS }
    val foregroundRefreshMinutesFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_FOREGROUND_REFRESH_MINUTES]
            ?.takeIf { it in ALLOWED_FOREGROUND_REFRESH_MINUTES }
            ?: DEFAULT_FOREGROUND_REFRESH_MINUTES
    }

    fun smallWidgetItemFlow(appWidgetId: Int? = null): Flow<String> = context.dataStore.data.map { prefs ->
        val widgetKey = appWidgetId?.takeIf { it > 0 }?.let { stringPreferencesKey(WIDGET_SMALL_ITEM_PREFIX + it) }
        (widgetKey?.let { prefs[it] } ?: prefs[KEY_WIDGET_SMALL_ITEM] ?: "usd").lowercase()
    }

    fun mediumWidgetItemsFlow(appWidgetId: Int? = null): Flow<List<String>> = context.dataStore.data.map { prefs ->
        val widgetKey = appWidgetId?.takeIf { it > 0 }?.let { stringPreferencesKey(WIDGET_MEDIUM_ITEMS_PREFIX + it) }
        parseAssetIds(widgetKey?.let { prefs[it] } ?: prefs[KEY_WIDGET_MEDIUM_ITEMS], 4)
            .ifEmpty { DEFAULT_MEDIUM_ITEMS }
    }
    val mediumWidgetItemsFlow: Flow<List<String>> = mediumWidgetItemsFlow()

    /** An empty list means this large widget still follows the user's favourites. */
    fun largeWidgetItemsFlow(appWidgetId: Int): Flow<List<String>> = context.dataStore.data.map { prefs ->
        if (appWidgetId <= 0) emptyList() else parseAssetIds(
            prefs[stringPreferencesKey(WIDGET_LARGE_ITEMS_PREFIX + appWidgetId)],
            6
        )
    }
    val appThemeModeFlow: Flow<String> = context.dataStore.data.map { it[KEY_APP_THEME_MODE] ?: DEFAULT_APP_THEME_MODE }
    val lastUpdateTimeFlow: Flow<Long> = context.dataStore.data.map { it[KEY_LAST_UPDATE_TIME] ?: 0L }
    val diagnosticsConsentFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_DIAGNOSTICS_CONSENT] ?: false }

    val cachedPricesFlow: Flow<List<PriceItem>> = context.dataStore.data
        .map { prefs -> prefs[KEY_CACHED_PRICES_JSON] }
        .distinctUntilChanged()
        .map(::parseCachedPrices)

    /** Reads the entire widget state from one DataStore snapshot for a fast, consistent render. */
    suspend fun readWidgetSnapshot(appWidgetId: Int): WidgetSnapshot {
        val prefs = context.dataStore.data.first()
        val smallKey = stringPreferencesKey(WIDGET_SMALL_ITEM_PREFIX + appWidgetId)
        val mediumKey = stringPreferencesKey(WIDGET_MEDIUM_ITEMS_PREFIX + appWidgetId)
        val largeKey = stringPreferencesKey(WIDGET_LARGE_ITEMS_PREFIX + appWidgetId)
        return WidgetSnapshot(
            prices = parseCachedPrices(prefs[KEY_CACHED_PRICES_JSON]),
            favorites = prefs[KEY_FAVORITES] ?: DEFAULT_FAVORITES,
            smallItemId = prefs[smallKey] ?: prefs[KEY_WIDGET_SMALL_ITEM] ?: "usd",
            mediumItemIds = parseAssetIds(prefs[mediumKey] ?: prefs[KEY_WIDGET_MEDIUM_ITEMS], 4)
                .ifEmpty { DEFAULT_MEDIUM_ITEMS },
            largeItemIds = parseAssetIds(prefs[largeKey], 6),
            themeId = prefs[KEY_WIDGET_THEME] ?: DEFAULT_WIDGET_THEME,
            opacity = prefs[KEY_WIDGET_OPACITY] ?: DEFAULT_WIDGET_OPACITY,
            cornerRadius = prefs[KEY_WIDGET_CORNER_RADIUS] ?: DEFAULT_WIDGET_CORNER_RADIUS
        )
    }

    suspend fun saveCachedPrices(items: List<PriceItem>) {
        if (items.isNotEmpty()) context.dataStore.edit { it[KEY_CACHED_PRICES_JSON] = gson.toJson(items) }
    }

    suspend fun toggleFavorite(itemId: String) = context.dataStore.edit { prefs ->
        val current = (prefs[KEY_FAVORITES] ?: DEFAULT_FAVORITES).toMutableSet()
        if (!current.add(itemId)) current.remove(itemId)
        prefs[KEY_FAVORITES] = current
    }

    suspend fun setSmallWidgetItem(itemId: String, appWidgetId: Int? = null) {
        val normalized = itemId.trim().lowercase()
        require(normalized.matches(Regex("[a-z0-9_-]{1,32}"))) { "Invalid asset id" }
        context.dataStore.edit { prefs ->
            val widgetKey = appWidgetId?.takeIf { it > 0 }?.let { stringPreferencesKey(WIDGET_SMALL_ITEM_PREFIX + it) }
            if (widgetKey == null) prefs[KEY_WIDGET_SMALL_ITEM] = normalized else prefs[widgetKey] = normalized
        }
    }

    /** The detail-screen shortcut explicitly applies its asset to every installed small widget. */
    suspend fun setSmallWidgetItemForAll(itemId: String, appWidgetIds: IntArray) {
        val normalized = itemId.trim().lowercase()
        require(normalized.matches(Regex("[a-z0-9_-]{1,32}"))) { "Invalid asset id" }
        context.dataStore.edit { prefs ->
            prefs[KEY_WIDGET_SMALL_ITEM] = normalized
            appWidgetIds.filter { it > 0 }.forEach { id ->
                prefs[stringPreferencesKey(WIDGET_SMALL_ITEM_PREFIX + id)] = normalized
            }
        }
    }

    suspend fun setMediumWidgetItems(itemIds: List<String>, appWidgetId: Int? = null) {
        val normalized = normalizeAssetIds(itemIds, 4)
        require(normalized.isNotEmpty()) { "At least one asset is required" }
        context.dataStore.edit { prefs ->
            val widgetKey = appWidgetId?.takeIf { it > 0 }?.let { stringPreferencesKey(WIDGET_MEDIUM_ITEMS_PREFIX + it) }
            if (widgetKey == null) prefs[KEY_WIDGET_MEDIUM_ITEMS] = normalized.joinToString(",")
            else prefs[widgetKey] = normalized.joinToString(",")
        }
    }

    suspend fun setLargeWidgetItems(itemIds: List<String>, appWidgetId: Int) {
        require(appWidgetId > 0) { "A valid widget id is required" }
        val normalized = normalizeAssetIds(itemIds, 6)
        require(normalized.isNotEmpty()) { "At least one asset is required" }
        context.dataStore.edit { prefs ->
            prefs[stringPreferencesKey(WIDGET_LARGE_ITEMS_PREFIX + appWidgetId)] = normalized.joinToString(",")
        }
    }

    suspend fun setWidgetTheme(themeId: String) = context.dataStore.edit { it[KEY_WIDGET_THEME] = themeId }
    suspend fun setWidgetStyle(themeId: String, opacity: Int, radius: Int) = context.dataStore.edit {
        it[KEY_WIDGET_THEME] = themeId
        it[KEY_WIDGET_OPACITY] = opacity.coerceIn(20, 100)
        it[KEY_WIDGET_CORNER_RADIUS] = radius.coerceIn(12, 28)
    }
    suspend fun updateLastSyncTime(timeMs: Long = System.currentTimeMillis()) = context.dataStore.edit { it[KEY_LAST_UPDATE_TIME] = timeMs }
    suspend fun setAppThemeMode(mode: String) {
        require(mode in setOf("light", "dark", "system")) { "Invalid theme mode" }
        context.dataStore.edit { it[KEY_APP_THEME_MODE] = mode }
    }

    suspend fun setForegroundRefreshMinutes(minutes: Int) {
        require(minutes in ALLOWED_FOREGROUND_REFRESH_MINUTES) { "Invalid refresh interval" }
        context.dataStore.edit { it[KEY_FOREGROUND_REFRESH_MINUTES] = minutes }
    }

    suspend fun setDiagnosticsConsent(enabled: Boolean) {
        check(com.chand.app.diagnostics.DiagnosticsReporter.applyConsent(context, enabled)) {
            "Crash reporting preference could not be applied"
        }
        try {
            context.dataStore.edit { it[KEY_DIAGNOSTICS_CONSENT] = enabled }
        } catch (error: Exception) {
            com.chand.app.diagnostics.DiagnosticsReporter.applyConsent(context, !enabled)
            throw error
        }
    }

    private fun parseAssetIds(value: String?, maxItems: Int): List<String> =
        value?.split(',')?.let { normalizeAssetIds(it, maxItems) } ?: emptyList()

    private fun parseCachedPrices(json: String?): List<PriceItem> {
        if (json == null) return emptyList()
        return try {
            val type = object : TypeToken<List<PriceItem>>() {}.type
            gson.fromJson<List<PriceItem>>(json, type)
                ?.filter { it.id.isNotBlank() && it.priceTomans > 0 } ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun normalizeAssetIds(itemIds: List<String>, maxItems: Int): List<String> =
        itemIds.map { it.trim().lowercase() }
            .filter { it.matches(Regex("[a-z0-9_-]{1,32}")) }
            .distinct()
            .take(maxItems)
}
