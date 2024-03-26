package com.example.stockinsight.data.remote

import com.example.stockinsight.data.model.StockHomePage
import retrofit2.Response
import retrofit2.http.GET

interface StockApi {
    @GET("/stock/homepage")
    suspend fun getStockHomePage() : Response<ArrayList<StockHomePage>>
}