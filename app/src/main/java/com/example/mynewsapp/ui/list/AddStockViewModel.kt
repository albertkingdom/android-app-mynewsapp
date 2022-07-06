package com.example.mynewsapp.ui.list

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mynewsapp.model.StockIdNameStar
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

class AddStockViewModel: ViewModel() {
    companion object {
        val TAG = "AddStockViewModel"
    }
    private var searchQuery = ""
    var stockNames = mutableListOf<String>()
    var followingStockIds = listOf<String>()
    val filteredStockNames = MutableLiveData<List<StockIdNameStar>>()
    val compositeDisposable = CompositeDisposable()

    init {
        filterSearchQuery()
    }
    fun updateSearchQuery(query: String) {
        searchQuery = query
    }
    fun filterSearchQuery() {
        compositeDisposable.clear()
        //Log.d(TAG, "filterSearchQuery")
        //Log.d(TAG, "following ids $followingStockIds")
        val observer = object : Observer<List<String>> {
            override fun onSubscribe(d: Disposable) {
                compositeDisposable.add(d)
            }

            override fun onNext(listOfString: List<String>) {
                //Log.d(TAG, "filterSearchQuery onNext $listOfString")
                val stockNameAndStarList = listOfString.map { stockIdName ->
                    val stockId = stockIdName.split(" ").first()
                    if (followingStockIds.indexOf(stockId) != -1) {
                        //Log.d(TAG, "stockId $stockId true")
                        return@map StockIdNameStar(stockIdName, true)
                    }
                    //Log.d(TAG, "stockId $stockId false")

                    StockIdNameStar(stockIdName, false)
                }
                filteredStockNames.value = stockNameAndStarList
            }

            override fun onError(e: Throwable) {
                Log.e(TAG, e.toString())
            }

            override fun onComplete() {
                Log.d(TAG, "onComplete")
            }

        }
        Observable
            .just(searchQuery)
            .map {
                stockNames.filter { string ->
                   string.contains(searchQuery)
               }
            }
            .subscribe(observer)
    }
}