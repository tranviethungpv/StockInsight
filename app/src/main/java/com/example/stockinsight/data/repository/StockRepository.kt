package com.example.stockinsight.data.repository

import com.example.stockinsight.data.model.stock.StockInfo
import com.example.stockinsight.utils.UiState

interface StockRepository {
    suspend fun fetchQuoteInformation(symbol: String, interval: String, range: String, result: (UiState<StockInfo>) -> Unit)
}