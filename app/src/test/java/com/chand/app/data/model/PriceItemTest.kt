package com.chand.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PriceItemTest {
    @Test
    fun calculatedChangeUsesThePreviousPriceInsteadOfTheCurrentPrice() {
        val item = PriceItem(
            id = "usd",
            nameFa = "دلار",
            symbol = "USD",
            category = PriceCategory.CURRENCY,
            priceTomans = 110,
            change24hPercent = 10.0,
            changeAmount = 0
        )

        assertEquals("↑10", item.arrowChangeFormatted)
    }

    @Test
    fun dollarPricedCryptoDisplaysPercentChangeRatherThanTomanDelta() {
        val item = PriceItem(
            id = "btc",
            nameFa = "بیت‌کوین",
            symbol = "BTC",
            category = PriceCategory.CRYPTO,
            priceTomans = 6_000_000_000,
            priceUsd = 60_000.0,
            change24hPercent = -1.25,
            changeAmount = -75_000_000
        )

        assertEquals("↓1.25%", item.arrowChangeFormatted)
    }
}
