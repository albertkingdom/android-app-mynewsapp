package com.example.mynewsapp.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.mynewsapp.MainActivity
import com.example.mynewsapp.R
import com.example.mynewsapp.adapter.NewsAdapter
import com.example.mynewsapp.databinding.FragmentNewsBinding
import com.example.mynewsapp.util.Resource
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NewsFragment : Fragment(R.layout.fragment_news) {

    private val viewModel: NewsViewModel by activityViewModels()

    private lateinit var binding: FragmentNewsBinding
    private lateinit var newsAdapter: NewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.getHeadlines()
        return super.onCreateView(inflater, container, savedInstanceState)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //change toolbar title
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "News"
        binding = FragmentNewsBinding.bind(view)

        val recyclerView: RecyclerView = binding.newsRecyclerview

        newsAdapter = NewsAdapter()
        newsAdapter.setClickListener{

            val webpage: Uri = Uri.parse(it.url)
            val builder = CustomTabsIntent.Builder()
            val customTabsIntent = builder.build()

            customTabsIntent.launchUrl(requireContext(), webpage)

        }
        recyclerView.adapter = newsAdapter

        //viewModel = (activity as MainActivity).viewModel

        viewModel.news.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    response.data?.let { newsResponse ->
                        newsAdapter.submitList(newsResponse.articles)
                    }
                    binding.swipeRefresh.isRefreshing = false
                }
                is Resource.Error -> {
                    response.message?.let { message ->
                        //Log.e("newsfragment", "An error occured: $message")
                        Snackbar.make(view, "An error occured: $message", Snackbar.LENGTH_LONG).show()
                    }
                }
                is Resource.Loading -> {
                    binding.swipeRefresh.isRefreshing = true
                }
            }
        })

        binding.swipeRefresh.setOnRefreshListener {
            //Log.d("list fragment", "pull to refresh")
            viewModel.getHeadlines()

        }
    }

}