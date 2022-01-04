package com.example.mynewsapp.db

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InvestHistoryDao {

    @Query("SELECT * FROM investHistory WHERE stockNo = :stockNo")
    suspend fun getHistoryByStockNo(stockNo: String): List<InvestHistory>
}