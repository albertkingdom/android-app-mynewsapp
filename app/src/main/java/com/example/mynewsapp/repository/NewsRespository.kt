package com.example.mynewsapp.repository

import NewsResponse
import com.example.mynewsapp.api.RetrofitInstance
import retrofit2.Response

class NewsRespository {
    suspend fun searchNews(stockName:String="台積電", page:Int):Response<NewsResponse>{
        return RetrofitInstance.retrofitService.searchForNews(stockName, page)
    }

    suspend fun getHeadlines(country:String, page: Int, category: String):Response<NewsResponse>{
        return RetrofitInstance.retrofitService.getHeadlines(country, category,page)
    }
}