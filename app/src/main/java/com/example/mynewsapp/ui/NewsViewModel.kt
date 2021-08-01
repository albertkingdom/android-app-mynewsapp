package com.example.mynewsapp.ui

import NewsResponse
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mynewsapp.StockPriceInfoResponse
import com.example.mynewsapp.repository.NewsRepository
import com.example.mynewsapp.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response

class NewsViewModel(val newsRepository: NewsRepository):ViewModel() {
    var page = 1
    val news:MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    val stockPriceInfo: MutableLiveData<Resource<StockPriceInfoResponse>> = MutableLiveData()
    val stockList = mutableListOf("2330","0050")
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

    private fun handleNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse>{
        if (response.isSuccessful){
            response.body()?.let {
                resultResponse->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleStockPriceInfoResponse(response: Response<StockPriceInfoResponse>): Resource<StockPriceInfoResponse>{
        if (response.isSuccessful){
            response.body()?.let {
                    resultResponse->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }
}

class NewsViewModelProviderFactory(val newsRepository: NewsRepository): ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return NewsViewModel(newsRepository) as T
    }
}