package com.example.mynewsapp.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import com.example.mynewsapp.model.NewsResponse
import androidx.lifecycle.*
import com.example.mynewsapp.MyApplication
import com.example.mynewsapp.db.InvestHistory

import com.example.mynewsapp.model.StockPriceInfoResponse
import com.example.mynewsapp.db.Stock
import com.example.mynewsapp.model.CandleStickData
import com.example.mynewsapp.model.StockStatistic
import com.example.mynewsapp.repository.NewsRepository
import com.example.mynewsapp.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response

class NewsViewModel(val newsRepository: NewsRepository, application: MyApplication):AndroidViewModel(
    application
) {
    var page = 1
    val news:MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    val stockPriceInfo: MutableLiveData<Resource<StockPriceInfoResponse>> = MutableLiveData()
    var viewModelStockNoList = listOf<String>()
    val allStocksFromdb: LiveData<List<Stock>> = newsRepository.allstocks.asLiveData()
    val candleStickData: MutableLiveData<Resource<CandleStickData>> = MutableLiveData()
//    val investHistoryList: List<StockHistory> = listOf(
//        StockHistory(id= 1, stockNo = "0050", date = Date(), price = 135.0, amount = 999, status = 1),
//        StockHistory(id= 2, stockNo = "0050", date = Date(), price = 135.0, amount = 999, status = 0),
//        StockHistory(id= 3, stockNo = "0050", date = Date(), price = 135.0, amount = 999, status = 1),
//        StockHistory(id= 4, stockNo = "0050", date = Date(), price = 135.0, amount = 999, status = 1),
//        StockHistory(id= 4, stockNo = "2603", date = Date(), price = 80.0, amount = 999, status = 1),
//    )
    var investHistoryList: LiveData<List<InvestHistory>> = MutableLiveData<List<InvestHistory>>()
    val allInvestHistoryList: LiveData<List<InvestHistory>> = newsRepository.allHistory.asLiveData()
//    var investStatistics: MutableLiveData<Map<String, Float>> = MutableLiveData()
    var investStatisticsList: MutableLiveData<List<StockStatistic>> = MutableLiveData()
    init {
        getHeadlines()

    }


    fun getHeadlines(){
       if(isNetworkAvailable()){
           viewModelScope.launch {
               news.postValue(Resource.Loading())
               val response = newsRepository.getHeadlines(country= "tw",category = "business",page = page)
               news.postValue(handleNewsResponse(response))
           }
       }else{
           news.postValue(Resource.Error("No Internet Connection"))
       }

    }

    fun getStockPriceInfo(stockList: List<String> = viewModelStockNoList){
        //Log.d("model getStockPriceInfo","getStockPriceInfo")
        if(isNetworkAvailable()){
            viewModelScope.launch {
                stockPriceInfo.postValue(Resource.Loading())

                val stockListString:String = stockList.joinToString("|") {
                    "tse_${it}.tw"
                }
                val response = newsRepository.getStockPriceInfo(stockNo = stockListString)
                stockPriceInfo.value = handleStockPriceInfoResponse(response)
            }
        }else{
            stockPriceInfo.postValue(Resource.Error("No Internet Connection"))

        }
    }

    fun getRelatedNews(stockName:String){
        if(isNetworkAvailable()) {
            viewModelScope.launch {
                news.postValue(Resource.Loading())
                val response = newsRepository.searchNews(stockName = stockName, page = page)
                news.postValue(handleNewsResponse(response))
            }
        }else{
            news.postValue(Resource.Error("No Internet Connection"))
        }
    }

    fun getCandleStickData(currentDate:String, stockNo: String){
        if (isNetworkAvailable()){
            viewModelScope.launch {
                candleStickData.postValue(Resource.Loading())

                val response = newsRepository.getCandleStickData(currentDate, stockNo)
                candleStickData.value = handleCandleStickDataResponse(response)

            }
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

    private fun handleCandleStickDataResponse(response: Response<CandleStickData>): Resource<CandleStickData>{
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

    fun getStockNoListAndToQueryStockPriceInfo(stockList: List<String> = viewModelStockNoList){
        //Log.d("model check!!!","getStockNoListAndToQueryStockPriceInfo")
        if(stockList.size == viewModelStockNoList.size && stockList.containsAll(viewModelStockNoList)){
            return
        }else{
            viewModelStockNoList = stockList
            getStockPriceInfo(viewModelStockNoList)
        }
    }

    fun isNetworkAvailable(): Boolean {
        //Log.d("viewmodel", "isNetworkAvailable")
        val connectivityManager = getApplication<MyApplication>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
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
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                return true
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

    fun deleteAllHistory(stockNo: String){
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

                mapOfStockNoToAmount[history.stockNo] = history.amount * (if (history.status == 0) 1 else -1)
            } else {
                mapOfStockNoToAmount[history.stockNo] = mapOfStockNoToAmount[history.stockNo]!! + history.amount * (if (history.status == 0) 1 else -1)
            }
        }
        //Log.d("viewmodel", mapOfStockNoToAmount.toString())

        stockPriceInfo.value?.data?.msgArray?.map {

            mapOfStockNoToCurrentPrice[it.c] = (if (it.z != "-") it.z else it.y).toFloat()
        }
        //Log.d("viewmodel mapOfCurrentPrice", mapOfCurrentPrice.toString())

        mapOfStockNoToAmount.map { entry ->
            mapOfStockNoToTotalMoney[entry.key] = entry.value * mapOfStockNoToCurrentPrice[entry.key]!!

        }
        investStatisticsList.value = mapOfStockNoToAmount.map { entry ->

            StockStatistic(entry.key, entry.value * mapOfStockNoToCurrentPrice[entry.key]!!)
        }
        //Log.d("viewmodel mapOfTotalMoney", mapOfStockNoToTotalMoney.toString())

        //investStatistics.postValue(mapOfStockNoToTotalMoney)

        //Log.d("viewmodel investStatistics", investStatistics.value.toString())
    }
}

class NewsViewModelProviderFactory(val newsRepository: NewsRepository, val application: MyApplication): ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return NewsViewModel(newsRepository,application) as T
    }
}