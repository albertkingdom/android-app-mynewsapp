package com.example.mynewsapp

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController

import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.mynewsapp.databinding.ActivityMainBinding
import com.example.mynewsapp.db.StockDatabase
import com.example.mynewsapp.repository.NewsRepository
import com.example.mynewsapp.ui.NewsViewModel
import com.example.mynewsapp.ui.NewsViewModelProviderFactory


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var viewModel: NewsViewModel
    private lateinit var appBarConfiguration: AppBarConfiguration

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

        appBarConfiguration = AppBarConfiguration(setOf(R.id.listFragment, R.id.newsFragment))

        setupActionBarWithNavController(navController, appBarConfiguration)


    }
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.StockNavHostFragment)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}