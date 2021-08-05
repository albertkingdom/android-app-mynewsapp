package com.example.mynewsapp

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.mynewsapp.databinding.ActivityMainBinding
import com.example.mynewsapp.db.StockDatabase
import com.example.mynewsapp.repository.NewsRepository
import com.example.mynewsapp.ui.NewsViewModel
import com.example.mynewsapp.ui.NewsViewModelProviderFactory


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var viewModel: NewsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val newsRepository = NewsRepository((application as MyApplication).database.stockDao())
        val viewModelFactory = NewsViewModelProviderFactory(newsRepository,
            application as MyApplication
        )
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(NewsViewModel::class.java)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.StockNavHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        navView.setupWithNavController(navController)

//        setSupportActionBar(findViewById(R.id.my_toolbar))
    }
}