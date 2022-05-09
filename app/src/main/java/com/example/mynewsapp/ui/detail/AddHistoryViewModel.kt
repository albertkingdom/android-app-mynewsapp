package com.example.mynewsapp.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynewsapp.db.InvestHistory
import com.example.mynewsapp.repository.NewsRepository
import kotlinx.coroutines.launch

class AddHistoryViewModel(val repository: NewsRepository): ViewModel() {

    fun insertHistory(investHistory: InvestHistory) {
        viewModelScope.launch {
            repository.insertHistory(investHistory)
        }
    }
}