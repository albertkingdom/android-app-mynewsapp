package com.example.mynewsapp.ui.list

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.work.*
import com.example.mynewsapp.widget.UpdateWidgetPeriodicTask
import com.example.mynewsapp.MyApplication
import com.example.mynewsapp.db.FollowingList
import com.example.mynewsapp.db.FollowingListWithStock
import com.example.mynewsapp.db.Stock
import com.example.mynewsapp.model.StockPriceInfoResponse
import com.example.mynewsapp.repository.NewsRepository
import com.example.mynewsapp.util.Constant.Companion.NO_INTERNET_CONNECTION
import com.example.mynewsapp.util.Constant.Companion.WORKER_INPUT_DATA_KEY
import com.example.mynewsapp.util.Resource
import com.example.mynewsapp.util.isNetworkAvailable
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.*
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit


// inherit AndroidViewModel to obtain context
class ListViewModel(
    val repository: NewsRepository,
    application: Application
) : AndroidViewModel(application) {
    companion object {
        const val TAG = "ListViewModel"
    }
    val currentSelectedFollowingListId: MutableLiveData<Int> = MutableLiveData(0)

    val allFollowingList: LiveData<List<FollowingList>> = repository.allFollowingList.asLiveData()

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

    val stockPriceInfo = MutableLiveData<Resource<StockPriceInfoResponse>>()

    val stockIdsInCurrentList = MutableLiveData<List<String>>() // a copy of following stock ids

    val compositeDisposable = CompositeDisposable()

    init {
        println("ListViewModel INIT")
    }
    /*
    1. change following list id
    2. fetch single list -> followingListWithStocks
    3. stockPriceInfo
     */
    private fun setupGetStockPriceDataPipe(followingListId: Int) {
        compositeDisposable.clear() // cancel existing subscription
        val observable = Observable.just(followingListId)

        val observer = object : Observer<Resource<StockPriceInfoResponse>> {
            override fun onSubscribe(d: Disposable) {
                //Log.d(TAG, "onSubscribe")
                compositeDisposable.add(d)
            }

            override fun onNext(t: Resource<StockPriceInfoResponse>) {
                stockPriceInfo.value = t
            }

            override fun onError(e: Throwable) {
                //Log.d(TAG, "onError $e")
                stockPriceInfo.value = e.message?.let { Resource.Error(it) }
            }

            override fun onComplete() {
                //Log.d(TAG, "onComplete")
            }
        }
        // repeat every 5 min
        observable
            .subscribeOn(Schedulers.io())
            .repeatWhen { complete -> complete.delay(5, TimeUnit.MINUTES) }
            .flatMap { num ->
                // use flowable so that new data will be auto emitted
                fetchSingleListRx(num).toObservable()
            }
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap { list ->
                getCurrentStockIds(list)
                Observable.just(list)
            }
            .observeOn(Schedulers.io())
            .flatMap { list ->
                if (list.stocks.isEmpty() && isNetworkAvailable(getApplication())) {
                    // return empty array, don't fetch api
                    Observable.just(Resource.Success(StockPriceInfoResponse(listOf())))
                } else if (isNetworkAvailable(getApplication())) {
                    fetchStockPriceInfoRx(list).toObservable()
                } else {
                    Observable.error(Throwable(NO_INTERNET_CONNECTION))
                }
            }
            .retry(2)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observer)
    }
    fun changeCurrentFollowingList(index: Int) {
        val followingListId = allFollowingList.value?.get(index)?.followingListId!!
        currentSelectedFollowingListId.value = allFollowingList.value?.get(index)?.followingListId
        setupGetStockPriceDataPipe(followingListId)
    }
    fun createFollowingList(followingList: FollowingList) {
        viewModelScope.launch {
            repository.insertFollowingList(followingList)
        }
    }

    fun changeCurrentFollowingListId(id: Int? = currentSelectedFollowingListId.value) {
        if (id !== null) {
            currentSelectedFollowingListId.value = id
            setupGetStockPriceDataPipe(id)
        }
    }


    private fun fetchStockPriceInfoRx(followingListWithStocks: FollowingListWithStock): Single<Resource<StockPriceInfoResponse>> {
        val stockNoStringList = followingListWithStocks.stocks.map { stock -> stock.stockNo }
        println("stockNoStringList $stockNoStringList")
        // setup workmanager
        setupWorkManagerForUpdateWidget(stockNoStringList)
        return getStockPriceInfoRx(stockNoStringList)
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
        // ??????work request
        val workRequest =
            PeriodicWorkRequestBuilder<UpdateWidgetPeriodicTask>(15L, TimeUnit.MINUTES)
                .addTag(WORK_TAG)
                .setInputData(inputData)
                .build()


        // ??????work request ???system
        // ????????????(or???????????????)
        WorkManager.getInstance(getApplication()).enqueueUniquePeriodicWork(
            WORK_TAG,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    private fun fetchSingleListRx(followingListId: Int): Flowable<FollowingListWithStock> {
        return repository.getOneListWithStocksRx(followingListId)
    }
    fun deleteFollowingList(followingListId: Int) {
        viewModelScope.launch {
            repository.deleteFollowingList(followingListId)
        }
    }

    private fun getStockPriceInfoRx(stockList: List<String>): Single<Resource<StockPriceInfoResponse>> {
        val stockListString: String = stockList.joinToString("|") {
            "tse_${it}.tw"
        }
        val response = repository.getStockPriceInfoRx(stockListString)
        return response
            .map {
                handleStockPriceInfoResponse(it)
            }
    }

    private fun handleStockPriceInfoResponse(response: Response<StockPriceInfoResponse>): Resource<StockPriceInfoResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    fun addToStockList(stockNo: String, followingListId: Int = currentSelectedFollowingListId.value!!) {
        viewModelScope.launch {
            if (stockIdsInCurrentList.value?.indexOf(stockNo) == -1) {
                repository.insert(stock = Stock(0, stockNo, followingListId))
            }
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
    private fun getCurrentStockIds(list: FollowingListWithStock) {
        val stockIds = list.stocks.map { stock ->
            stock.stockNo
        }
        stockIdsInCurrentList.value = stockIds
    }

    fun deleteAllHistory(stockNo: String) {
        viewModelScope.launch {
            repository.deleteAllHistory(stockNo)
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}

class ListViewModelFactory(val repository: NewsRepository, val application: MyApplication): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
       return ListViewModel(repository = repository, application = application) as T
    }

}






