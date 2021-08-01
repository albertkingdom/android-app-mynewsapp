package com.example.mynewsapp.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.example.mynewsapp.MainActivity
import com.example.mynewsapp.R
import com.example.mynewsapp.adapter.NewsAdapter
import com.example.mynewsapp.databinding.FragmentNewsBinding
import com.example.mynewsapp.util.Resource

class NewsFragment : Fragment(R.layout.fragment_news) {

    private lateinit var viewModel: NewsViewModel

    private var binding: FragmentNewsBinding? = null
    private lateinit var newsAdapter: NewsAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentNewsBinding.bind(view)

        val recyclerView: RecyclerView = binding!!.newsRecyclerview

        newsAdapter = NewsAdapter()
        recyclerView.adapter = newsAdapter

        viewModel = (activity as MainActivity).viewModel

        viewModel.news.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    response.data?.let { newsResponse ->
                        newsAdapter.submitList(newsResponse.articles)
                    }
                    hideProgressbar()
                }
                is Resource.Error -> {
                    response.message?.let { message ->
                        Log.e("newsfragment", "An error occured: $message")
                    }
                    showProgressbar()
                }
                is Resource.Loading -> {
                    showProgressbar()
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun hideProgressbar() {
        binding?.paginationProgressBar?.visibility = View.INVISIBLE
    }

    private fun showProgressbar() {
        binding?.paginationProgressBar?.visibility = View.VISIBLE
    }
}