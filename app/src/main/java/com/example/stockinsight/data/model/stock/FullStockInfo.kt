package com.example.stockinsight.data.model.stock

import com.google.gson.annotations.SerializedName

data class FullStockInfo(
    @SerializedName("historicData") var historicData: HistoricData,
    @SerializedName("quoteInfo") var quoteInfo: QuoteInfo
)