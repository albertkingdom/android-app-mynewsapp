package com.example.mynewsapp.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.example.mynewsapp.MainActivity
import com.example.mynewsapp.R
import com.example.mynewsapp.adapter.StatisticAdapter
import com.example.mynewsapp.databinding.FragmentStatisticBinding
import com.example.mynewsapp.model.StockStatistic
import com.example.mynewsapp.util.Resource
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.snackbar.Snackbar
import kotlin.random.Random

class StatisticFragment: Fragment() {
    val TAG = "StatisticFragment"
    lateinit var binding: FragmentStatisticBinding
    lateinit var viewModel: NewsViewModel
    lateinit var pieChart: PieChart
    lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StatisticAdapter
    companion object {
        fun createPieChart(dataSource: List<StockStatistic>, targetChart: PieChart) {
            val TAG = "createPieChart"
            // entries -> pieDataset -> pieData -> pieChart
            val colors = mutableListOf<Int>()
            var entries: List<PieEntry>

            val pieDataSet: PieDataSet
            val pieData: PieData

            Log.d(TAG, "customMap...${dataSource.toString()}")


            dataSource.map { entry->
                val randomColor = Color.argb(255, Random.nextInt(0, 255), Random.nextInt(0, 255), Random.nextInt(0, 255))
                colors.add(randomColor)
            }

            val sumOfAsset: Float =
                dataSource.map { stockStatistic -> stockStatistic.totalAssets }.reduce { acc, item -> acc + item }
            //Log.d(TAG, "sumOfAsset...$sumOfAsset")
            entries = dataSource.map{ entry -> PieEntry(entry.totalAssets / sumOfAsset * 100, entry.stockNo) }

            //Log.d(TAG, "entries..$entries")


            pieDataSet = PieDataSet(entries, "stockNo.")
            pieDataSet.colors = colors

            pieData = PieData(pieDataSet)
            pieData.setDrawValues(true)


            pieData.setValueFormatter(PercentFormatter(targetChart)) // display % on chart
            pieData.setValueTextSize(12f)
            targetChart.data = pieData
            targetChart.setUsePercentValues(true) // display % on chart
            targetChart.invalidate() //refresh

            val description = Description() // @ bottom right of chart
            description.text = "" // remove description
            targetChart.description = description
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel =(activity as MainActivity).viewModel
        binding = FragmentStatisticBinding.inflate(inflater)
        pieChart = binding.pieChart

        viewModel.allInvestHistoryList.observe(viewLifecycleOwner, {
            Log.d(TAG, "all history...$it")
            viewModel.calculateForPieChart()
        })
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        viewModel.investStatistics.observe(viewLifecycleOwner, { customMap ->
//
//           StatisticFragment.createPieChart(customMap, pieChart)
//        })
        recyclerView = binding.recyclerView
        adapter = StatisticAdapter()
        recyclerView.adapter = adapter

        viewModel.investStatisticsList.observe(viewLifecycleOwner, { customMap ->

            StatisticFragment.createPieChart(customMap, pieChart)
            adapter.submitList(customMap)
        })





    }
}