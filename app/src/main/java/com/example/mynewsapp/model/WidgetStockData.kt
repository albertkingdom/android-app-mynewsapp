package com.example.mynewsapp.model

import java.io.Serializable

data class WidgetStockData(
    val stockNo: String,
    val stockPrice: String,
    val stockName: String,
    val yesterDayPrice: String
): Serializable