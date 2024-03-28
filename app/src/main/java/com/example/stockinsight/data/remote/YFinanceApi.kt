package com.example.stockinsight.data.remote

import com.example.stockinsight.data.model.stock.StockInfo
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface YFinanceApi {
//    @GET("/multi-quote/VNM,%5EDJI,AAPL,SBUX,NKE,GOOG,%5EFTSE,%5EGDAXI,%5EHSI,%5EN225,%5EGSPC%27")
    @GET("/simple-quote/{symbol}/{interval}/{range}")
    suspend fun fetchQuoteInformation(
        @Path("symbol") symbol: String,
        @Path("interval") interval: String,
        @Path("range") range: String
    ): Response<StockInfo>
}