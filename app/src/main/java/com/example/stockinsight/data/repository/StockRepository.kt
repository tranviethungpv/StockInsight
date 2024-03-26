package com.example.stockinsight.data.repository

import com.example.stockinsight.data.model.StockHomePage
import com.example.stockinsight.utils.UiState

interface StockRepository {
    suspend fun getStockHomePage(result: (UiState<ArrayList<StockHomePage>>) -> Unit)
}