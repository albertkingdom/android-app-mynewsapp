package com.example.mynewsapp

data class StockPriceInfoResponse(
    val cachedAlive: Int,
    val exKey: String,
    val msgArray: List<MsgArray>,
    val queryTime: QueryTime,
    val referer: String,
    val rtcode: String,
    val rtmessage: String,
    val userDelay: Int
)