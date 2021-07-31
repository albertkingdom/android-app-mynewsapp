package com.example.mynewsapp.ui

import NewsResponse
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mynewsapp.repository.NewsRespository
import com.example.mynewsapp.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response

class NewsViewModel(val newsRespository: NewsRespository):ViewModel() {
    var page = 1
    val news:MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    init {
        getHeadlines()
    }
    fun getHeadlines(){

        viewModelScope.launch {
            news.postValue(Resource.Loading())
            val response = newsRespository.getHeadlines(country= "tw",category = "business",page = page)
            news.postValue(handleNewsResponse(response))
        }
    }

    private fun handleNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse>{
        if (response.isSuccessful){
            response.body()?.let {
                resultResponse->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }
}

class NewsViewModelProviderFactory(val newsRespository: NewsRespository): ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return NewsViewModel(newsRespository) as T
    }
}