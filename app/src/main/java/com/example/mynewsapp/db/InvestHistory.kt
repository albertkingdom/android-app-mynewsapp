package com.example.mynewsapp.db

import androidx.room.Entity
import androidx.room.PrimaryKey

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
