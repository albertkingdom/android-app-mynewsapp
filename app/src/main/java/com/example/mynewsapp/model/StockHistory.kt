package com.example.mynewsapp.model

import java.util.*

data class StockHistory (
    var id: Int,
    var stockNo: String,
    var date: Date,
    var price: Double,
    var amount: Int,
    var status: Int //0: buy, 1: sell
)