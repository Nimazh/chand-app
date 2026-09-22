package com.chand.app.data.model

/** Stable asset choices; a missing quote is represented by zero and never shown as a price. */
object PriceCatalog {
    val all: List<PriceItem> = listOf(
        asset("usd", "دلار آمریکا", "USD", PriceCategory.CURRENCY),
        asset("eur", "یورو", "EUR", PriceCategory.CURRENCY),
        asset("aed", "درهم امارات", "AED", PriceCategory.CURRENCY),
        asset("gbp", "پوند انگلیس", "GBP", PriceCategory.CURRENCY),
        asset("cad", "دلار کانادا", "CAD", PriceCategory.CURRENCY),
        asset("try", "لیر ترکیه", "TRY", PriceCategory.CURRENCY),
        asset("cny", "یوان چین", "CNY", PriceCategory.CURRENCY),
        asset("iqd", "دینار عراق (۱۰۰)", "IQD", PriceCategory.CURRENCY),
        asset("emami", "سکه امامی", "EMAMI", PriceCategory.GOLD),
        asset("bahar", "سکه بهار آزادی", "BAHAR", PriceCategory.GOLD),
        asset("nim", "نیم سکه", "NIM", PriceCategory.GOLD),
        asset("rob", "ربع سکه", "ROB", PriceCategory.GOLD),
        asset("gerami", "سکه گرمی", "GERAMI", PriceCategory.GOLD),
        asset("gold18", "طلای ۱۸ عیار", "GOLD18", PriceCategory.GOLD),
        asset("gold24", "طلای ۲۴ عیار", "GOLD24", PriceCategory.GOLD),
        asset("mesghal", "مثقال طلا", "MESGHAL", PriceCategory.GOLD),
        asset("usdt", "تتر", "USDT", PriceCategory.CRYPTO),
        asset("btc", "بیت‌کوین", "BTC", PriceCategory.CRYPTO),
        asset("eth", "اتریوم", "ETH", PriceCategory.CRYPTO),
        asset("ton", "تون‌کوین", "TON", PriceCategory.CRYPTO),
        asset("trx", "ترون", "TRX", PriceCategory.CRYPTO),
        asset("sol", "سولانا", "SOL", PriceCategory.CRYPTO),
        asset("doge", "دوج‌کوین", "DOGE", PriceCategory.CRYPTO),
        asset("bnb", "بایننس کوین", "BNB", PriceCategory.CRYPTO),
        asset("not", "نات‌کوین", "NOT", PriceCategory.CRYPTO)
    )

    fun withQuotes(quotes: List<PriceItem>): List<PriceItem> {
        val byId = quotes.associateBy { it.id.lowercase() }
        return all.map { byId[it.id] ?: it } + quotes.filter { it.id.lowercase() !in allIds }
    }

    private val allIds by lazy { all.mapTo(HashSet()) { it.id } }

    private fun asset(id: String, name: String, symbol: String, category: PriceCategory) =
        PriceItem(id, name, symbol, category, priceTomans = 0L, change24hPercent = 0.0)
}
