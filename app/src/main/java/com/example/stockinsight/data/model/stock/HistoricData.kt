package com.example.stockinsight.data.model.stock

import com.google.gson.annotations.SerializedName

data class HistoricData(
    @SerializedName("Open") val open: Map<String, Double>,
    @SerializedName("High") val high: Map<String, Double>,
    @SerializedName("Low") val low: Map<String, Double>,
    @SerializedName("Close") val close: Map<String, Double>,
    @SerializedName("Volume") val volume: Map<String, Double>,
    @SerializedName("Dividends") val dividends: Map<String, Double>,
    @SerializedName("Stock Splits") val stockSplits: Map<String, Double>
)
