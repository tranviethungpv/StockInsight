package com.example.stockinsight.data.repository

import com.example.stockinsight.data.model.stock.FullStockInfo
import com.example.stockinsight.utils.UiState

interface StockRepository {
    fun getStockInfo(
        symbols: List<String>,
        interval: String,
        range: String,
        result: (UiState<ArrayList<FullStockInfo>>) -> Unit
    )

    fun fetchWatchlist(userId: String?, result: (UiState<ArrayList<FullStockInfo>>) -> Unit)
    suspend fun getStockInfoBySymbol(
        symbol: String, interval: String, range: String
    ): UiState<FullStockInfo>

    suspend fun addStockToWatchlist(userId: String, symbol: String): UiState<String>
    suspend fun removeStockFromWatchlist(userId: String, symbol: String): UiState<String>
    fun disconnect()
}