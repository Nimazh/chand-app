package com.chand.app.data.model

import java.util.Locale

data class PriceItem(
    val id: String,
    val nameFa: String,
    val symbol: String,
    val category: PriceCategory,
    val priceTomans: Long,
    val change24hPercent: Double,
    val high24h: Long = 0,
    val low24h: Long = 0,
    val lastUpdatedEpochMs: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val sparklinePoints: List<Double> = emptyList(),
    val priceUsd: Double? = null,
    val nameEn: String? = null,
    val changeAmount: Long? = 0L
) {
    val isPositiveChange: Boolean get() = change24hPercent >= 0

    /**
     * Whether the price movement is economically favorable in Iranian markets.
     * Currencies & Gold: Decreasing price (cooling inflation / strengthening Rial) is favorable (Green).
     * Cryptocurrencies: Increasing price is favorable (Green).
     * Exactly matches the official Apple Chand app color system.
     */
    val isFavorable: Boolean
        get() = if (category == PriceCategory.CRYPTO) change24hPercent >= 0 else change24hPercent <= 0

    /**
     * English name fallback based on id if not explicitly passed or if null from cached json
     */
    val effectiveNameEn: String
        get() = if (!nameEn.isNullOrBlank()) nameEn else when (id.lowercase(Locale.US)) {
            "usd" -> "US Dollar"
            "eur" -> "Euro"
            "gbp" -> "British Pound"
            "aed" -> "UAE Dirham"
            "cad" -> "Canadian Dollar"
            "try" -> "Turkish Lira"
            "cny" -> "Chinese Yuan"
            "iqd" -> "Iraqi Dinar"
            "chf" -> "Swiss Franc"
            "aud" -> "Australian Dollar"
            "emami" -> "Emami Coin"
            "bahar" -> "Bahar Azadi"
            "nim" -> "Half Azadi"
            "rob" -> "1/4 Azadi"
            "gerami" -> "Gerami Coin"
            "gold18" -> "18K Gold"
            "gold24" -> "24K Gold"
            "mesghal" -> "Mesghal"
            "usdt" -> "Tether"
            "btc" -> "Bitcoin"
            "eth" -> "Ethereum"
            "ton" -> "Toncoin"
            "trx" -> "Tron"
            "sol" -> "Solana"
            "doge" -> "Dogecoin"
            else -> nameFa
        }

    /**
     * Vector SVG logo resource ID for native Apple-style circular badges
     */
    val iconResId: Int
        get() = when (id.lowercase(Locale.US)) {
            "usd" -> com.chand.app.R.drawable.ic_asset_usd
            "eur" -> com.chand.app.R.drawable.ic_asset_eur
            "gbp" -> com.chand.app.R.drawable.ic_asset_gbp
            "aed" -> com.chand.app.R.drawable.ic_asset_aed
            "cad" -> com.chand.app.R.drawable.ic_asset_cad
            "try" -> com.chand.app.R.drawable.ic_asset_try
            "cny" -> com.chand.app.R.drawable.ic_asset_cny
            "iqd" -> com.chand.app.R.drawable.ic_asset_iqd
            "chf" -> com.chand.app.R.drawable.ic_asset_chf
            "aud" -> com.chand.app.R.drawable.ic_asset_aud
            "emami" -> com.chand.app.R.drawable.ic_asset_emami
            "bahar" -> com.chand.app.R.drawable.ic_asset_bahar
            "nim" -> com.chand.app.R.drawable.ic_asset_nim
            "rob" -> com.chand.app.R.drawable.ic_asset_rob
            "gerami" -> com.chand.app.R.drawable.ic_asset_gerami
            "gold18" -> com.chand.app.R.drawable.ic_asset_gold18
            "gold24" -> com.chand.app.R.drawable.ic_asset_gold24
            "mesghal" -> com.chand.app.R.drawable.ic_asset_mesghal
            "usdt" -> com.chand.app.R.drawable.ic_asset_usdt
            "btc" -> com.chand.app.R.drawable.ic_asset_btc
            "eth" -> com.chand.app.R.drawable.ic_asset_eth
            "ton" -> com.chand.app.R.drawable.ic_asset_ton
            "trx" -> com.chand.app.R.drawable.ic_asset_trx
            "sol" -> com.chand.app.R.drawable.ic_asset_sol
            "doge" -> com.chand.app.R.drawable.ic_asset_doge
            "bnb" -> com.chand.app.R.drawable.ic_asset_bnb
            "not" -> com.chand.app.R.drawable.ic_asset_not
            else -> com.chand.app.R.drawable.ic_asset_default
        }

    /**
     * Flag or symbol icon for Apple-style circular badges (Emoji fallback)
     */
    val flagOrIcon: String
        get() = when (id.lowercase(Locale.US)) {
            "usd" -> "🇺🇸"
            "eur" -> "🇪🇺"
            "gbp" -> "🇬🇧"
            "aed" -> "🇦🇪"
            "cad" -> "🇨🇦"
            "try" -> "🇹🇷"
            "cny" -> "🇨🇳"
            "iqd" -> "🇮🇶"
            "chf" -> "🇨🇭"
            "aud" -> "🇦🇺"
            "emami", "bahar", "nim", "rob", "gerami" -> "🪙"
            "gold18", "gold24", "mesghal" -> "🥇"
            "btc" -> "₿"
            "eth" -> "Ξ"
            "usdt" -> "₮"
            "ton" -> "💎"
            "trx" -> "TRX"
            "sol" -> "◎"
            "doge" -> "Ð"
            else -> "🏷️"
        }

    /**
     * Cryptocurrencies (except Tether/USDT) are priced in USD as requested.
     * Tether stays in Tomans as it represents the Iranian free-market dollar rate.
     */
    val isUsd: Boolean
        get() = category == PriceCategory.CRYPTO && !id.equals("usdt", ignoreCase = true)

    val unitText: String
        get() = if (isUsd) "دلار" else "تومان"

    val effectiveUsdPrice: Double
        get() = priceUsd ?: (if (priceTomans > 0) priceTomans.toDouble() / 229180.0 else 0.0)

    val formattedPrice: String
        get() {
            return if (isUsd) {
                val usd = effectiveUsdPrice
                when {
                    usd >= 1000.0 -> String.format(Locale.US, "%,d", usd.toLong())
                    usd >= 1.0 -> String.format(Locale.US, "%,.2f", usd)
                    else -> String.format(Locale.US, "%,.4f", usd)
                }
            } else {
                String.format(Locale.US, "%,d", priceTomans)
            }
        }

    val formattedPriceWithUnit: String
        get() = "$formattedPrice $unitText"

    val formattedHigh: String
        get() {
            return if (isUsd) {
                val usd = if (high24h > 0) high24h.toDouble() / 229180.0 else effectiveUsdPrice * 1.015
                when {
                    usd >= 1000.0 -> "${String.format(Locale.US, "%,d", usd.toLong())} $unitText"
                    usd >= 1.0 -> "${String.format(Locale.US, "%,.2f", usd)} $unitText"
                    else -> "${String.format(Locale.US, "%,.4f", usd)} $unitText"
                }
            } else {
                "${String.format(Locale.US, "%,d", high24h)} $unitText"
            }
        }

    val formattedLow: String
        get() {
            return if (isUsd) {
                val usd = if (low24h > 0) low24h.toDouble() / 229180.0 else effectiveUsdPrice * 0.985
                when {
                    usd >= 1000.0 -> "${String.format(Locale.US, "%,d", usd.toLong())} $unitText"
                    usd >= 1.0 -> "${String.format(Locale.US, "%,.2f", usd)} $unitText"
                    else -> "${String.format(Locale.US, "%,.4f", usd)} $unitText"
                }
            } else {
                "${String.format(Locale.US, "%,d", low24h)} $unitText"
            }
        }

    val formattedChange: String
        get() {
            val sign = if (change24hPercent > 0) "+" else if (change24hPercent < 0) "-" else ""
            val absVal = Math.abs(change24hPercent)
            return String.format(Locale.US, "%s%.2f%%", sign, absVal)
        }

    /**
     * Apple-style arrow change indicator (e.g. ↓5.66K or ↑2.40% or ↓5,625)
     * Matches screenshot 1, 2, and 3
     */
    val arrowChangeFormatted: String
        get() {
            val arrow = if (change24hPercent >= 0) "↑" else "↓"
            val absPercent = Math.abs(change24hPercent)
            val amt = changeAmount ?: 0L
            return if (amt != 0L) {
                val absAmt = Math.abs(amt)
                if (absAmt >= 10000) {
                    val kVal = absAmt / 1000.0
                    String.format(Locale.US, "%s%.2fK", arrow, kVal)
                } else {
                    String.format(Locale.US, "%s%,d", arrow, absAmt)
                }
            } else {
                // If amount not explicitly set, calculate from percent and current price
                val computedAmt = (priceTomans * absPercent / 100.0).toLong()
                if (computedAmt >= 10000) {
                    val kVal = computedAmt / 1000.0
                    String.format(Locale.US, "%s%.2fK", arrow, kVal)
                } else if (computedAmt > 0) {
                    String.format(Locale.US, "%s%,d", arrow, computedAmt)
                } else {
                    String.format(Locale.US, "%s%.2f%%", arrow, absPercent)
                }
            }
        }

    /**
     * Apple widget arrow change indicator (e.g. ↓5,625 or ↑1,150)
     * Shows comma-separated integers for values under 1,000,000 to match screenshot media_1789907711489.png
     */
    val widgetArrowChangeFormatted: String
        get() {
            val arrow = if (change24hPercent >= 0) "↑" else "↓"
            val absPercent = Math.abs(change24hPercent)
            val amt = changeAmount ?: 0L
            val absAmt = if (amt != 0L) Math.abs(amt) else (priceTomans * absPercent / 100.0).toLong()
            return when {
                absAmt >= 1_000_000 -> {
                    val mVal = absAmt / 1_000_000.0
                    String.format(Locale.US, "%s%.1fM", arrow, mVal)
                }
                absAmt > 0 -> {
                    String.format(Locale.US, "%s%,d", arrow, absAmt)
                }
                else -> {
                    String.format(Locale.US, "%s%.2f%%", arrow, absPercent)
                }
            }
        }
}
