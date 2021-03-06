package com.example.mynewsapp.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "stocks")
data class Stock (
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val stockNo: String,
    @ColumnInfo(defaultValue = "0")
    val parentFollowingListId: Int
)