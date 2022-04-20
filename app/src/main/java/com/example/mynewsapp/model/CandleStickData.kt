package com.example.mynewsapp.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CandleStickData(
    val data: List<List<String>>,
    val date: String,
    val fields: List<String>,
    val notes: List<String>,
    val stat: String,
    val title: String
)