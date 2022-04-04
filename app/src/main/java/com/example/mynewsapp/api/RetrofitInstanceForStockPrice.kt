package com.example.mynewsapp.api

import com.example.mynewsapp.util.Constant.Companion.BASE_URL_STOCK_PRICE
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitInstanceForStockPrice {
    private val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    private val client = OkHttpClient.Builder().addInterceptor(logging).build()
    val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL_STOCK_PRICE)
        .addConverterFactory(MoshiConverterFactory.create())
        .client(client)
        .build()
    val retrofitService:StockInfoApi by lazy {
        retrofit.create(StockInfoApi::class.java)
    }
}