package com.example.mynewsapp.ui

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle

import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.mynewsapp.MainActivity
import com.example.mynewsapp.StockAppWidgetProvider

import com.example.mynewsapp.R
import com.example.mynewsapp.adapter.StockInfoAdapter
import com.example.mynewsapp.databinding.FragmentListBinding
import com.example.mynewsapp.db.FollowingList
import com.example.mynewsapp.model.MsgArray
import com.example.mynewsapp.model.WidgetStockData
import com.example.mynewsapp.util.Resource
import com.google.android.material.snackbar.Snackbar
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types


class ListFragment : Fragment() {
    private lateinit var binding: FragmentListBinding
    private val viewModel: NewsViewModel by activityViewModels()

    private lateinit var stockAdapter: StockInfoAdapter
    private lateinit var swipeBackground: ColorDrawable
    private lateinit var deleteIcon: Drawable

    val TAG = "ListFragment"
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentListBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")
        val recyclerView: RecyclerView = binding.stockListRecyclerview
        //change toolbar title
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "自選股"


        stockAdapter = StockInfoAdapter(getStockNameToGetRelatedNews, toCandelStickChartFragment)
        recyclerView.adapter = stockAdapter

        viewModel.stockPriceInfo.observe(viewLifecycleOwner, Observer { response ->


            when (response) {
                is Resource.Success -> {
                    response.data?.let { stockInfoResponse ->
                        //stockAdapter.submitList(stockInfoResponse.msgArray)
                        val listOfMsgArray = stockInfoResponse.msgArray
                        stockAdapter.setData(stockInfoResponse.msgArray.toMutableList())

                        val listOfWidgetStockData = listOfMsgArray.map { msgArray ->
                            WidgetStockData(stockNo = msgArray.c, stockPrice = msgArray.z, stockName = msgArray.n, yesterDayPrice = msgArray.y)
                        }

                        updateWidget(listOfWidgetStockData)



                    }
                    binding.swipeRefresh.isRefreshing = false
                }
                is Resource.Error -> {

                    response.message?.let { message ->
                        Log.e("stock list fragment", "An error occured: $message")
                        Snackbar.make(view, "An error occured: $message", Snackbar.LENGTH_LONG).show()
                    }
                }
                is Resource.Loading -> {
                    binding.swipeRefresh.isRefreshing = true
                }
            }

        })

        // get following lists and stocks
        viewModel.allFollowingList.observe(viewLifecycleOwner, { lists ->
            println("allFollowingListWithStock $lists")
            if (lists.isEmpty()) {
                val followingList = FollowingList(followingListId = 0,listName = "Default")
                viewModel.createFollowingList(followingList)
            }
            if (lists.isNotEmpty()) {
                viewModel.currentSelectedFollowingListId.postValue(lists.first().followingListId)
            }
            (activity as MainActivity).showMenuSelectorBtn(lists)


        })
        // observe selected list id and get
        viewModel.currentSelectedFollowingListId.observe(viewLifecycleOwner, { listId ->
            println("currentSelectedFollowingListId  $listId")
            viewModel.getOneFollowingListWithStocks(listId)
        })

        binding.floatingBtn.setOnClickListener {
            val dialog = CustomDialogFragment()

            dialog.show(parentFragmentManager,"stock")
        }


        /**
         * swipe to delete a stockNo from db
         */
        ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                val currentStockItem = stockAdapter.currentList[viewHolder.adapterPosition]

                //viewModel.deleteStock(currentStockItem.c)
                viewModel.deleteStockByStockNoAndListId(currentStockItem.c)
                viewModel.deleteAllHistory(currentStockItem.c)
                Snackbar.make(view, "追蹤股票代號已刪除",Snackbar.LENGTH_LONG).show()

                viewModel.getOneFollowingListWithStocks()
                stockAdapter.notifyItemChanged(viewHolder.layoutPosition)
//                stockAdapter.notifyDataSetChanged()
            }


            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                // draw background and icon when swipe
                swipeBackground = ColorDrawable(resources.getColor(R.color.red, null))
                deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.delete_icon)!!

                val itemView = viewHolder.itemView
                val iconMargin = (itemView.height - deleteIcon.intrinsicHeight) / 2


                if (dX > 0) {
                    // swipe right
                    swipeBackground.setBounds(itemView.left, itemView.top, dX.toInt(), itemView.bottom)
                    deleteIcon.setBounds(itemView.left + iconMargin, itemView.top + iconMargin, itemView.left + iconMargin + deleteIcon.intrinsicWidth, itemView.bottom - iconMargin)

                } else {
                    swipeBackground.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                    deleteIcon.setBounds(itemView.right - iconMargin - deleteIcon.intrinsicWidth, itemView.top + iconMargin, itemView.right - iconMargin, itemView.bottom - iconMargin)

                }
                swipeBackground.draw(c)

                c.save()
                if (dX > 0 ) {
                    c.clipRect(itemView.left, itemView.top, dX.toInt(), itemView.bottom)
                } else {
                    c.clipRect(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)

                }
                deleteIcon.draw(c)
                c.restore()
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }

        }).attachToRecyclerView(recyclerView)

        binding.swipeRefresh.setOnRefreshListener {

            viewModel.cancelRepeatFetchPriceJob()
            viewModel.getStockNoListAndToQueryStockPriceInfo()
        }


    }


    private val getStockNameToGetRelatedNews:(stockContent: MsgArray)->Unit = { stockContent->

        val stockName = stockContent.n
        viewModel.getRelatedNews(stockName)
        findNavController().navigate(ListFragmentDirections.actionListFragmentToNewsFragment())
    }

    private val toCandelStickChartFragment:(stockContent: MsgArray) -> Unit = {

        val stockNo = it.c
        val stockPrice:String = if(it.z != "-") it.z else it.y
        val stockName = it.n

        findNavController().navigate(ListFragmentDirections.actionListFragmentToCandleStickChartFragment(stockNo,stockName,stockPrice))
    }
     private fun updateWidget(listOfWidgetStockData: List<WidgetStockData>) {
         // Send broadcast to widgetProvider to invoke onRecieve method
         val updateIntent = Intent(context, StockAppWidgetProvider::class.java)
         updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
         context?.sendBroadcast(updateIntent)


         // Write stock price info to shared preference
         val sharedPref = activity?.getSharedPreferences("sharedPref", Context.MODE_PRIVATE) ?: return
         val moshi = Moshi.Builder().build()
         val jsonAdapter: JsonAdapter<List<WidgetStockData>> = moshi.adapter(Types.newParameterizedType(List::class.java, WidgetStockData::class.java))
         val jsonString = jsonAdapter.toJson(listOfWidgetStockData)
         with (sharedPref.edit()) {
             putString("sharedPref1", jsonString)
             apply()
         }
     }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.options_menu, menu)
        val searchView = menu.findItem(R.id.app_bar_search).actionView as SearchView
        searchView.apply {
            queryHint = "請輸入代號"
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(p0: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(text: String?): Boolean {
                    Log.d("query text", text.toString())
                    stockAdapter.filter.filter(text)
                    return true
                }

            })

        }


    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onstop")
        viewModel.cancelRepeatFetchPriceJob()
        (activity as MainActivity).hideMenuSelectorBtn()
    }
}