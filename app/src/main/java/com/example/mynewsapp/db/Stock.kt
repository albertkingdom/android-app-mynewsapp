package com.example.mynewsapp.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stockList")
data class Stock (
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val stockNo: String
)