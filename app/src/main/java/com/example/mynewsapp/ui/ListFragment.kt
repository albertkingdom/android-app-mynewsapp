package com.example.mynewsapp.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.mynewsapp.MainActivity
import com.example.mynewsapp.model.MsgArray
import com.example.mynewsapp.adapter.StockInfoAdapter
import com.example.mynewsapp.databinding.FragmentListBinding
import com.example.mynewsapp.util.Resource

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
                    }
                    showProgressbar()
                }
                is Resource.Loading -> {
                    showProgressbar()
                }
            }

        })

        viewModel.allStocksFromdb.observe(viewLifecycleOwner, {
            //Log.d("list fragment", it.toString())
            /**
             * 1. observe the allstocks
             * 2. map to get List<StockNo>
             * 3. call getStockPriceInfo() with List<stockNo>
             */
            val stockList:List<String> = it.map { stock ->
                stock.stockNo
            }.toSet().toList()


            viewModel.getStockPriceInfo(stockList)
        })

        binding.floatingBtn.setOnClickListener {
            val dialog = CustomDialogFragment()

            dialog.show(parentFragmentManager,"stock")
        }
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