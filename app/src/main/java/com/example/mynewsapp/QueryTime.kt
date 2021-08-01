package com.example.mynewsapp

data class QueryTime(
    val sessionFromTime: Int,
    val sessionLatestTime: Int,
    val sessionStr: String,
    val showChart: Boolean,
    val stockInfo: Int,
    val stockInfoItem: Int,
    val sysDate: String,
    val sysTime: String
)