package com.chand.app.data.remote

import com.google.gson.annotations.SerializedName

data class NobitexStatsResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("stats") val stats: Map<String, NobitexMarketItem>?
)

data class NobitexMarketItem(
    @SerializedName("latest") val latest: String?,
    @SerializedName("dayLow") val dayLow: String?,
    @SerializedName("dayHigh") val dayHigh: String?,
    @SerializedName("dayChange") val dayChange: String?
)
