package com.example.mynewsapp.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "stockList")
data class Stock (
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val stockNo: String
)


@Entity(tableName = "investHistory")
data class InvestHistory (
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val stockNo: String,
    val date: Long,
    val price: Double,
    val amount: Int,
    val status: Int //0: buy, 1: sell
)