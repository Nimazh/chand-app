package com.chand.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.chand.app.data.model.PriceItem
import com.chand.app.data.remote.PriceApiService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "chand_settings")

class PreferencesManager(private val context: Context) {

    companion object {
        private val KEY_FAVORITES = stringSetPreferencesKey("favorites")
        private val KEY_WIDGET_SMALL_ITEM = stringPreferencesKey("widget_small_item")
        private val KEY_WIDGET_MEDIUM_ITEMS = stringSetPreferencesKey("widget_medium_items")
        private val KEY_CUSTOM_API_URL = stringPreferencesKey("custom_api_url")
        private val KEY_LAST_UPDATE_TIME = longPreferencesKey("last_update_time")
        private val KEY_DARK_THEME_FORCED = booleanPreferencesKey("dark_theme_forced")
        private val KEY_APP_THEME_MODE = stringPreferencesKey("app_theme_mode")
        private val KEY_CACHED_PRICES_JSON = stringPreferencesKey("cached_prices_json")
        private val KEY_WIDGET_THEME = stringPreferencesKey("widget_theme")
        private val KEY_WIDGET_OPACITY = androidx.datastore.preferences.core.intPreferencesKey("widget_opacity")
        private val KEY_WIDGET_CORNER_RADIUS = androidx.datastore.preferences.core.intPreferencesKey("widget_corner_radius")

        val DEFAULT_FAVORITES = setOf("usd", "gold18", "emami", "usdt", "btc")
        val DEFAULT_MEDIUM_ITEMS = setOf("usd", "gold18", "emami", "usdt")
        const val DEFAULT_WIDGET_THEME = "apple_white"
        const val DEFAULT_WIDGET_OPACITY = 100
        const val DEFAULT_WIDGET_CORNER_RADIUS = 22
        const val DEFAULT_APP_THEME_MODE = "system"
    }

    private val gson = Gson()

    val favoritesFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_FAVORITES] ?: DEFAULT_FAVORITES
    }

    val widgetThemeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_WIDGET_THEME] ?: DEFAULT_WIDGET_THEME
    }

    val widgetOpacityFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_WIDGET_OPACITY] ?: DEFAULT_WIDGET_OPACITY
    }

    val widgetCornerRadiusFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_WIDGET_CORNER_RADIUS] ?: DEFAULT_WIDGET_CORNER_RADIUS
    }

    val smallWidgetItemFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_WIDGET_SMALL_ITEM] ?: "usd"
    }

    val mediumWidgetItemsFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_WIDGET_MEDIUM_ITEMS] ?: DEFAULT_MEDIUM_ITEMS
    }

    val customApiUrlFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_CUSTOM_API_URL] ?: ""
    }

    val appThemeModeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_APP_THEME_MODE] ?: DEFAULT_APP_THEME_MODE
    }

    val lastUpdateTimeFlow: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[KEY_LAST_UPDATE_TIME] ?: System.currentTimeMillis()
    }

    val cachedPricesFlow: Flow<List<PriceItem>> = context.dataStore.data.map { prefs ->
        val json = prefs[KEY_CACHED_PRICES_JSON]
        if (!json.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<PriceItem>>() {}.type
                val rawList = gson.fromJson<List<PriceItem>>(json, type)
                rawList?.map { item ->
                    PriceItem(
                        id = item.id ?: "usd",
                        nameFa = item.nameFa ?: "",
                        symbol = item.symbol ?: "",
                        category = item.category ?: com.chand.app.data.model.PriceCategory.CURRENCY,
                        priceTomans = item.priceTomans,
                        change24hPercent = item.change24hPercent,
                        high24h = item.high24h,
                        low24h = item.low24h,
                        lastUpdatedEpochMs = item.lastUpdatedEpochMs,
                        isFavorite = item.isFavorite,
                        sparklinePoints = item.sparklinePoints ?: emptyList(),
                        priceUsd = item.priceUsd,
                        nameEn = item.nameEn ?: "",
                        changeAmount = item.changeAmount ?: 0L
                    )
                } ?: PriceApiService.getMarketBaselineItems()
            } catch (e: Exception) {
                PriceApiService.getMarketBaselineItems()
            }
        } else {
            PriceApiService.getMarketBaselineItems()
        }
    }

    suspend fun saveCachedPrices(items: List<PriceItem>) {
        if (items.isEmpty()) return
        context.dataStore.edit { prefs ->
            prefs[KEY_CACHED_PRICES_JSON] = gson.toJson(items)
        }
    }

    suspend fun toggleFavorite(itemId: String) {
        context.dataStore.edit { prefs ->
            val current = (prefs[KEY_FAVORITES] ?: DEFAULT_FAVORITES).toMutableSet()
            if (current.contains(itemId)) {
                current.remove(itemId)
            } else {
                current.add(itemId)
            }
            prefs[KEY_FAVORITES] = current
        }
    }

    suspend fun setSmallWidgetItem(itemId: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WIDGET_SMALL_ITEM] = itemId
        }
    }

    suspend fun setMediumWidgetItems(itemIds: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WIDGET_MEDIUM_ITEMS] = itemIds
        }
    }

    suspend fun setCustomApiUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CUSTOM_API_URL] = url
        }
    }

    suspend fun setWidgetTheme(themeId: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WIDGET_THEME] = themeId
        }
    }

    suspend fun setWidgetOpacity(opacity: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WIDGET_OPACITY] = opacity
        }
    }

    suspend fun setWidgetCornerRadius(radius: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WIDGET_CORNER_RADIUS] = radius
        }
    }

    suspend fun setWidgetStyle(themeId: String, opacity: Int, radius: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WIDGET_THEME] = themeId
            prefs[KEY_WIDGET_OPACITY] = opacity
            prefs[KEY_WIDGET_CORNER_RADIUS] = radius
        }
    }

    suspend fun updateLastSyncTime(timeMs: Long = System.currentTimeMillis()) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LAST_UPDATE_TIME] = timeMs
        }
    }

    suspend fun setAppThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_APP_THEME_MODE] = mode
        }
    }
}
