package com.example.mynewsapp.model

data class QueryTime(
    val sessionFromTime: Long,
    val sessionLatestTime: Long,
    val sessionStr: String,
    val showChart: Boolean,
    val stockInfo: Int,
    val stockInfoItem: Int,
    val sysDate: String,
    val sysTime: String
)