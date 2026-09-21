package com.chand.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chand.app.data.model.PriceCategory
import com.chand.app.data.model.PriceItem
import com.chand.app.ui.components.CategoryTabBar
import com.chand.app.ui.components.ChandHeader
import com.chand.app.ui.components.LiquidGlassAtmosphere
import com.chand.app.ui.components.PriceCard
import com.chand.app.ui.components.PriceDetailModal
import com.chand.app.ui.theme.AppleBackground
import com.chand.app.ui.theme.AppleTextPrimary
import com.chand.app.ui.theme.AppleTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onOpenSettings: () -> Unit
) {
    val items by viewModel.displayedPrices.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val lastUpdatedText by viewModel.formattedLastUpdateTime.collectAsState()
    val selectedDetailItem by viewModel.selectedItemForDetail.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleBackground)
    ) {
        LiquidGlassAtmosphere()

        Scaffold(
            containerColor = AppleBackground,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                ChandHeader(
                    searchQuery = searchQuery,
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    isSearchActive = isSearchActive,
                    onToggleSearch = viewModel::toggleSearch,
                    isRefreshing = isRefreshing,
                    onRefresh = viewModel::refreshPrices,
                    onOpenSettings = onOpenSettings,
                    lastUpdatedText = lastUpdatedText,
                    syncStatus = syncStatus
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
            ) {
                // Apple Segmented Control Category Tabs
                CategoryTabBar(
                    selectedCategory = selectedCategory,
                    onCategorySelected = viewModel::selectCategory
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Apple 2-Column Grid of Squircle Cards (Matches Photo 1 & 2)
                if (items.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotEmpty()) "موردی با این جستجو پیدا نشد" else "آیتمی در این دسته وجود ندارد",
                            color = AppleTextSecondary,
                            fontSize = 15.sp
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (selectedCategory == PriceCategory.ALL && searchQuery.isEmpty()) {
                            // Section-based display matching Photo 1 & 2
                            val currencies = items.filter { it.category == PriceCategory.CURRENCY }
                            val gold = items.filter { it.category == PriceCategory.GOLD }
                            val crypto = items.filter { it.category == PriceCategory.CRYPTO }

                            if (currencies.isNotEmpty()) {
                                item(span = { GridItemSpan(2) }, key = "header_currencies") {
                                    SectionHeader(title = "💵 ارزها (Currencies)")
                                }
                                items(currencies, key = { it.id }) { item ->
                                    PriceCard(
                                        item = item,
                                        onClick = { viewModel.openItemDetail(item) },
                                        onToggleFavorite = { viewModel.toggleFavorite(item) }
                                    )
                                }
                            }

                            if (gold.isNotEmpty()) {
                                item(span = { GridItemSpan(2) }, key = "header_gold") {
                                    SectionHeader(title = "🪙 طلا و سکه (Gold & Coins)")
                                }
                                items(gold, key = { it.id }) { item ->
                                    PriceCard(
                                        item = item,
                                        onClick = { viewModel.openItemDetail(item) },
                                        onToggleFavorite = { viewModel.toggleFavorite(item) }
                                    )
                                }
                            }

                            if (crypto.isNotEmpty()) {
                                item(span = { GridItemSpan(2) }, key = "header_crypto") {
                                    SectionHeader(title = "💎 کریپتو (Cryptocurrency)")
                                }
                                items(crypto, key = { it.id }) { item ->
                                    PriceCard(
                                        item = item,
                                        onClick = { viewModel.openItemDetail(item) },
                                        onToggleFavorite = { viewModel.toggleFavorite(item) }
                                    )
                                }
                            }
                        } else {
                            // Single category or search query mode
                            items(items, key = { it.id }) { item ->
                                PriceCard(
                                    item = item,
                                    onClick = { viewModel.openItemDetail(item) },
                                    onToggleFavorite = { viewModel.toggleFavorite(item) }
                                )
                            }
                        }

                        item(span = { GridItemSpan(2) }) {
                            Spacer(modifier = Modifier.height(28.dp))
                            Spacer(modifier = Modifier.navigationBarsPadding())
                        }
                    }
                }
            }

            // Detailed Modal BottomSheet
            if (selectedDetailItem != null) {
                PriceDetailModal(
                    item = selectedDetailItem,
                    sheetState = sheetState,
                    onDismiss = viewModel::closeItemDetail,
                    onToggleFavorite = viewModel::toggleFavorite,
                    onSetAsSmallWidget = viewModel::setAsSmallWidget
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 4.dp, start = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = AppleTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
