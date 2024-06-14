package com.example.stockinsight.data.repository

import com.example.stockinsight.data.model.stock.FullStockInfo
import com.example.stockinsight.utils.UiState

interface StockRepository {
    fun getStockInfo(
        symbols: List<String>,
        range: String,
        result: (UiState<ArrayList<FullStockInfo>>) -> Unit
    )

    fun fetchWatchlist(userId: String?, result: (UiState<ArrayList<FullStockInfo>>) -> Unit)
    suspend fun getStockInfoBySymbol(
        symbol: String, range: String
    ): UiState<FullStockInfo>

    suspend fun addStockToWatchlist(userId: String, symbol: String, threshold: Double, lastNotifiedPrice: Double): UiState<String>
    suspend fun removeStockFromWatchlist(userId: String, symbol: String): UiState<String>

    fun searchStocksByKeyword(
        keyword: String,
        range: String,
        result: (UiState<ArrayList<FullStockInfo>>) -> Unit
    )

    suspend fun updateThreshold(userId: String, symbol: String, threshold: Double): UiState<String>

    fun closeSocket(name: String)

    fun disconnect()
}