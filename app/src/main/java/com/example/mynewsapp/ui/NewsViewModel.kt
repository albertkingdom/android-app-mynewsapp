package com.example.mynewsapp.ui

import android.util.Log
import com.example.mynewsapp.model.NewsResponse
import androidx.lifecycle.*
import com.example.mynewsapp.model.StockPriceInfoResponse
import com.example.mynewsapp.db.Stock
import com.example.mynewsapp.repository.NewsRepository
import com.example.mynewsapp.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response

class NewsViewModel(val newsRepository: NewsRepository):ViewModel() {
    var page = 1
    val news:MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    val stockPriceInfo: MutableLiveData<Resource<StockPriceInfoResponse>> = MutableLiveData()
    var viewModelStockNoList = listOf<String>()
    val allStocksFromdb: LiveData<List<Stock>> = newsRepository.allstocks.asLiveData()


    init {
        getHeadlines()
    }


    fun getHeadlines(){

        viewModelScope.launch {
            news.postValue(Resource.Loading())
            val response = newsRepository.getHeadlines(country= "tw",category = "business",page = page)
            news.postValue(handleNewsResponse(response))
        }
    }

    fun getStockPriceInfo(stockList: List<String>){
        //Log.d("model getStockPriceInfo","getStockPriceInfo")
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

        viewModelScope.launch {
            newsRepository.insert(stock = Stock(0,stockNo))
        }

    }

    fun deleteStock(stockNo:String){
        viewModelScope.launch {
            newsRepository.delStock(stockNo)
        }
    }

    fun getStockNoListAndToQueryStockPriceInfo(stockList: List<String>){
        //Log.d("model check!!!","getStockNoListAndToQueryStockPriceInfo")
        if(stockList.size == viewModelStockNoList.size && stockList.containsAll(viewModelStockNoList)){
            return
        }else{
            viewModelStockNoList = stockList
            getStockPriceInfo(viewModelStockNoList)
        }
    }
}

class NewsViewModelProviderFactory(val newsRepository: NewsRepository): ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return NewsViewModel(newsRepository) as T
    }
}