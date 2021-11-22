package com.example.mynewsapp.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {

    @Query("SELECT * FROM stockList")
    fun getAllStocks(): Flow<List<Stock>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stock:Stock)

    @Query("DELETE FROM stockList WHERE stockNo = :stockNumberToDel")
    suspend fun delete(stockNumberToDel:String)

    @Query("SELECT * FROM investHistory WHERE stockNo = :stockNo")
    fun getHistoryByStockNo(stockNo: String): Flow<List<InvestHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(investHistory: InvestHistory)

    @Query("DELETE FROM investHistory WHERE stockNo = :stockNo")
    suspend fun deleteAllHistory(stockNo:String)
}