package com.chand.app.data.repository

import android.util.Log
import com.chand.app.data.local.PreferencesManager
import com.chand.app.data.model.PriceCategory
import com.chand.app.data.model.PriceCatalog
import com.chand.app.data.model.PriceItem
import com.chand.app.data.remote.PriceApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

enum class PriceSyncStatus { IDLE, REFRESHING, SUCCESS, STALE, ERROR }
data class PriceRefreshOutcome(val items: List<PriceItem>, val pricesChanged: Boolean)

class PriceRepository(
    private val apiService: PriceApiService,
    private val preferencesManager: PreferencesManager,
    observeCache: Boolean = true
) {
    companion object {
        private const val TAG = "PriceRepository"
        private const val MIN_AUTO_REFRESH_GAP_MS = 30_000L
        private val refreshMutex = Mutex()
        private val expectedCryptoIds = setOf("usdt", "btc", "eth", "ton", "trx", "sol", "doge")
    }

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _prices = MutableStateFlow<List<PriceItem>>(emptyList())
    val prices: StateFlow<List<PriceItem>> = _prices.asStateFlow()
    private val _syncStatus = MutableStateFlow(PriceSyncStatus.IDLE)
    val syncStatus: StateFlow<PriceSyncStatus> = _syncStatus.asStateFlow()
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        if (observeCache) repositoryScope.launch {
            combine(
                preferencesManager.cachedPricesFlow,
                preferencesManager.favoritesFlow,
                preferencesManager.lastUpdateTimeFlow
            ) { cached, favorites, lastSync ->
                cached.map { it.copy(isFavorite = it.id in favorites) } to lastSync
            }.collect { (cached, lastSync) ->
                if (cached.isNotEmpty()) {
                    _prices.value = cached
                    if (_syncStatus.value == PriceSyncStatus.IDLE) {
                        setStatus(if (System.currentTimeMillis() - lastSync in 0 until 15 * 60_000L) {
                            PriceSyncStatus.SUCCESS
                        } else {
                            PriceSyncStatus.STALE
                        })
                    }
                }
            }
        }
    }

    suspend fun refreshPrices(force: Boolean = false): Result<PriceRefreshOutcome> = withContext(Dispatchers.IO) {
        refreshMutex.withLock {
            try {
                val cached = preferencesManager.cachedPricesFlow.first()
                val lastSync = preferencesManager.lastUpdateTimeFlow.first()
                val now = System.currentTimeMillis()
                if (!force && cached.isNotEmpty() && now - lastSync in 0 until MIN_AUTO_REFRESH_GAP_MS) {
                    return@withLock Result.success(PriceRefreshOutcome(cached, false))
                }
                setStatus(PriceSyncStatus.REFRESHING)
                val tgjuItems = apiService.fetchAllFromTgju()
                val tgjuCryptoIds = tgjuItems.filter { it.category == PriceCategory.CRYPTO }.map { it.id }.toSet()
                val missingCryptoIds = expectedCryptoIds - tgjuCryptoIds
                val fallbackCrypto = if (missingCryptoIds.isEmpty()) emptyList() else {
                    apiService.fetchCryptoFromNobitex().filter { it.id in missingCryptoIds }
                }
                val incoming = tgjuItems + fallbackCrypto
                if (incoming.isEmpty()) {
                    setStatus(if (cached.isEmpty()) PriceSyncStatus.ERROR else PriceSyncStatus.STALE)
                    return@withLock Result.failure(IllegalStateException("No valid market prices received"))
                }

                val workerQuotes = apiService.fetchCorePricesFromWorker().associateBy { it.id }
                val incomingWithCorePrices = incoming.associateBy { it.id }.toMutableMap()
                workerQuotes.forEach { (id, quote) ->
                    val base = incomingWithCorePrices[id] ?: cached.find { it.id == id }
                        ?: PriceCatalog.all.first { it.id == id }
                    incomingWithCorePrices[id] = base.copy(
                        priceTomans = quote.priceTomans,
                        lastUpdatedEpochMs = quote.fetchedAtEpochMs
                    )
                }

                val currentMap = cached.associateBy { it.id }.toMutableMap()
                val favorites = preferencesManager.favoritesFlow.first()
                var pricesChanged = false
                incomingWithCorePrices.values.forEach { item ->
                    val existing = currentMap[item.id]
                    if (existing == null || !existing.hasSameQuoteAs(item)) {
                        pricesChanged = true
                        currentMap[item.id] = item.copy(
                            sparklinePoints = mergeObservedPrices(item, existing),
                            lastUpdatedEpochMs = item.lastUpdatedEpochMs.takeIf { it > 0 } ?: now,
                            isFavorite = item.id in favorites
                        )
                    }
                }
                val finalItems = currentMap.values.sortedWith(compareBy<PriceItem> { it.category.ordinal }.thenBy { it.symbol })
                if (pricesChanged) {
                    preferencesManager.saveCachedPrices(finalItems)
                    _prices.value = finalItems
                }
                preferencesManager.updateLastSyncTime(now)
                setStatus(PriceSyncStatus.SUCCESS)
                Result.success(PriceRefreshOutcome(finalItems, pricesChanged))
            } catch (cancelled: CancellationException) {
                setStatus(if (_prices.value.isEmpty()) PriceSyncStatus.IDLE else PriceSyncStatus.STALE)
                throw cancelled
            } catch (error: Exception) {
                Log.w(TAG, "Price refresh failed: ${error.javaClass.simpleName}")
                setStatus(if (preferencesManager.cachedPricesFlow.first().isEmpty()) PriceSyncStatus.ERROR else PriceSyncStatus.STALE)
                Result.failure(error)
            }
        }
    }

    /** Polls only the three centrally collected prices while the activity is visible. */
    suspend fun refreshCorePrices(): Result<PriceRefreshOutcome> {
        val quotes = apiService.fetchCorePricesFromWorker()
        if (quotes.isEmpty()) return Result.failure(IllegalStateException("No recent core prices from Worker"))
        return withContext(Dispatchers.IO) {
            refreshMutex.withLock {
                val cached = preferencesManager.cachedPricesFlow.first()
                val favorites = preferencesManager.favoritesFlow.first()
                val currentMap = cached.associateBy { it.id }.toMutableMap()
                var changed = false
                quotes.forEach { quote ->
                    val base = currentMap[quote.id] ?: PriceCatalog.all.first { it.id == quote.id }
                    if (base.priceTomans != quote.priceTomans) {
                        val updated = base.copy(
                            priceTomans = quote.priceTomans,
                            lastUpdatedEpochMs = quote.fetchedAtEpochMs,
                            isFavorite = quote.id in favorites
                        )
                        currentMap[quote.id] = updated.copy(
                            sparklinePoints = mergeObservedPrices(updated, base)
                        )
                        changed = true
                    }
                }
                val items = currentMap.values.sortedWith(compareBy<PriceItem> { it.category.ordinal }.thenBy { it.symbol })
                if (changed) {
                    preferencesManager.saveCachedPrices(items)
                    _prices.value = items
                }
                Result.success(PriceRefreshOutcome(items, changed))
            }
        }
    }

    private fun mergeObservedPrices(newItem: PriceItem, existing: PriceItem?): List<Double> {
        val history = existing?.sparklinePoints?.toMutableList() ?: mutableListOf()
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

    fun close() = repositoryScope.cancel()

    private fun setStatus(status: PriceSyncStatus) {
        _syncStatus.value = status
        _isRefreshing.value = status == PriceSyncStatus.REFRESHING
    }

    private fun PriceItem.hasSameQuoteAs(other: PriceItem): Boolean =
        priceTomans == other.priceTomans && priceUsd == other.priceUsd &&
            change24hPercent == other.change24hPercent && high24h == other.high24h &&
            low24h == other.low24h && changeAmount == other.changeAmount
}
