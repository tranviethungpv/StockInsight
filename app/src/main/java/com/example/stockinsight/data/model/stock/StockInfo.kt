package com.example.stockinsight.data.model.stock

import com.google.gson.annotations.SerializedName

data class StockInfo(
    @SerializedName("historicData")
    val historicData: HistoricData,
    @SerializedName("quoteInfo")
    val quoteInfo: QuoteInfo
)
