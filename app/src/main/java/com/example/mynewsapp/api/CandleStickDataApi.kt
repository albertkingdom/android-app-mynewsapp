package com.example.mynewsapp.api

import com.example.mynewsapp.model.CandleStickData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface CandleStickDataApi {
    //STOCK_DAY?response=json&date=20201010&stockNo=2330
//    @GET("STOCK_DAY?response=json")
    @GET
    suspend fun getCandleStickData(
        @Url
        urlString: String,
//        @Query("date")
//        date:String,
//        @Query("stockNo")
//        stockNo:String
    ):Response<CandleStickData>
}