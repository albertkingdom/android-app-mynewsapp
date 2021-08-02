package com.example.mynewsapp.ui

import NewsResponse
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mynewsapp.StockPriceInfoResponse
import com.example.mynewsapp.repository.NewsRepository
import com.example.mynewsapp.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.lang.Exception

class NewsViewModel(val newsRepository: NewsRepository):ViewModel() {
    var page = 1
    val news:MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    val stockPriceInfo: MutableLiveData<Resource<StockPriceInfoResponse>> = MutableLiveData()
    val stockList = mutableSetOf("2330","0050")
    init {
        getHeadlines()
        getStockPriceInfo()
    }
    fun getHeadlines(){

        viewModelScope.launch {
            news.postValue(Resource.Loading())
            val response = newsRepository.getHeadlines(country= "tw",category = "business",page = page)
            news.postValue(handleNewsResponse(response))
        }
    }

    fun getStockPriceInfo(){
        viewModelScope.launch {
            stockPriceInfo.postValue(Resource.Loading())
            val stockListString:String = stockList.joinToString("|") {
                "tse_${it}.tw"
            }
            val response = newsRepository.getStockPriceInfo(stockNo = stockListString)
            stockPriceInfo.postValue(handleStockPriceInfoResponse(response))
        }
    }

    fun getRelatedNews(stockName:String){

        viewModelScope.launch {
            news.postValue(Resource.Loading())
            val response = newsRepository.searchNews(stockName = stockName,page = page)
            news.postValue(handleNewsResponse(response))
        }
    }
    //get news headlines
    private fun handleNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse>{
        if (response.isSuccessful){
            response.body()?.let {
                resultResponse->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }
    //get stockprice
    private fun handleStockPriceInfoResponse(response: Response<StockPriceInfoResponse>): Resource<StockPriceInfoResponse>{
        if (response.isSuccessful){
            response.body()?.let {
                    resultResponse->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    fun addToStockList(stockNo:String){
        stockList.add(stockNo)
        Log.d("stocklist",stockList.toString())
        try {
            getStockPriceInfo()
        }catch (e:Exception){
            Log.e("viewmodel error",e.toString())
        }
    }
}

class NewsViewModelProviderFactory(val newsRepository: NewsRepository): ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return NewsViewModel(newsRepository) as T
    }
}