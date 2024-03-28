package com.example.stockinsight.data.model.stock

import com.google.gson.annotations.SerializedName

data class QuoteInfo(
    @SerializedName("companyName")
    val companyName: String,
    @SerializedName("diff")
    val diff: Double,
    @SerializedName("percentChange")
    val percentChange: Double,
    @SerializedName("symbol")
    val symbol: String,
    @SerializedName("today")
    val today: Double,
    @SerializedName("yesterday")
    val yesterday: Double
)
