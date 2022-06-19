package com.example.mynewsapp.ui.list

import android.app.Application
import androidx.lifecycle.*
import androidx.work.*
import com.example.mynewsapp.widget.UpdateWidgetPeriodicTask
import com.example.mynewsapp.MyApplication
import com.example.mynewsapp.db.FollowingList
import com.example.mynewsapp.db.FollowingListWithStock
import com.example.mynewsapp.db.Stock
import com.example.mynewsapp.model.StockPriceInfoResponse
import com.example.mynewsapp.repository.NewsRepository
import com.example.mynewsapp.util.Constant.Companion.WORKER_INPUT_DATA_KEY
import com.example.mynewsapp.util.Resource
import com.example.mynewsapp.util.isNetworkAvailable
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.*
import retrofit2.Response
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit


// inherit AndroidViewModel to obtain context
class ListViewModel(
    val repository: NewsRepository,
    application: Application
) : AndroidViewModel(application) {

    val currentSelectedFollowingListId: MutableLiveData<Int> = MutableLiveData(0)

    val allFollowingList: LiveData<List<FollowingList>> = repository.allFollowingList.asLiveData()

    private var fetchPriceJob: Job? = null

    val appBarMenuButtonTitle = currentSelectedFollowingListId.map { listId ->
        allFollowingList.value?.find { list -> list.followingListId == listId }?.listName
    }

    val appBarMenuItemNameList = allFollowingList.map { listOfFollowingLists ->
        if (listOfFollowingLists.isEmpty()) {
            val followingList = FollowingList(followingListId = 0, listName = "Default")
            createFollowingList(followingList)
        }
        if (listOfFollowingLists.isNotEmpty()) {
            changeCurrentFollowingList(0)
        }

        // Set list popup's content
        val listNameArrayAndEdit = mutableListOf<String>()
        listNameArrayAndEdit.addAll(listOfFollowingLists.map { followingList -> followingList.listName })
        listNameArrayAndEdit.add("Edit...")
        return@map listNameArrayAndEdit
    }

    init {
        println("ListViewModel INIT")
    }

    fun changeCurrentFollowingList(index: Int) {
        currentSelectedFollowingListId.value = allFollowingList.value?.get(index)?.followingListId
    }
    fun createFollowingList(followingList: FollowingList) {
        viewModelScope.launch {
            repository.insertFollowingList(followingList)
        }
    }

    fun changeCurrentFollowingListId(id: Int? = currentSelectedFollowingListId.value) {
        if (id !== null) {
            currentSelectedFollowingListId.value = id
        }
    }

    // observe currentSelectedListId -> fetchSingleList -> change followingListWithStocks
    val followingListWithStocks = currentSelectedFollowingListId.switchMap { id ->
        fetchSingleList(id)
    }
    val stockPriceInfo =
        Transformations.switchMap(followingListWithStocks) { listWithStock ->
            println("singleFollowingListWithStocks $listWithStock")
            //var result: FollowingListWithStock? = null
            val stockPrice = MutableLiveData<Resource<StockPriceInfoResponse>>()

            if (listWithStock == null || listWithStock.stocks.isEmpty()) {
                stockPrice.value = Resource.Success(StockPriceInfoResponse(msgArray = listOf()))

                return@switchMap stockPrice
            }
            fetchPriceJob?.cancel()

            fetchPriceJob = viewModelScope.launch {

                if (listWithStock != null) {

                    val stockNoStringList = listWithStock.stocks.map { stock -> stock.stockNo }
                    println("stockNoStringList $stockNoStringList")
                    //getStockNoListAndToQueryStockPriceInfo(stockNoStringList)

                    /// setup workmanager
                    setupWorkManagerForUpdateWidget(stockNoStringList)

                    while (true) {
                        if (!isNetworkAvailable(getApplication())) {
                            stockPrice.value = Resource.Error("No Internet Connection")
                            delay(1000 * 1 * 60)
                            continue
                        }
                        stockPrice.value = getStockPriceInfo(stockNoStringList).await()
                        delay(1000 * 1 * 60 * 2)
                    }

                }
            }

            println("stockPrice $stockPrice")
            //MutableLiveData<Int>()
            stockPrice
        }

    private fun setupWorkManagerForUpdateWidget(stockNos: List<String>) {
        val WORK_TAG = "fetch_stock_price_update_widget"
        // convert to json string
        val moshi: Moshi = Moshi.Builder().build()
        val type: Type = Types.newParameterizedType(
            List::class.java,
            String::class.java
        )
        val jsonAdapter = moshi.adapter<List<String>>(type)
        val inputData = Data.Builder()
            .putString(WORKER_INPUT_DATA_KEY, jsonAdapter.toJson(stockNos))
            .build()
        // 建立work request
        val workRequest =
            PeriodicWorkRequestBuilder<UpdateWidgetPeriodicTask>(15L, TimeUnit.MINUTES)
                .addTag(WORK_TAG)
                .setInputData(inputData)
                .build()


        // 註冊work request 到system
        // 重複任務(or一次性任務)
        WorkManager.getInstance(getApplication()).enqueueUniquePeriodicWork(
            WORK_TAG,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
    fun fetchSingleList(followingListId: Int): LiveData<FollowingListWithStock> {
        val result = MutableLiveData<FollowingListWithStock>()
        viewModelScope.launch {
            result.value = repository.getOneListWithStocks(followingListId)

        }
        return result
    }

    fun deleteFollowingList(followingListId: Int) {

        viewModelScope.launch {
            repository.deleteFollowingList(followingListId)
        }

    }

    private fun getStockPriceInfo(stockList: List<String>): Deferred<Resource<StockPriceInfoResponse>> =

        viewModelScope.async {

            val stockListString: String = stockList.joinToString("|") {
                "tse_${it}.tw"
            }

            val response = repository.getStockPriceInfo(stockNo = stockListString)
            val result = handleStockPriceInfoResponse(response)
            result

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

    fun addToStockList(stockNo: String, followingListId: Int) {

        viewModelScope.launch {

            println("addToStockList stockNo = $stockNo")
            repository.insert(stock = Stock(0, stockNo, followingListId))

        }

    }

    fun deleteStockByStockNoAndListId(
        stockNo: String,
        followingListId: Int = currentSelectedFollowingListId.value!!
    ) {
        viewModelScope.launch {
            repository.deleteStockByStockNoAndListId(stockNo, followingListId)
            changeCurrentFollowingListId()
        }
    }

    fun deleteAllHistory(stockNo: String) {
        viewModelScope.launch {
            repository.deleteAllHistory(stockNo)
        }
    }


    fun cancelRepeatFetchPriceJob() {
        fetchPriceJob?.cancel()
    }

}

class ListViewModelFactory(val repository: NewsRepository, val application: MyApplication): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
       return ListViewModel(repository = repository, application = application) as T
    }

}






