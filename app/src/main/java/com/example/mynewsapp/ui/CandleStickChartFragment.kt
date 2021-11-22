package com.example.mynewsapp.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mynewsapp.MainActivity
import com.example.mynewsapp.R
import com.example.mynewsapp.adapter.StockHistoryAdapter
import com.example.mynewsapp.databinding.FragmentCandleStickChartBinding
import com.example.mynewsapp.db.InvestHistory
//import com.example.mynewsapp.db.InvestHistory
import com.example.mynewsapp.model.CandleStickData
import com.example.mynewsapp.model.StockHistory
import com.example.mynewsapp.util.Resource
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.EntryXComparator
import com.google.android.material.snackbar.Snackbar
import java.util.*
import kotlin.collections.ArrayList

class CandleStickChartFragment: Fragment() {

    lateinit var binding: FragmentCandleStickChartBinding
    lateinit var viewModel: NewsViewModel
    private val args: CandleStickChartFragmentArgs by navArgs()

    lateinit var chart:CandleStickChart
    val xLabels = ArrayList<String>() //x axis label list
    val candelStickEntry = ArrayList<CandleEntry>() //y values
    lateinit var candleDataSet:CandleDataSet //y values group: values + label
    private lateinit var historyAdapter: StockHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Log.d("candle fragment","onCreate")
        setHasOptionsMenu(true)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCandleStickChartBinding.inflate(inflater)
        chart = binding.candleStickChart
        //change toolbar title
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "${args.stockNo} k線圖"

        viewModel =(activity as MainActivity).viewModel
        viewModel.getCandleStickData("", args.stockNo)
        viewModel.queryHistoryByStockNo(args.stockNo)
        Log.d("candle fragment","on create view ${args.stockNo}")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("candle fragment","onViewCreated")

        binding.stockNo.text = args.stockNo
        binding.stockName.text = args.stockName
        binding.stockPrice.text = String.format("%.2f",args.stockPrice.toFloat())

        val recyclerView = binding.investHistory

        historyAdapter = StockHistoryAdapter()
        historyAdapter.setStockPrice(args.stockPrice)
        recyclerView.adapter = historyAdapter

        viewModel.candleStickData.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {

                    response.data?.let { stockInfoResponse ->
                        for ((index,day) in stockInfoResponse.data.withIndex()){

                            xLabels.add(day[0])
                            candelStickEntry.add(
                                CandleEntry(
                                    index.toFloat(),
                                    day[4].toFloat(),//high
                                    day[5].toFloat(), //low
                                    day[3].toFloat(), //open
                                    day[6].toFloat() //close
                                )
                            )
                            initOpenCloseHighLowValue(stockInfoResponse.data.last()[7])
                        }
                        Collections.sort(candelStickEntry, EntryXComparator())
                        candleDataSet = CandleDataSet(candelStickEntry, args.stockNo)

                        initCandleDataFormat()
                        chart.data = CandleData(candleDataSet)
                        initXFormat()
                        initChartFormat()

                        setupClickChart(stockInfoResponse)
                        chart.invalidate()

                    }
                }
                is Resource.Error -> {
                    response.message?.let { message ->
                        Log.e("stock list fragment", "An error occured: $message")
                        Snackbar.make(view, "An error occured: $message", Snackbar.LENGTH_LONG).show()
                    }

                }
                is Resource.Loading -> {

                }
            }
        })

        viewModel.investHistoryList.observe(viewLifecycleOwner, {

            historyAdapter.submitList(it)
        })

    }

    /**
     * format x label data
     */
    private fun initXFormat(){
        chart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(xLabels)
            position = XAxis.XAxisPosition.BOTTOM
            labelRotationAngle= -25f
            setDrawGridLines(false)
        }
        chart.setDrawBorders(true)
        chart.axisRight.isEnabled = false


    }

    /**
     * format data set: color, style of each candle
     */
    private fun initCandleDataFormat(){
        candleDataSet.apply {
            shadowColor = getColor(requireContext(),R.color.black)
            decreasingColor = getColor(requireContext(),R.color.green)
            increasingColor = getColor(requireContext(),R.color.red)
            decreasingPaintStyle = Paint.Style.FILL
            increasingPaintStyle = Paint.Style.FILL
            setDrawValues(false)
        }

    }
    private fun initChartFormat(){
        chart.extraBottomOffset = 50F
        chart.apply{
            //左下方Legend
            legend.isEnabled = false
            //右下方description label
            description.isEnabled = false
            setScaleEnabled(false)

        }
    }
    /**
     * click chart to show detail
     */
    private fun setupClickChart(alldatas: CandleStickData){
        chart.setOnChartValueSelectedListener(object:OnChartValueSelectedListener{
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                //e : x:index,y: y value clicked

                val index = e!!.x.toInt()
                val openValue = alldatas.data[index][3]
                val closeValue = alldatas.data[index][6]
                val highValue = alldatas.data[index][4]
                val lowValue = alldatas.data[index][5]
                val selectedDate = alldatas.data[index][0]
                binding.apply {
                    open.text = getString(R.string.stockprice_open, openValue)
                    close.text = getString(R.string.stockprice_close, closeValue)
                    high.text = getString(R.string.stockprice_high, highValue)
                    low.text = getString(R.string.stockprice_low, lowValue)
                    date.text = getString(R.string.stockprice_date, selectedDate)
                }
            }

            override fun onNothingSelected() {

            }
        })
    }
    private fun initOpenCloseHighLowValue(stockpriceDiff:String){
        binding.apply {
            open.text = getString(R.string.stockprice_open, "")
            close.text = getString(R.string.stockprice_close, "")
            high.text = getString(R.string.stockprice_high, "")
            low.text = getString(R.string.stockprice_low, "")
            date.text = getString(R.string.stockprice_date, "")
            priceDiff.text = stockpriceDiff
            priceDiff.setTextColor(if (stockpriceDiff.toFloat() > 0f ) Color.RED else getColor(requireContext(),R.color.green))

        }
    }
    private fun navigateToAddHistoryFragment() {
        val stockNo = args.stockNo
        findNavController().navigate(CandleStickChartFragmentDirections.actionCandleStickChartFragmentToAddHistoryFragment(stockNo))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.candle_stick_chart_fragment_option_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.addButton -> {
                navigateToAddHistoryFragment()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //Log.d("candle fragment","on destroy view")
    }
}