package com.chand.app.data.repository

import android.util.Log
import com.chand.app.data.local.PreferencesManager
import com.chand.app.data.model.PriceCategory
import com.chand.app.data.model.PriceItem
import com.chand.app.data.remote.PriceApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

enum class PriceSyncStatus { IDLE, REFRESHING, SUCCESS, STALE, ERROR }

class PriceRepository(
    private val apiService: PriceApiService,
    private val preferencesManager: PreferencesManager
) {
    companion object {
        private const val TAG = "PriceRepository"
        private val refreshMutex = Mutex()
        private val expectedCryptoIds = setOf("usdt", "btc", "eth", "ton", "trx", "sol", "doge")
    }

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _prices = MutableStateFlow<List<PriceItem>>(emptyList())
    val prices: StateFlow<List<PriceItem>> = _prices.asStateFlow()
    private val _syncStatus = MutableStateFlow(PriceSyncStatus.IDLE)
    val syncStatus: StateFlow<PriceSyncStatus> = _syncStatus.asStateFlow()
    val isRefreshing: StateFlow<Boolean> = MutableStateFlow(false).also { output ->
        repositoryScope.launch { syncStatus.collect { output.value = it == PriceSyncStatus.REFRESHING } }
    }.asStateFlow()

    private val priceHistoryMap = mutableMapOf<String, MutableList<Double>>()

    init {
        repositoryScope.launch {
            preferencesManager.cachedPricesFlow.collect { cached ->
                if (cached.isNotEmpty()) {
                    _prices.value = cached
                    cached.forEach { item ->
                        if (item.sparklinePoints.isNotEmpty()) priceHistoryMap[item.id] = item.sparklinePoints.toMutableList()
                    }
                    if (_syncStatus.value == PriceSyncStatus.IDLE) _syncStatus.value = PriceSyncStatus.STALE
                }
            }
        }
    }

    suspend fun refreshPrices(): Result<List<PriceItem>> = withContext(Dispatchers.IO) {
        refreshMutex.withLock {
            _syncStatus.value = PriceSyncStatus.REFRESHING
            try {
                if (_prices.value.isEmpty()) {
                    val cached = preferencesManager.cachedPricesFlow.first()
                    if (cached.isNotEmpty()) _prices.value = cached
                }
                val tgjuItems = apiService.fetchAllFromTgju()
                val tgjuCryptoIds = tgjuItems.filter { it.category == PriceCategory.CRYPTO }.map { it.id }.toSet()
                val missingCryptoIds = expectedCryptoIds - tgjuCryptoIds
                val fallbackCrypto = if (missingCryptoIds.isEmpty()) emptyList() else {
                    apiService.fetchCryptoFromNobitex().filter { it.id in missingCryptoIds }
                }
                val incoming = tgjuItems + fallbackCrypto
                if (incoming.isEmpty()) {
                    _syncStatus.value = if (_prices.value.isEmpty()) PriceSyncStatus.ERROR else PriceSyncStatus.STALE
                    return@withLock Result.failure(IllegalStateException("No valid market prices received"))
                }

                val currentMap = _prices.value.associateBy { it.id }.toMutableMap()
                val favorites = preferencesManager.favoritesFlow.first()
                val now = System.currentTimeMillis()
                incoming.forEach { item ->
                    val points = mergeObservedPrices(item, currentMap[item.id])
                    currentMap[item.id] = item.copy(
                        sparklinePoints = points,
                        lastUpdatedEpochMs = now,
                        isFavorite = item.id in favorites
                    )
                }
                val finalItems = currentMap.values.sortedWith(compareBy<PriceItem> { it.category.ordinal }.thenBy { it.symbol })
                _prices.value = finalItems
                preferencesManager.saveCachedPrices(finalItems)
                preferencesManager.updateLastSyncTime(now)
                _syncStatus.value = PriceSyncStatus.SUCCESS
                Result.success(finalItems)
            } catch (error: Exception) {
                Log.w(TAG, "Price refresh failed: ${error.javaClass.simpleName}")
                _syncStatus.value = if (_prices.value.isEmpty()) PriceSyncStatus.ERROR else PriceSyncStatus.STALE
                Result.failure(error)
            }
        }
    }

    private fun mergeObservedPrices(newItem: PriceItem, existing: PriceItem?): List<Double> {
        val history = priceHistoryMap.getOrPut(newItem.id) {
            existing?.sparklinePoints?.toMutableList() ?: mutableListOf()
        }
        val current = if (newItem.isUsd) newItem.effectiveUsdPrice else newItem.priceTomans.toDouble()
        if (current > 0 && (history.lastOrNull() == null || history.last() != current)) {
            history.add(current)
            while (history.size > 24) history.removeAt(0)
        }
        return history.toList()
    }

    suspend fun toggleFavorite(itemId: String) {
        preferencesManager.toggleFavorite(itemId)
        val favorites = preferencesManager.favoritesFlow.first()
        _prices.value = _prices.value.map { it.copy(isFavorite = it.id in favorites) }
    }

    fun getItemById(id: String): PriceItem? = _prices.value.find { it.id.equals(id, ignoreCase = true) }
}
