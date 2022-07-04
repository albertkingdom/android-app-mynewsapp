package com.example.mynewsapp.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynewsapp.db.InvestHistory
import com.example.mynewsapp.repository.NewsRepository
import com.example.mynewsapp.util.InputDataStatus
import kotlinx.coroutines.launch

class AddHistoryViewModel(val repository: NewsRepository): ViewModel() {

    fun insertHistory(investHistory: InvestHistory) {
        viewModelScope.launch {
            repository.insertHistory(investHistory)
        }
    }

    fun checkDataInput(stockNo: String, amount: Int?, price: Double?, date: Long?): InputDataStatus {
        if (stockNo.isEmpty()) {
            return InputDataStatus.InvalidStockNo

        } else if (amount == 0 || amount == null) {
           return InputDataStatus.InvalidAmount

        } else if (price == 0.0 || price == null) {
            return InputDataStatus.InvalidPrice

        } else if (date == null) {
            return InputDataStatus.InvalidDate
        }
        return InputDataStatus.OK
    }


}