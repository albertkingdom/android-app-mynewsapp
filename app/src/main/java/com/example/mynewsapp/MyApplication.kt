package com.example.mynewsapp

import android.app.Application
import com.example.mynewsapp.db.StockDatabase
import com.example.mynewsapp.repository.NewsRepository

class MyApplication:Application() {
    val database by lazy { StockDatabase.getDatabase(this) }
    val repository by lazy { NewsRepository(database.stockDao()) }
}