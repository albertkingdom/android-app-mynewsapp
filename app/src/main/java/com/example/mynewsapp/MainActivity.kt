package com.example.mynewsapp

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toolbar
import androidx.activity.viewModels
import androidx.annotation.MenuRes
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.ListPopupWindow
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController

import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.example.mynewsapp.databinding.ActivityMainBinding
import com.example.mynewsapp.db.FollowingList
import com.example.mynewsapp.ui.ChatViewModel
import com.example.mynewsapp.ui.ListFragmentDirections
import com.example.mynewsapp.ui.NewsViewModel
import com.example.mynewsapp.ui.NewsViewModelFactory
import com.google.android.material.navigation.NavigationBarView


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var viewModel: NewsViewModel
    val chatViewModel: ChatViewModel by viewModels()
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.my_toolbar))

        val navView: BottomNavigationView = binding.navView

       // Initialize view model ==============
        val viewModelFactory = NewsViewModelFactory((application as MyApplication).repository,
            application as MyApplication
        )
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(NewsViewModel::class.java)
        //================
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.StockNavHostFragment) as NavHostFragment
        navController = navHostFragment.navController
        navView.setupWithNavController(navController)


        appBarConfiguration = AppBarConfiguration(setOf(R.id.stockListFragment, R.id.news, R.id.statisticFragment))
        setupActionBarWithNavController(navController, appBarConfiguration)

        // determine whether to reload tabs based on current selected bottom nav menu items
        navView.setOnItemSelectedListener(object : NavigationBarView.OnItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                // newly selected menu is the current one -> don't reload
                if (item.itemId == navView.selectedItemId) {
                    return false
                }
                // newly selected menu is not the current one -> load new tab
                NavigationUI.onNavDestinationSelected(item, navController)
                return true
            }

        })


    }
    override fun onSupportNavigateUp(): Boolean {

        return navController.navigateUp(appBarConfiguration)
    }


    fun showMenuSelectorBtn(menuItems: List<FollowingList>) {
        binding.showListMenuButton.visibility = View.VISIBLE
        setupMenuSelectorBtn(menuItems)
    }
    fun hideMenuSelectorBtn() {
        binding.showListMenuButton.visibility = View.INVISIBLE
    }
    private fun setupMenuSelectorBtn(menuItems: List<FollowingList>) {
        val menuSelectorButton = binding.showListMenuButton
        val listPopupWindow = ListPopupWindow(this, null, R.attr.listPopupWindowStyle)


        // Set button as the list popup's anchor
        listPopupWindow.anchorView = menuSelectorButton


        // Set list popup's content
        val listNameArray = menuItems.map { list -> list.listName }
        val listNameArrayAndEdit = mutableListOf<String>()
        listNameArrayAndEdit.addAll(listNameArray)
        listNameArrayAndEdit.add("Edit...")
        val adapter = ArrayAdapter(this, R.layout.menu_list_selector_item, listNameArrayAndEdit)
        listPopupWindow.setAdapter(adapter)
        // Set list popup's item click listener
        listPopupWindow.setOnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
            // Respond to list popup window item click.
            if (position == listNameArrayAndEdit.lastIndex) {
                // go to edit following list page
                println("click editing btn of following list")
                findNavController(R.id.StockNavHostFragment).navigate(ListFragmentDirections.actionListFragmentToAddFollowingListDialogFragment())
                // Dismiss popup.
                listPopupWindow.dismiss()
                return@setOnItemClickListener
            }
            println("click on list menu $position name = ${listNameArray[position]}")
            viewModel.currentSelectedFollowingListId.postValue(menuItems[position].followingListId)
            //menuSelectorButton.text = menuItems[position].listName
            // Dismiss popup.
            listPopupWindow.dismiss()
        }

        // Show list popup window on button click.
        menuSelectorButton.setOnClickListener { v: View? -> listPopupWindow.show() }

        viewModel.currentSelectedFollowingListId.observe(this, { listId ->
            menuSelectorButton.text =
                menuItems.find { list -> list.followingListId.equals(listId) }?.listName
        })
    }



}