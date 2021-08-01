package com.example.mynewsapp.api

import com.example.mynewsapp.StockPriceInfoResponse
import retrofit2.Response
import retrofit2.http.*

data class StockNoString(
    val str: String = "tse_2330.tw"
)
interface StockInfoApi {
    @Headers("Content-Type: application/json")
    @POST("stock/stockprice")
    suspend fun getStockPriceInfobyPost(
        @Body stockNo: StockNoString
    ): Response<StockPriceInfoResponse>

    @GET("api/getStockInfo.jsp")
    suspend fun getStockPriceInfo(
        @Query("ex_ch")
        stockNo: String ="tse_2330.tw",
        @Query("json")
        isJson: String = "1"
    ): Response<StockPriceInfoResponse>
}