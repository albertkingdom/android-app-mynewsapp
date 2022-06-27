package com.example.mynewsapp.api

import com.example.mynewsapp.model.StockPriceInfoResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.Response
import retrofit2.http.*


interface StockInfoApi {


//    @GET("api/getStockInfo.jsp")
    @GET
    suspend fun getStockPriceInfo(
        @Url
        urlString: String,
//        @Query("ex_ch")
//        stockNo: String ="tse_2330.tw",
//        @Query("json")
//        isJson: String = "1"
    ): Response<StockPriceInfoResponse>

    @GET
    fun getStockPriceInfoRx(
        @Url
        urlString: String,
    ): Single<Response<StockPriceInfoResponse>>
}