package com.example.mynewsapp.ui

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mynewsapp.MainActivity
import com.example.mynewsapp.R
import com.example.mynewsapp.adapter.StockHistoryAdapter
import com.example.mynewsapp.databinding.FragmentCandleStickChartBinding
import com.example.mynewsapp.model.CandleStickData
import com.example.mynewsapp.util.Resource
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.LargeValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.EntryXComparator
import com.google.android.material.snackbar.Snackbar
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

class CandleStickChartFragment: Fragment() {

    lateinit var binding: FragmentCandleStickChartBinding
    lateinit var viewModel: NewsViewModel
    private val chatViewModel: ChatViewModel by activityViewModels()
    private val args: CandleStickChartFragmentArgs by navArgs()

    private lateinit var chart:CombinedChart
    private lateinit var xLabels: List<String> //x axis label list
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
        (requireActivity() as AppCompatActivity).supportActionBar?.title = args.stockNo

        viewModel =(activity as MainActivity).viewModel
        viewModel.getCandleStickData("", args.stockNo)
        viewModel.queryHistoryByStockNo(args.stockNo)
        Log.d("candle fragment","on create view ${args.stockNo}")
        chatViewModel.checkIsExistingChannel(args.stockNo)
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
        historyAdapter.setListener { targetDate ->
            // when clicked record, highlight the day of investing record on chart

            xLabels.forEachIndexed { index, dateString ->
                //Log.d("CandleStickChartFragment", dateString)
                if(dateString == targetDate){
                    try {
                        val highlight = Highlight(index.toFloat(), 0, -1)
                        highlight.dataIndex = 1
                        chart.highlightValue(highlight, false)
                    } catch (e: Exception) {
                        Log.e("touch history", e.toString())
                    }

                }
            }


        }
        recyclerView.adapter = historyAdapter

        viewModel.candleStickData.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    response.data?.let { stockInfoResponse ->

                        generateXLabels(stockInfoResponse.data)
                        initOpenCloseHighLowValue(stockInfoResponse.data.last()[7])
                        val candleData = generateCandleData(stockInfoResponse.data)
                        val barData = generateBarData(stockInfoResponse.data)

                        val combinedData = CombinedData()
                        combinedData.setData(candleData)
                        combinedData.setData(barData)
                        chart.data = combinedData


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

        binding.hideShowButton.setOnClickListener {
            binding.chartContainer.updateLayoutParams {
                 if(this.height == ViewGroup.LayoutParams.WRAP_CONTENT)   {
                    this.height = 0
                     binding.hideShowButton.setImageResource(R.drawable.ic_arrow_drop_down)
                } else {
                    this.height = ViewGroup.LayoutParams.WRAP_CONTENT
                     binding.hideShowButton.setImageResource(R.drawable.ic_arrow_drop_up)
                 }
            }
        }
    }
    private fun generateXLabels(data: List<List<String>>) {
        xLabels = data.map { day->
            day[0]
        }
    }
    private fun generateCandleData(data: List<List<String>>): CandleData {
        val candleStickEntry = data.mapIndexed { index, day ->

                CandleEntry(
                    index.toFloat(),
                    day[4].toFloat(),//high
                    day[5].toFloat(), //low
                    day[3].toFloat(), //open
                    day[6].toFloat() //close
                )

        }
        val candleDataSet = CandleDataSet(candleStickEntry, args.stockNo)
        candleDataSet.apply {
            //shadowColor = getColor(requireContext(),R.color.black)
            decreasingColor = getColor(requireContext(),R.color.green)
            increasingColor = getColor(requireContext(),R.color.red)
            decreasingPaintStyle = Paint.Style.FILL
            increasingPaintStyle = Paint.Style.FILL
            setDrawValues(false)
            shadowColorSameAsCandle = true
            axisDependency = YAxis.AxisDependency.LEFT
        }
        val candleData = CandleData(candleDataSet)
        return candleData
    }
    private fun generateBarData(data: List<List<String>>): BarData {
        val barEntries = data.mapIndexed { index, day ->
            BarEntry(index.toFloat(), NumberFormat.getInstance().parse(day[2]).toFloat())
        }
        val barDataSet = BarDataSet(barEntries, "volume")
        barDataSet.apply {
            axisDependency = YAxis.AxisDependency.RIGHT
            setDrawValues(false)
            color = Color.LTGRAY

        }
        val barData = BarData(barDataSet)
        return barData
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


    }

    private fun initChartFormat(){
        chart.extraBottomOffset = 50F
        chart.apply{
            //左下方Legend
            legend.isEnabled = false
            //右下方description label
            description.isEnabled = false
            setScaleEnabled(false)
            axisRight.isEnabled = true
            axisRight.setDrawGridLines(false)
            axisRight.valueFormatter = LargeValueFormatter()
            axisRight.axisMaximum = barData.yMax * 10
            drawOrder = arrayOf(CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.CANDLE)
            barData.isHighlightEnabled = false
            candleData.isHighlightEnabled = true
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
    private fun navigateToChatFragment() {

        chatViewModel.stockNo = args.stockNo
        findNavController().navigate(CandleStickChartFragmentDirections.actionCandleStickChartFragmentToChatFragment())
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
            R.id.go_to_chatRoom -> {
                navigateToChatFragment()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        viewModel.clearCandleStickData()
        super.onDestroyView()
        //Log.d("candle fragment","on destroy view")
    }
}