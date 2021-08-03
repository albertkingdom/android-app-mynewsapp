package com.example.mynewsapp.db

import androidx.lifecycle.LiveData
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
}