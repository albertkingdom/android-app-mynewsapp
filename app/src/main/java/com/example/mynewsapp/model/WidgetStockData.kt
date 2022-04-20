package com.example.mynewsapp.model

import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class WidgetStockData(
    val stockNo: String,
    val stockPrice: String,
    val stockName: String,
    val yesterDayPrice: String
): Serializable