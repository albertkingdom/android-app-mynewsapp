package com.example.mynewsapp.api

import NewsResponse
import com.example.mynewsapp.util.Constant.Companion.API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsAPI {

    @GET("v2/everything")
    suspend fun searchForNews(
        @Query("q")
        stockName:String = "台積電",
        @Query("page")
        page:Int=1,
        @Query("apiKey")
        apiKey:String = API_KEY
    ):Response<NewsResponse>

    @GET("v2/top-headlines")
    suspend fun getHeadlines(
        @Query("country")
        country:String = "tw",
        @Query("category")
        category: String = "business",
        @Query("page")
        page:Int=1,
        @Query("apiKey")
        apiKey:String = API_KEY
    ):Response<NewsResponse>

}