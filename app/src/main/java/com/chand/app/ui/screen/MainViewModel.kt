package com.chand.app.ui.screen

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chand.app.data.local.PreferencesManager
import com.chand.app.data.model.PriceCategory
import com.chand.app.data.model.PriceItem
import com.chand.app.data.remote.PriceApiService
import com.chand.app.data.repository.PriceRepository
import com.chand.app.data.repository.PriceSyncStatus
import com.chand.app.widget.ChandWidgetUpdater
import com.chand.app.widget.ChandSmallWidgetReceiver
import com.chand.app.widget.ChandWidgetType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)
    private val apiService = PriceApiService()
    val repository = PriceRepository(apiService, preferencesManager)

    val formattedLastUpdateTime: StateFlow<String> = preferencesManager.lastUpdateTimeFlow
        .map { timeMs -> formatTimestamp(timeMs) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val appThemeMode: StateFlow<String> = preferencesManager.appThemeModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PreferencesManager.DEFAULT_APP_THEME_MODE)

    val foregroundRefreshMinutes: StateFlow<Int> = preferencesManager.foregroundRefreshMinutesFlow
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            PreferencesManager.DEFAULT_FOREGROUND_REFRESH_MINUTES
        )

    private var foregroundRefreshJob: Job? = null
    private var coreRefreshJob: Job? = null

    fun setAppThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesManager.setAppThemeMode(mode)
        }
    }

    private val _selectedCategory = MutableStateFlow(PriceCategory.ALL)
    val selectedCategory: StateFlow<PriceCategory> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    private val _selectedItemId = MutableStateFlow<String?>(null)
    val selectedItemForDetail: StateFlow<PriceItem?> = combine(repository.prices, _selectedItemId) { prices, id ->
        id?.let { selectedId -> prices.find { it.id.equals(selectedId, ignoreCase = true) } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isRefreshing: StateFlow<Boolean> = repository.isRefreshing
    val syncStatus: StateFlow<PriceSyncStatus> = repository.syncStatus

    // Filtered list based on category and search text
    val displayedPrices: StateFlow<List<PriceItem>> = combine(
        repository.prices,
        _selectedCategory,
        _searchQuery
    ) { prices, category, query ->
        prices.filter { item ->
            val matchesCategory = when (category) {
                PriceCategory.ALL -> true
                PriceCategory.WATCHLIST -> item.isFavorite
                else -> item.category == category
            }
            val normalizedQuery = normalizeSearch(query)
            val matchesQuery = normalizedQuery.isBlank() ||
                    normalizeSearch(item.nameFa).contains(normalizedQuery) ||
                    normalizeSearch(item.effectiveNameEn).contains(normalizedQuery) ||
                    item.symbol.contains(query.trim(), ignoreCase = true)

            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectCategory(category: PriceCategory) {
        _selectedCategory.value = category
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun toggleSearch() {
        _isSearchActive.value = !_isSearchActive.value
        if (!_isSearchActive.value) {
            _searchQuery.value = ""
        }
    }

    fun openItemDetail(item: PriceItem) {
        _selectedItemId.value = item.id
    }

    fun closeItemDetail() {
        _selectedItemId.value = null
    }

    fun refreshPrices() {
        viewModelScope.launch {
            refreshPricesAndWidgets(force = true)
        }
    }

    /** Refreshes only while the app has a visible activity. Android limits background periodic work separately. */
    fun startForegroundAutoRefresh() {
        if (coreRefreshJob?.isActive != true) {
            coreRefreshJob = viewModelScope.launch {
                while (isActive) {
                    try {
                        val outcome = repository.refreshCorePrices().getOrNull()
                        if (outcome?.pricesChanged == true) updateAllWidgets()
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (error: Exception) {
                        Log.w("MainViewModel", "Core price refresh failed", error)
                    }
                    delay(30_000L)
                }
            }
        }
        if (foregroundRefreshJob?.isActive == true) return
        foregroundRefreshJob = viewModelScope.launch {
            preferencesManager.foregroundRefreshMinutesFlow.collectLatest { minutes ->
                val intervalMs = minutes * 60_000L
                while (true) {
                    val lastSync = preferencesManager.lastUpdateTimeFlow.first()
                    val elapsed = System.currentTimeMillis() - lastSync
                    if (lastSync > 0 && elapsed in 0 until intervalMs) {
                        delay(intervalMs - elapsed)
                    }
                    refreshPricesAndWidgets()
                    delay(intervalMs)
                }
            }
        }
    }

    fun stopForegroundAutoRefresh() {
        coreRefreshJob?.cancel()
        coreRefreshJob = null
        foregroundRefreshJob?.cancel()
        foregroundRefreshJob = null
    }

    fun setForegroundRefreshMinutes(minutes: Int) {
        viewModelScope.launch { preferencesManager.setForegroundRefreshMinutes(minutes) }
    }

    fun toggleFavorite(item: PriceItem) {
        viewModelScope.launch {
            repository.toggleFavorite(item.id)
            updateAllWidgets()
        }
    }

    fun setAsSmallWidget(item: PriceItem) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val ids = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(ComponentName(context, ChandSmallWidgetReceiver::class.java))
            preferencesManager.setSmallWidgetItemForAll(item.id, ids)
            ChandWidgetUpdater.updateType(context, ChandWidgetType.SMALL)
            closeItemDetail()
        }
    }

    private suspend fun updateAllWidgets() {
        try {
            val context = getApplication<Application>()
            ChandWidgetUpdater.updateAll(context)
        } catch (error: Exception) {
            Log.w("MainViewModel", "Widget update failed", error)
        }
    }

    private suspend fun refreshPricesAndWidgets(force: Boolean = false) {
        val outcome = repository.refreshPrices(force).getOrNull()
        if (outcome?.pricesChanged == true) updateAllWidgets()
    }

    override fun onCleared() {
        stopForegroundAutoRefresh()
        repository.close()
        super.onCleared()
    }

    companion object {
        private fun normalizeSearch(value: String): String = value.trim().lowercase()
            .replace('ي', 'ی').replace('ك', 'ک')

        fun formatTimestamp(timeMs: Long): String {
            if (timeMs <= 0) return ""
            val cal = java.util.Calendar.getInstance().apply { timeInMillis = timeMs }
            val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
            val minute = cal.get(java.util.Calendar.MINUTE)
            val second = cal.get(java.util.Calendar.SECOND)
            return String.format(java.util.Locale.US, "%02d:%02d:%02d", hour, minute, second)
                .replace('0', '۰')
                .replace('1', '۱')
                .replace('2', '۲')
                .replace('3', '۳')
                .replace('4', '۴')
                .replace('5', '۵')
                .replace('6', '۶')
                .replace('7', '۷')
                .replace('8', '۸')
                .replace('9', '۹')
        }
    }
}
