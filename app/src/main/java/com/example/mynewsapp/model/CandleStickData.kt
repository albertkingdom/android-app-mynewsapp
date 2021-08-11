package com.example.mynewsapp.model

data class CandleStickData(
    val data: List<List<String>>,
    val date: String,
    val fields: List<String>,
    val notes: List<String>,
    val stat: String,
    val title: String
)