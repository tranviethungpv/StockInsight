package com.example.stockinsight.data.model

data class StockHomePage(
    val companyName: String,
    val diff: Double,
    val percentChange: Double,
    val symbol: String,
    val today: Double,
    val yesterday: Double
)