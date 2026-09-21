package com.chand.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.chand.app.data.model.PriceItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "chand_settings")

class PreferencesManager(private val context: Context) {
    companion object {
        private val KEY_FAVORITES = stringSetPreferencesKey("favorites")
        private val KEY_WIDGET_SMALL_ITEM = stringPreferencesKey("widget_small_item")
        private const val WIDGET_SMALL_ITEM_PREFIX = "widget_small_item_"
        private val KEY_WIDGET_MEDIUM_ITEMS = stringPreferencesKey("widget_medium_items_order")
        private val KEY_LAST_UPDATE_TIME = longPreferencesKey("last_update_time")
        private val KEY_APP_THEME_MODE = stringPreferencesKey("app_theme_mode")
        private val KEY_CACHED_PRICES_JSON = stringPreferencesKey("cached_prices_json")
        private val KEY_WIDGET_THEME = stringPreferencesKey("widget_theme")
        private val KEY_WIDGET_OPACITY = intPreferencesKey("widget_opacity")
        private val KEY_WIDGET_CORNER_RADIUS = intPreferencesKey("widget_corner_radius")

        val DEFAULT_FAVORITES = setOf("usd", "gold18", "emami", "usdt", "btc")
        val DEFAULT_MEDIUM_ITEMS = listOf("usd", "gold18", "emami", "usdt")
        const val DEFAULT_WIDGET_THEME = "apple_white"
        const val DEFAULT_WIDGET_OPACITY = 100
        const val DEFAULT_WIDGET_CORNER_RADIUS = 22
        const val DEFAULT_APP_THEME_MODE = "system"
    }

    private val gson = Gson()

    val favoritesFlow: Flow<Set<String>> = context.dataStore.data.map { it[KEY_FAVORITES] ?: DEFAULT_FAVORITES }
    val widgetThemeFlow: Flow<String> = context.dataStore.data.map { it[KEY_WIDGET_THEME] ?: DEFAULT_WIDGET_THEME }
    val widgetOpacityFlow: Flow<Int> = context.dataStore.data.map { it[KEY_WIDGET_OPACITY] ?: DEFAULT_WIDGET_OPACITY }
    val widgetCornerRadiusFlow: Flow<Int> = context.dataStore.data.map { it[KEY_WIDGET_CORNER_RADIUS] ?: DEFAULT_WIDGET_CORNER_RADIUS }

    fun smallWidgetItemFlow(appWidgetId: Int? = null): Flow<String> = context.dataStore.data.map { prefs ->
        val widgetKey = appWidgetId?.takeIf { it > 0 }?.let { stringPreferencesKey(WIDGET_SMALL_ITEM_PREFIX + it) }
        (widgetKey?.let { prefs[it] } ?: prefs[KEY_WIDGET_SMALL_ITEM] ?: "usd").lowercase()
    }

    val mediumWidgetItemsFlow: Flow<List<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_WIDGET_MEDIUM_ITEMS]?.split(',')?.map(String::trim)?.filter(String::isNotBlank)
            ?.distinct()?.take(4)?.takeIf { it.isNotEmpty() } ?: DEFAULT_MEDIUM_ITEMS
    }
    val appThemeModeFlow: Flow<String> = context.dataStore.data.map { it[KEY_APP_THEME_MODE] ?: DEFAULT_APP_THEME_MODE }
    val lastUpdateTimeFlow: Flow<Long> = context.dataStore.data.map { it[KEY_LAST_UPDATE_TIME] ?: 0L }

    val cachedPricesFlow: Flow<List<PriceItem>> = context.dataStore.data.map { prefs ->
        val json = prefs[KEY_CACHED_PRICES_JSON] ?: return@map emptyList()
        try {
            val type = object : TypeToken<List<PriceItem>>() {}.type
            gson.fromJson<List<PriceItem>>(json, type)
                ?.filter { it.id.isNotBlank() && it.priceTomans > 0 } ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
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

    suspend fun setMediumWidgetItems(itemIds: List<String>) {
        val normalized = itemIds.map { it.trim().lowercase() }.filter { it.matches(Regex("[a-z0-9_-]{1,32}")) }
            .distinct().take(4)
        require(normalized.isNotEmpty()) { "At least one asset is required" }
        context.dataStore.edit { it[KEY_WIDGET_MEDIUM_ITEMS] = normalized.joinToString(",") }
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
}
