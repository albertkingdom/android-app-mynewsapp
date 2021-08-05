package com.example.mynewsapp.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.makeText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.mynewsapp.MainActivity
import com.example.mynewsapp.adapter.StockInfoAdapter
import com.example.mynewsapp.databinding.FragmentListBinding
import com.example.mynewsapp.db.Stock
import com.example.mynewsapp.model.MsgArray
import com.example.mynewsapp.util.Resource
import com.google.android.material.snackbar.Snackbar
import java.util.*

class ListFragment : Fragment() {
    private lateinit var binding: FragmentListBinding
    private lateinit var viewModel: NewsViewModel

    private lateinit var stockAdapter: StockInfoAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = binding.stockListRecyclerview
        //change toolbar title
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "List"
        viewModel =(activity as MainActivity).viewModel

        stockAdapter = StockInfoAdapter(getStockNameToGetRelatedNews)
        recyclerView.adapter = stockAdapter

        viewModel.stockPriceInfo.observe(viewLifecycleOwner, Observer { response ->
            //Log.d("list fragment", "observe stockpriceinfo")

            when (response) {
                is Resource.Success -> {
                    hideProgressbar()
                    response.data?.let { stockInfoResponse ->
                        stockAdapter.submitList(stockInfoResponse.msgArray)
                    }
                }
                is Resource.Error -> {
                    response.message?.let { message ->
                        Log.e("stock list fragment", "An error occured: $message")
                        Snackbar.make(view, "An error occured: $message", Snackbar.LENGTH_LONG).show()
                    }
                    hideProgressbar()
                }
                is Resource.Loading -> {
                    showProgressbar()
                }
            }

        })

        viewModel.allStocksFromdb.observe(viewLifecycleOwner, {
            //Log.d("list fragment", "observe allstocksfromdb")
            /**
             * 1. observe the allstocks
             * 2. map to get List<StockNo>
             * 3. call getStockPriceInfo() with List<stockNo>
             */
            val stockList:List<String> = it.map { stock ->
                stock.stockNo
            }.toSet().toList()
            viewModel.getStockNoListAndToQueryStockPriceInfo(stockList)

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

                viewModel.deleteStock(currentStockItem.c)
                Snackbar.make(view, "追蹤股票代號已刪除",Snackbar.LENGTH_LONG).show()
            }

        }).attachToRecyclerView(recyclerView)
    }
    private fun hideProgressbar(){
        binding.paginationProgressBar.visibility = View.INVISIBLE
    }
    private fun showProgressbar(){
        binding.paginationProgressBar.visibility = View.VISIBLE
    }

    private val getStockNameToGetRelatedNews:(stockContent: MsgArray)->Unit = { stockContent->

        val stockName = stockContent.n
        viewModel.getRelatedNews(stockName)
        findNavController().navigate(ListFragmentDirections.actionListFragmentToNewsFragment())
    }
}