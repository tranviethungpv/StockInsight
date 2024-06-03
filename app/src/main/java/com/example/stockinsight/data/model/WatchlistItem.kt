package com.example.stockinsight.data.model


data class WatchlistItem(
    val symbol: String = "",
    val threshold: Double = 0.0,
    val lastNotifiedPrice: Double = 0.0
)