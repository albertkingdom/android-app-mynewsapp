package com.example.mynewsapp.repository

import com.example.mynewsapp.model.NewsResponse
import com.example.mynewsapp.model.StockPriceInfoResponse
import com.example.mynewsapp.api.RetrofitInstance
import com.example.mynewsapp.api.RetrofitInstanceForCandleStickData
import com.example.mynewsapp.api.RetrofitInstanceForStockPrice
import com.example.mynewsapp.db.*


import com.example.mynewsapp.model.CandleStickData
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

class NewsRepository(val stockDao: StockDao) {
    suspend fun searchNews(stockName:String="台積電", page:Int):Response<NewsResponse>{
        return RetrofitInstance.retrofitService.searchForNews(stockName, page)
    }

    suspend fun getHeadlines(country:String, page: Int, category: String):Response<NewsResponse>{
        return RetrofitInstance.retrofitService.getHeadlines(country, category,page)
    }

    suspend fun getStockPriceInfo(stockNo:String):Response<StockPriceInfoResponse>{
        return RetrofitInstanceForStockPrice.retrofitService.getStockPriceInfo(stockNo)
    }

    val allstocks: Flow<List<Stock>> =stockDao.getAllStocks()

    val allFollowingList: Flow<List<FollowingList>> = stockDao.getAllFollowingList()
    //val allFollowingListWithStock: List<FollowingListWithStock> = stockDao.getAllListsWithStocks()
    fun getAllFollowingList() {
        //return stockDao.getAllListsWithStocks()
        stockDao.getAllFollowingList()

    }
    suspend fun getOneListWithStocks(followingListId: Int): FollowingListWithStock{
        return stockDao.getListsWithStocks(followingListId)
    }
    suspend fun insertFollowingList(followingList: FollowingList) {
        stockDao.insertFollowingList(followingList = followingList)
    }

    suspend fun deleteFollowingList(followingListId: Int) {
        stockDao.deleteFollowingList(followingListId)
        stockDao.deleteStockAfterDeleteFollowingList(followingListId)
    }
    suspend fun insert(stock:Stock){
        stockDao.insert(stock = stock)
    }

    suspend fun delStock(stockNo:String){
        stockDao.delete(stockNo)
    }
    suspend fun deleteStockByStockNoAndListId(stockNo: String, followingListId: Int){
        stockDao.deleteStockByStockNoAndListId(stockNo, followingListId)
    }
    suspend fun getCandleStickData(currentDate:String, stockNo: String):Response<CandleStickData>{
        return  RetrofitInstanceForCandleStickData.retrofitService.getCandleStickData(currentDate, stockNo)
    }

    val allHistory: Flow<List<InvestHistory>> = stockDao.getAllHistory()
    
    fun queryHistoryByStockNo(stockNo: String): Flow<List<InvestHistory>> {
        return stockDao.getHistoryByStockNo(stockNo)
    }

    suspend fun insertHistory(investHistory: InvestHistory){
        stockDao.insertHistory(investHistory)
    }

    suspend fun deleteAllHistory(stockNo: String){
        stockDao.deleteAllHistory(stockNo)
    }
}