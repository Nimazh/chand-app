package com.chand.app.data.remote

import android.util.Log
import com.chand.app.data.model.PriceCategory
import com.chand.app.data.model.PriceItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class PriceApiService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build(),
    private val gson: Gson = Gson()
) {
    companion object {
        private const val TAG = "PriceApiService"
        private val TGJU_ENDPOINTS = listOf(
            "https://call.tgju.org/ajax.json",
            "https://call1.tgju.org/ajax.json",
            "https://call2.tgju.org/ajax.json",
            "https://call3.tgju.org/ajax.json"
        )

        /**
         * Static preview-only data for the widget configuration mockup.
         * This must never be used by the price repository or production widgets.
         */
        fun getPreviewItems(): List<PriceItem> {
            val items = mutableListOf<PriceItem>()

            // Currencies (Tomans) - Baseline matches Apple Chand screenshot (USD: 100,115 and ↓5,625)
            items.add(PriceItem("usd", "دلار آمریکا", "USD", PriceCategory.CURRENCY, 100115L, -0.26, 100800L, 99125L, isFavorite = true, sparklinePoints = listOf(105890.0, 104200.0, 102100.0, 100115.0), changeAmount = -5625L))
            items.add(PriceItem("eur", "یورو", "EUR", PriceCategory.CURRENCY, 264050L, 0.67, 264750L, 260700L, isFavorite = true, sparklinePoints = listOf(261000.0, 262500.0, 263800.0, 264050.0)))
            items.add(PriceItem("aed", "درهم امارات", "AED", PriceCategory.CURRENCY, 62550L, 0.31, 62710L, 61970L, isFavorite = true, sparklinePoints = listOf(62000.0, 62200.0, 62450.0, 62550.0)))
            items.add(PriceItem("gbp", "پوند انگلیس", "GBP", PriceCategory.CURRENCY, 307880L, 0.97, 309000L, 304500L, sparklinePoints = listOf(305000.0, 306200.0, 307880.0)))
            items.add(PriceItem("cad", "دلار کانادا", "CAD", PriceCategory.CURRENCY, 164290L, 0.83, 165000L, 162500L, sparklinePoints = listOf(163000.0, 163800.0, 164290.0)))
            items.add(PriceItem("try", "لیر ترکیه", "TRY", PriceCategory.CURRENCY, 4785L, 0.84, 4850L, 4720L, sparklinePoints = listOf(4730.0, 4750.0, 4785.0)))
            items.add(PriceItem("cny", "یوان چین", "CNY", PriceCategory.CURRENCY, 31800L, 0.25, 32100L, 31500L, sparklinePoints = listOf(31600.0, 31750.0, 31800.0)))
            items.add(PriceItem("iqd", "دینار عراق (۱۰۰)", "IQD", PriceCategory.CURRENCY, 17400L, 0.15, 17600L, 17300L, sparklinePoints = listOf(17350.0, 17380.0, 17400.0)))

            // Gold & Coins (Tomans)
            items.add(PriceItem("emami", "سکه امامی", "EMAMI", PriceCategory.GOLD, 235500000L, 1.52, 236000000L, 233500000L, isFavorite = true, sparklinePoints = listOf(233500000.0, 234200000.0, 235500000.0)))
            items.add(PriceItem("bahar", "سکه بهار آزادی", "BAHAR", PriceCategory.GOLD, 231270000L, 1.39, 232000000L, 229500000L, sparklinePoints = listOf(229500000.0, 230500000.0, 231270000.0)))
            items.add(PriceItem("nim", "نیم سکه", "NIM", PriceCategory.GOLD, 119500000L, 0.42, 120500000L, 118500000L, sparklinePoints = listOf(118500000.0, 119000000.0, 119500000.0)))
            items.add(PriceItem("rob", "ربع سکه", "ROB", PriceCategory.GOLD, 63000000L, 0.10, 63500000L, 62500000L, isFavorite = true, sparklinePoints = listOf(62600000.0, 62800000.0, 63000000.0)))
            items.add(PriceItem("gerami", "سکه گرمی", "GERAMI", PriceCategory.GOLD, 33000000L, 0.00, 33500000L, 32800000L, sparklinePoints = listOf(33000000.0, 33000000.0, 33000000.0)))
            items.add(PriceItem("gold18", "طلای ۱۸ عیار", "GOLD18", PriceCategory.GOLD, 23750000L, 1.66, 23820000L, 23400000L, isFavorite = true, sparklinePoints = listOf(23400000.0, 23550000.0, 23750000.0)))
            items.add(PriceItem("gold24", "طلای ۲۴ عیار", "GOLD24", PriceCategory.GOLD, 31660000L, 1.66, 31750000L, 31200000L, sparklinePoints = listOf(31200000.0, 31400000.0, 31660000.0)))
            items.add(PriceItem("mesghal", "مثقال طلا", "MESGHAL", PriceCategory.GOLD, 102885000L, 1.67, 103100000L, 101800000L, sparklinePoints = listOf(101800000.0, 102200000.0, 102885000.0)))

            // Cryptocurrencies (Tether in Tomans, other cryptos in USD)
            items.add(PriceItem("usdt", "تتر", "USDT", PriceCategory.CRYPTO, 229180L, 0.92, 229800L, 227500L, isFavorite = true, sparklinePoints = listOf(227500.0, 228200.0, 229180.0), priceUsd = 1.0))
            items.add(PriceItem("btc", "بیت‌کوین", "BTC", PriceCategory.CRYPTO, 19320000000L, 1.33, 19500000000L, 19100000000L, isFavorite = true, sparklinePoints = listOf(83200.0, 83800.0, 84300.0), priceUsd = 84300.0))
            items.add(PriceItem("eth", "اتریوم", "ETH", PriceCategory.CRYPTO, 607300000L, 1.68, 612000000L, 598000000L, sparklinePoints = listOf(2610.0, 2630.0, 2650.0), priceUsd = 2650.0))
            items.add(PriceItem("ton", "تون‌کوین", "TON", PriceCategory.CRYPTO, 508700L, 0.81, 515000L, 502000L, sparklinePoints = listOf(2.18, 2.20, 2.22), priceUsd = 2.22))
            items.add(PriceItem("trx", "ترون", "TRX", PriceCategory.CRYPTO, 59580L, 0.45, 60200L, 58900L, sparklinePoints = listOf(0.255, 0.258, 0.260), priceUsd = 0.260))
            items.add(PriceItem("sol", "سولانا", "SOL", PriceCategory.CRYPTO, 38502000L, 2.10, 39200000L, 37800000L, sparklinePoints = listOf(164.0, 166.5, 168.0), priceUsd = 168.0))
            items.add(PriceItem("doge", "دوج‌کوین", "DOGE", PriceCategory.CRYPTO, 45370L, 1.15, 46000L, 44500L, sparklinePoints = listOf(0.192, 0.195, 0.198), priceUsd = 0.198))

            return items
        }
    }

    /**
     * Fetches all live rates (Currencies, Gold, Coins, and Crypto) from TGJU real-time ajax endpoints.
     */
    suspend fun fetchAllFromTgju(): List<PriceItem> = withContext(Dispatchers.IO) {
        for (url in TGJU_ENDPOINTS) {
            currentCoroutineContext().ensureActive()
            try {
                val urlWithTimestamp = if (url.contains("?")) "$url&_=${System.currentTimeMillis()}" else "$url?_=${System.currentTimeMillis()}"
                val request = Request.Builder()
                    .url(urlWithTimestamp)
                    .cacheControl(CacheControl.FORCE_NETWORK)
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .addHeader("Accept", "application/json")
                    .addHeader("Cache-Control", "no-cache, no-store, must-revalidate")
                    .addHeader("Pragma", "no-cache")
                    .addHeader("Referer", "https://www.tgju.org/")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.w(TAG, "Failed response from $url: ${response.code}")
                        return@use
                    }
                    val body = response.body?.string() ?: return@use
                    val type = object : TypeToken<Map<String, Any>>() {}.type
                    val rootData: Map<String, Any> = gson.fromJson(body, type)
                    @Suppress("UNCHECKED_CAST")
                    val current = rootData["current"] as? Map<String, Any> ?: return@use

                    val items = mutableListOf<PriceItem>()

                    // Definition mapping: TGJU key -> (id, NameFa, Symbol, Category)
                    val definitions = listOf(
                        // Currencies
                        ItemDef("price_dollar_rl", "usd", "دلار آمریکا", "USD", PriceCategory.CURRENCY, true),
                        ItemDef("price_eur", "eur", "یورو", "EUR", PriceCategory.CURRENCY, true),
                        ItemDef("price_aed", "aed", "درهم امارات", "AED", PriceCategory.CURRENCY, true),
                        ItemDef("price_gbp", "gbp", "پوند انگلیس", "GBP", PriceCategory.CURRENCY, false),
                        ItemDef("price_try", "try", "لیر ترکیه", "TRY", PriceCategory.CURRENCY, false),
                        ItemDef("price_cad", "cad", "دلار کانادا", "CAD", PriceCategory.CURRENCY, false),
                        ItemDef("price_cny", "cny", "یوان چین", "CNY", PriceCategory.CURRENCY, false),
                        ItemDef("price_iqd", "iqd", "دینار عراق (۱۰۰)", "IQD", PriceCategory.CURRENCY, false),

                        // Gold & Coins
                        ItemDef("sekee", "emami", "سکه امامی", "EMAMI", PriceCategory.GOLD, true),
                        ItemDef("sekeb", "bahar", "سکه بهار آزادی", "BAHAR", PriceCategory.GOLD, false),
                        ItemDef("nim", "nim", "نیم سکه", "NIM", PriceCategory.GOLD, false),
                        ItemDef("rob", "rob", "ربع سکه", "ROB", PriceCategory.GOLD, true),
                        ItemDef("gerami", "gerami", "سکه گرمی", "GERAMI", PriceCategory.GOLD, false),
                        ItemDef("geram18", "gold18", "طلای ۱۸ عیار", "GOLD18", PriceCategory.GOLD, true),
                        ItemDef("geram24", "gold24", "طلای ۲۴ عیار", "GOLD24", PriceCategory.GOLD, false),
                        ItemDef("mesghal", "mesghal", "مثقال طلا", "MESGHAL", PriceCategory.GOLD, false),

                        // Cryptocurrencies
                        ItemDef("crypto-tether-irr", "usdt", "تتر", "USDT", PriceCategory.CRYPTO, true),
                        ItemDef("crypto-bitcoin-irr", "btc", "بیت‌کوین", "BTC", PriceCategory.CRYPTO, true),
                        ItemDef("crypto-ethereum-irr", "eth", "اتریوم", "ETH", PriceCategory.CRYPTO, false),
                        ItemDef("crypto-toncoin-irr", "ton", "تون‌کوین", "TON", PriceCategory.CRYPTO, false),
                        ItemDef("crypto-tron-irr", "trx", "ترون", "TRX", PriceCategory.CRYPTO, false),
                        ItemDef("crypto-solana-irr", "sol", "سولانا", "SOL", PriceCategory.CRYPTO, false),
                        ItemDef("crypto-dogecoin-irr", "doge", "دوج‌کوین", "DOGE", PriceCategory.CRYPTO, false)
                    )

                    // Find Tether rate to accurately convert other cryptos to USD
                    var usdtPriceTomans: Long? = null
                    val usdtNode = current["crypto-tether-irr"] as? Map<String, Any>
                    if (usdtNode != null) {
                        val usdtRls = parseCleanLong(usdtNode["p"])
                        if (usdtRls > 0) usdtPriceTomans = usdtRls / 10
                    }

                    for (def in definitions) {
                        @Suppress("UNCHECKED_CAST")
                        val node = current[def.key] as? Map<String, Any> ?: continue

                        val rawPriceRls = parseCleanLong(node["p"])
                        if (rawPriceRls <= 0) continue

                        // Convert Rials to Tomans
                        val priceTomans = rawPriceRls / 10
                        val rawHigh = parseCleanLong(node["h"]) / 10
                        val rawLow = parseCleanLong(node["l"]) / 10
                        val high = rawHigh
                        val low = rawLow

                        val rawDp = parseCleanDouble(node["dp"])
                        val dt = node["dt"]?.toString()?.lowercase() ?: ""
                        val changePercent = if (dt == "low") -Math.abs(rawDp) else Math.abs(rawDp)
                        val rawD = parseCleanLong(node["d"]) / 10
                        val changeAmount = if (changePercent < 0) -Math.abs(rawD) else Math.abs(rawD)

                        val priceUsd = if (def.category == PriceCategory.CRYPTO) {
                            if (def.id == "usdt") 1.0 else usdtPriceTomans?.takeIf { it > 0 }?.let { priceTomans.toDouble() / it }
                        } else null
                        if (def.category == PriceCategory.CRYPTO && def.id != "usdt" && priceUsd == null) continue

                        items.add(
                            PriceItem(
                                id = def.id,
                                nameFa = def.nameFa,
                                symbol = def.symbol,
                                category = def.category,
                                priceTomans = priceTomans,
                                change24hPercent = changePercent,
                                high24h = high,
                                low24h = low,
                                isFavorite = def.isDefaultFavorite,
                                sparklinePoints = emptyList(),
                                priceUsd = priceUsd,
                                changeAmount = changeAmount
                            )
                        )
                    }

                    if (items.isNotEmpty()) {
                        Log.d(TAG, "Successfully fetched ${items.size} items from $url")
                        return@withContext items
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching from $url: ${e.message}")
            }
        }

        emptyList()
    }

    /**
     * Fallback crypto fetching from Nobitex if TGJU is unreachable for cryptos.
     */
    suspend fun fetchCryptoFromNobitex(): List<PriceItem> = withContext(Dispatchers.IO) {
        val url = "https://api.nobitex.ir/market/stats?srcCurrency=usdt,btc,eth,ton,sol,bnb,trx,doge,not&dstCurrency=rls"
        try {
            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Android; Chand App)")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: return@withContext emptyList()
                val data = gson.fromJson(body, NobitexStatsResponse::class.java)
                val stats = data.stats ?: return@withContext emptyList()

                val cryptoList = mutableListOf<PriceItem>()
                val usdtRls = parseCleanLong(stats["usdt-rls"]?.latest)
                val usdtTomans = usdtRls.takeIf { it > 0 }?.div(10)

                val cryptoDefs = listOf(
                    Triple("usdt-rls", "تتر", "USDT"),
                    Triple("btc-rls", "بیت‌کوین", "BTC"),
                    Triple("eth-rls", "اتریوم", "ETH"),
                    Triple("ton-rls", "تون‌کوین", "TON"),
                    Triple("sol-rls", "سولانا", "SOL"),
                    Triple("bnb-rls", "بایننس کوین", "BNB"),
                    Triple("trx-rls", "ترون", "TRX"),
                    Triple("doge-rls", "دوج‌کوین", "DOGE"),
                    Triple("not-rls", "نات‌کوین", "NOT")
                )

                for ((key, nameFa, symbol) in cryptoDefs) {
                    val item = stats[key]
                    if (item?.latest != null) {
                        val priceRls = parseCleanLong(item.latest)
                        val priceTomans = priceRls / 10
                        if (priceTomans <= 0) continue
                        val changePercent = parseCleanDouble(item.dayChange)
                        val high = parseCleanLong(item.dayHigh) / 10
                        val low = parseCleanLong(item.dayLow) / 10

                        val isUsdt = symbol.equals("USDT", ignoreCase = true)
                        val priceUsd = if (isUsdt) 1.0 else usdtTomans?.takeIf { it > 0 }?.let { priceTomans.toDouble() / it }
                        if (!isUsdt && priceUsd == null) continue

                        cryptoList.add(
                            PriceItem(
                                id = symbol.lowercase(),
                                nameFa = nameFa,
                                symbol = symbol,
                                category = PriceCategory.CRYPTO,
                                priceTomans = priceTomans,
                                change24hPercent = changePercent,
                                high24h = high,
                                low24h = low,
                                sparklinePoints = emptyList(),
                                priceUsd = priceUsd
                            )
                        )
                    }
                }
                cryptoList
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (e: Exception) {
            Log.e(TAG, "Nobitex error: ${e.message}")
            emptyList()
        }
    }

    private fun parseCleanLong(value: Any?): Long {
        if (value == null) return 0L
        val str = value.toString().replace(",", "").replace(" ", "").trim()
        return str.toDoubleOrNull()?.toLong() ?: 0L
    }

    private fun parseCleanDouble(value: Any?): Double {
        if (value == null) return 0.0
        val str = value.toString().replace(",", "").replace(" ", "").trim()
        return str.toDoubleOrNull() ?: 0.0
    }

    private data class ItemDef(
        val key: String,
        val id: String,
        val nameFa: String,
        val symbol: String,
        val category: PriceCategory,
        val isDefaultFavorite: Boolean
    )
}
