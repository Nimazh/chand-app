package com.chand.app.data.repository

import android.util.Log
import com.chand.app.data.local.PreferencesManager
import com.chand.app.data.model.PriceItem
import com.chand.app.data.remote.PriceApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PriceRepository(
    private val apiService: PriceApiService,
    private val preferencesManager: PreferencesManager
) {
    companion object {
        private const val TAG = "PriceRepository"
    }

    private val _prices = MutableStateFlow<List<PriceItem>>(emptyList())
    val prices: StateFlow<List<PriceItem>> = _prices.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val priceHistoryMap = mutableMapOf<String, MutableList<Double>>()

    init {
        // Initialize with realistic market baseline items immediately so UI is never blank
        val initialItems = PriceApiService.getMarketBaselineItems()
        _prices.value = initialItems
        for (item in initialItems) {
            priceHistoryMap[item.id] = item.sparklinePoints.toMutableList()
        }

        CoroutineScope(Dispatchers.IO).launch {
            val cached = preferencesManager.cachedPricesFlow.first()
            if (cached.isNotEmpty()) {
                _prices.value = cached
                for (item in cached) {
                    if (item.sparklinePoints.isNotEmpty()) {
                        priceHistoryMap[item.id] = item.sparklinePoints.toMutableList()
                    }
                }
            }
            applyFavoritesToPrices()
            refreshPrices()
        }
    }

    suspend fun refreshPrices(): Result<List<PriceItem>> = withContext(Dispatchers.IO) {
        _isRefreshing.value = true
        try {
            Log.d(TAG, "Starting price refresh...")

            // 1. Fetch live market prices from TGJU
            val liveTgjuItems = apiService.fetchAllFromTgju()

            // 2. Fetch crypto from Nobitex only if TGJU didn't return cryptos (avoids DNS timeout)
            val hasCryptoInTgju = liveTgjuItems.any { it.category == com.chand.app.data.model.PriceCategory.CRYPTO }
            val liveNobitexCrypto = if (!hasCryptoInTgju) {
                apiService.fetchCryptoFromNobitex()
            } else {
                emptyList()
            }

            // Check if network returned any data
            if (liveTgjuItems.isEmpty() && liveNobitexCrypto.isEmpty()) {
                Log.w(TAG, "Network returned empty, preserving existing prices without overwrite")
                _isRefreshing.value = false
                return@withContext Result.success(_prices.value)
            }

            // Start from current prices as baseline map to prevent any missing items
            val currentMap = _prices.value.associateBy { it.id }.toMutableMap()

            // Merge TGJU live items
            for (newItem in liveTgjuItems) {
                val existing = currentMap[newItem.id]
                val mergedPoints = mergeSparkline(newItem.id, newItem, existing)
                currentMap[newItem.id] = newItem.copy(sparklinePoints = mergedPoints)
            }

            // Merge Nobitex crypto items if any
            for (crypto in liveNobitexCrypto) {
                val existing = currentMap[crypto.id]
                val mergedPoints = mergeSparkline(crypto.id, crypto, existing)
                currentMap[crypto.id] = crypto.copy(sparklinePoints = mergedPoints)
            }

            val favs = preferencesManager.favoritesFlow.first()
            val finalItems = currentMap.values.map { item ->
                item.copy(isFavorite = favs.contains(item.id))
            }

            _prices.value = finalItems
            preferencesManager.saveCachedPrices(finalItems)
            preferencesManager.updateLastSyncTime()
            _isRefreshing.value = false
            Log.d(TAG, "Price refresh completed successfully with ${finalItems.size} items")
            Result.success(finalItems)
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing prices: ${e.message}", e)
            _isRefreshing.value = false
            Result.failure(e)
        }
    }

    private fun mergeSparkline(id: String, newItem: PriceItem, existing: PriceItem?): List<Double> {
        val history = priceHistoryMap.getOrPut(id) {
            (existing?.sparklinePoints ?: newItem.sparklinePoints).toMutableList()
        }

        val targetVal = if (newItem.category == com.chand.app.data.model.PriceCategory.CRYPTO && newItem.id != "usdt" && newItem.priceUsd != null) {
            newItem.priceUsd
        } else {
            newItem.priceTomans.toDouble()
        }

        if (history.isEmpty()) {
            history.addAll(newItem.sparklinePoints)
        } else {
            val lastRecorded = history.lastOrNull() ?: 0.0
            if (lastRecorded != targetVal) {
                history.add(targetVal)
                while (history.size > 12) {
                    history.removeAt(0)
                }
            }
        }
        return history.toList()
    }

    suspend fun toggleFavorite(itemId: String) {
        preferencesManager.toggleFavorite(itemId)
        applyFavoritesToPrices()
    }

    private suspend fun applyFavoritesToPrices() {
        val favs = preferencesManager.favoritesFlow.first()
        _prices.value = _prices.value.map { item ->
            item.copy(isFavorite = favs.contains(item.id))
        }
    }

    fun getItemById(id: String): PriceItem? {
        return _prices.value.find { it.id.equals(id, ignoreCase = true) }
    }
}
