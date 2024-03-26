package com.example.stockinsight.data.repository

import com.example.stockinsight.data.model.StockHomePage
import com.example.stockinsight.data.remote.StockApi
import com.example.stockinsight.utils.UiState
import javax.inject.Inject

class StockImpl @Inject constructor(
    private val stockApi: StockApi
)
: StockRepository {
    override suspend fun getStockHomePage(result: (UiState<ArrayList<StockHomePage>>) -> Unit) {
        val response = stockApi.getStockHomePage()
        if (response.isSuccessful) {
            result(UiState.Success(ArrayList(response.body()!!)))
        } else {
            result(UiState.Failure(response.message()))
        }
    }
}