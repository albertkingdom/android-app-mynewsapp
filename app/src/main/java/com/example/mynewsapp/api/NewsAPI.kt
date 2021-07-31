package com.example.mynewsapp.api

import NewsResponse
import com.example.mynewsapp.util.Constant.Companion.API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsAPI {

    @GET("v2/everything")
    fun getNews(
        @Query("q")
        stockName:String = "台積電",
        @Query("page")
        page:Int=1,
        @Query("apiKey")
        apiKey:String = API_KEY
    ):Response<NewsResponse>
}