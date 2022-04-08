package com.example.mynewsapp.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.lifecycle.*
import com.example.mynewsapp.MyApplication
import com.example.mynewsapp.db.InvestHistory

import com.example.mynewsapp.db.Stock
import com.example.mynewsapp.model.*
import com.example.mynewsapp.repository.NewsRepository
import com.example.mynewsapp.util.GetDateString
import com.example.mynewsapp.util.Resource
import com.google.firebase.Timestamp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import okio.EOFException
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class NewsViewModel(val newsRepository: NewsRepository, application: MyApplication) :
    AndroidViewModel(
        application
    ) {
    val news: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    val stockPriceInfo: MutableLiveData<Resource<StockPriceInfoResponse>> = MutableLiveData()
    var viewModelStockNoList = listOf<String>()
    val allStocksFromdb: LiveData<List<Stock>> = newsRepository.allstocks.asLiveData()
    val candleStickData: MutableLiveData<Resource<CandleStickData>?> = MutableLiveData()

    var investHistoryList: LiveData<List<InvestHistory>> = MutableLiveData<List<InvestHistory>>()
    val allInvestHistoryList: LiveData<List<InvestHistory>> = newsRepository.allHistory.asLiveData()

    var investStatisticsList: MutableLiveData<List<StockStatistic>> = MutableLiveData()

    var fetchPriceJob: Job? = null

    companion object {
        const val TAG = "NewsViewModel"
    }

    fun getHeadlines(page: Int = 1) {
        if (isNetworkAvailable()) {
            viewModelScope.launch {
                news.postValue(Resource.Loading())
                val response =
                    newsRepository.getHeadlines(country = "tw", category = "business", page = page)
                news.postValue(handleNewsResponse(response))
            }
        } else {
            news.postValue(Resource.Error("No Internet Connection"))
        }

    }

    private fun getStockPriceInfo(stockList: List<String>): Job {
        Log.d(TAG,"getStockPriceInfo")

            return viewModelScope.launch {
                if (isNetworkAvailable()) {
                    while (true) {

                        stockPriceInfo.postValue(Resource.Loading())

                        val stockListString: String = stockList.joinToString("|") {
                            "tse_${it}.tw"
                        }
                        try {
                            val response =
                                newsRepository.getStockPriceInfo(stockNo = stockListString)
                            stockPriceInfo.value = handleStockPriceInfoResponse(response)
                        } catch (e: EOFException) {
                            e.printStackTrace()
                        }

                        Log.d(TAG, "fetch for stock price")
                        delay(1000 * 60 * 5)
                    }
                }  else {
                    stockPriceInfo.postValue(Resource.Error("No Internet Connection"))

                }
            }

    }

    fun getRelatedNews(stockName: String, page: Int = 1) {
        if (isNetworkAvailable()) {
            viewModelScope.launch {
                news.postValue(Resource.Loading())
                val response = newsRepository.searchNews(stockName = stockName, page = page)
                news.postValue(handleNewsResponse(response))
            }
        } else {
            news.postValue(Resource.Error("No Internet Connection"))
        }
    }

    private fun <T> concatenate(vararg lists: List<T>): List<T> {
        // [[],[],[],...] + [[],[],[],...]
        val result: MutableList<T> = ArrayList()
        lists.forEach { list: List<T> -> result.addAll(list) }
        return result
    }

    fun getCandleStickData(currentDate: String, stockNo: String) {
        if (isNetworkAvailable()) {
            viewModelScope.launch {
                candleStickData.postValue(Resource.Loading())

                val currentMonthStr = GetDateString.outputCurrentDateString()
                val lastMonthStr = GetDateString.outputLastMonthDateString()
                val responseCurrentMonth =
                    newsRepository.getCandleStickData(currentMonthStr, stockNo)
                val responseLastMonth = newsRepository.getCandleStickData(lastMonthStr, stockNo)


                // concat multiple month candle stick data
                val fullCandleStickDataList = concatenate(
                    responseLastMonth.body()?.data!!,
                    responseCurrentMonth.body()?.data!!
                )
               if (!responseCurrentMonth.isSuccessful || !responseLastMonth.isSuccessful) {
                   candleStickData.value = Resource.Error("There's error in fetching candle stick data.")
               } else {
                   candleStickData.value =
                       Resource.Success(CandleStickData(fullCandleStickDataList, "", listOf(), listOf(), "", ""))
               }

            }
        }
    }

    //get news headlines
    private fun handleNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    //get stockprice
    private fun handleStockPriceInfoResponse(response: Response<StockPriceInfoResponse>): Resource<StockPriceInfoResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleCandleStickDataResponse(response: Response<CandleStickData>): Resource<CandleStickData> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    fun addToStockList(stockNo: String) {

        viewModelScope.launch {
            if(!viewModelStockNoList.contains(stockNo)) {
                newsRepository.insert(stock = Stock(0, stockNo))
            }
        }

    }

    fun deleteStock(stockNo: String) {
        viewModelScope.launch {
            newsRepository.delStock(stockNo)
        }
    }

    fun getStockNoListAndToQueryStockPriceInfo(stockList: List<String> = viewModelStockNoList) {

        if (stockList.isEmpty()){
            return
        }
        viewModelStockNoList = stockList //save a current stock number list copy

        fetchPriceJob = getStockPriceInfo(stockList)


    }

    private fun isNetworkAvailable(): Boolean {
        //Log.d("viewmodel", "isNetworkAvailable")
        val connectivityManager =
            getApplication<MyApplication>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    return true
                }
            }
        }
        return false
    }

    fun queryHistoryByStockNo(stockNo: String) {

        investHistoryList = newsRepository.queryHistoryByStockNo(stockNo).asLiveData()

//        return investHistoryList
    }

    fun insertHistory(investHistory: InvestHistory) {
        viewModelScope.launch {
            newsRepository.insertHistory(investHistory)
        }
    }

    fun deleteAllHistory(stockNo: String) {
        viewModelScope.launch {
            newsRepository.deleteAllHistory(stockNo)
        }
    }

    fun clearCandleStickData() {
        candleStickData.value = null
    }

    fun calculateForPieChart() {

        val mapOfStockNoToAmount = mutableMapOf<String, Int>() // {0056 to 200}
        val mapOfStockNoToCurrentPrice = mutableMapOf<String, Float>() //{0056 to 32}
        val mapOfStockNoToTotalMoney = mutableMapOf<String, Float>() // {0056 to 200*32}
        allInvestHistoryList.value?.map { history ->
            if (mapOfStockNoToAmount[history.stockNo] == null) {

                mapOfStockNoToAmount[history.stockNo] =
                    history.amount * (if (history.status == 0) 1 else -1)
            } else {
                mapOfStockNoToAmount[history.stockNo] =
                    mapOfStockNoToAmount[history.stockNo]!! + history.amount * (if (history.status == 0) 1 else -1)
            }
        }
        //Log.d("viewmodel", mapOfStockNoToAmount.toString())

        stockPriceInfo.value?.data?.msgArray?.map {

            mapOfStockNoToCurrentPrice[it.c] = (if (it.z != "-") it.z else it.y).toFloat()
        }
        //Log.d("viewmodel mapOfCurrentPrice", mapOfCurrentPrice.toString())

        mapOfStockNoToAmount.map { entry ->
            mapOfStockNoToTotalMoney[entry.key] =
                entry.value * mapOfStockNoToCurrentPrice[entry.key]!!

        }
        investStatisticsList.value = mapOfStockNoToAmount.map { entry ->

            StockStatistic(entry.key, entry.value * mapOfStockNoToCurrentPrice[entry.key]!!)
        }
        //Log.d("viewmodel mapOfTotalMoney", mapOfStockNoToTotalMoney.toString())

        //investStatistics.postValue(mapOfStockNoToTotalMoney)

        //Log.d("viewmodel investStatistics", investStatistics.value.toString())
    }

    fun cancelRepeatFetchPriceJob() {
        fetchPriceJob?.cancel()
    }






}

class NewsViewModelFactory(
    val newsRepository: NewsRepository,
    val application: MyApplication
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NewsViewModel(newsRepository, application) as T
    }
}