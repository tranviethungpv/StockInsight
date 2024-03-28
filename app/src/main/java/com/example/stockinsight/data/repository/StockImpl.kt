package com.example.stockinsight.data.repository

import com.example.stockinsight.data.model.stock.StockInfo
import com.example.stockinsight.data.remote.YFinanceApi
import com.example.stockinsight.utils.UiState
import javax.inject.Inject

class StockImpl @Inject constructor(
    private val yFinanceApi: YFinanceApi
)
: StockRepository {
    override suspend fun fetchQuoteInformation(symbol: String, interval: String, range: String, result: (UiState<StockInfo>) -> Unit) {
        val response = yFinanceApi.fetchQuoteInformation(symbol, interval, range)
        if (response.isSuccessful) {
            result(UiState.Success(response.body()!!))
        } else {
            result(UiState.Failure("An error occurred while fetching stock data."))
        }
    }
}