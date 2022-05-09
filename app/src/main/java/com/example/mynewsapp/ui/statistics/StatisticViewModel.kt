package com.example.mynewsapp.ui.statistics

import android.graphics.Color
import android.util.Log
import androidx.lifecycle.*
import com.example.mynewsapp.db.InvestHistory
import com.example.mynewsapp.model.StockPriceInfoResponse
import com.example.mynewsapp.model.StockStatistic
import com.example.mynewsapp.repository.NewsRepository
import com.example.mynewsapp.util.Resource
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlin.random.Random

class StatisticViewModel(val repository: NewsRepository): ViewModel() {
    private val allInvestHistoryList: LiveData<List<InvestHistory>> = repository.allHistory.asLiveData()

    private val stockPriceInfo = MutableLiveData<Resource<StockPriceInfoResponse>>()

    var investStatisticsList: LiveData<List<StockStatistic>> = Transformations.map(allInvestHistoryList) { listOfInvestHistory ->

            calculatePieChartSourceData(listOfInvestHistory)

    }

    var pieData: LiveData<PieData> = Transformations.map(investStatisticsList) { listOfStockStatistic ->
        calculatePieData(listOfStockStatistic)
    }

    fun setStockPriceInfo(stockPrice: Resource<StockPriceInfoResponse>) {
        stockPriceInfo.value = stockPrice
    }

    private fun calculatePieChartSourceData(list: List<InvestHistory>): List<StockStatistic> {

        val mapOfStockNoToAmount = mutableMapOf<String, Int>() // {0056 to 200}
        val mapOfStockNoToCurrentPrice = mutableMapOf<String, Float>() //{0056 to 32}
        val mapOfStockNoToTotalMoney = mutableMapOf<String, Float>() // {0056 to 200*32}

        list.map { history ->
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

            mapOfStockNoToCurrentPrice[it.stockNo] = (if (it.currentPrice != "-") it.currentPrice else it.lastDayPrice).toFloat()
        }
        //Log.d("viewmodel mapOfCurrentPrice", mapOfCurrentPrice.toString())

        mapOfStockNoToAmount.map { entry ->
            if (mapOfStockNoToCurrentPrice[entry.key] != null) {
                mapOfStockNoToTotalMoney[entry.key] =
                    entry.value * mapOfStockNoToCurrentPrice[entry.key]!!
            }

        }


        val listOfStockStatistic: List<StockStatistic> = mapOfStockNoToTotalMoney.map { entry ->
            StockStatistic(entry.key, entry.value)
        }

        return listOfStockStatistic
        //Log.d("viewmodel mapOfTotalMoney", mapOfStockNoToTotalMoney.toString())

        //investStatistics.postValue(mapOfStockNoToTotalMoney)

        //Log.d("viewmodel investStatistics", investStatistics.value.toString())
    }

    private fun calculatePieData(dataSource: List<StockStatistic>): PieData {
        val TAG = "createPieChart"
        // entries -> pieDataset -> pieData -> pieChart
        val colors = mutableListOf<Int>()
        var entries: List<PieEntry>

        Log.d(TAG, "customMap...${dataSource}")

        if (dataSource.isEmpty()) {
            return PieData()
        }

        dataSource.map { entry->
            val randomColor = Color.argb(255, Random.nextInt(0, 255), Random.nextInt(0, 255), Random.nextInt(0, 255))
            colors.add(randomColor)
        }

        val sumOfAsset: Float =
            dataSource.map { stockStatistic -> stockStatistic.totalAssets }.reduce{ acc, item -> acc + item }
        //Log.d(TAG, "sumOfAsset...$sumOfAsset")
        entries = dataSource.map{ entry -> PieEntry(entry.totalAssets / sumOfAsset * 100, entry.stockNo) }

        //Log.d(TAG, "entries..$entries")


        val pieDataSet: PieDataSet = PieDataSet(entries, "stockNo.")
        pieDataSet.colors = colors

        val pieData: PieData = PieData(pieDataSet)

        return pieData

    }
}