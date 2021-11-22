package com.example.mynewsapp.adapter


import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mynewsapp.R
import com.example.mynewsapp.db.InvestHistory
import com.example.mynewsapp.model.StockHistory
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


class StockHistoryAdapter: ListAdapter<InvestHistory, StockHistoryAdapter.StockHistoryViewHolder>(DiffCallback) {
    private var stockPrice: String? = null

    class StockHistoryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val statusView = itemView.findViewById<TextView>(R.id.status)
        val dateView = itemView.findViewById<TextView>(R.id.date)
        val priceView = itemView.findViewById<TextView>(R.id.price)
        val amountView = itemView.findViewById<TextView>(R.id.amount)
        val revenueView = itemView.findViewById<TextView>(R.id.revenue)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StockHistoryViewHolder {
       val view = LayoutInflater.from(parent.context).inflate(R.layout.item_stock_history, parent,false)
        return StockHistoryViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: StockHistoryViewHolder,
        position: Int
    ) {
        val currentHistory = getItem(position)

        holder.apply {
            val revenue = calcRevenue(price = currentHistory.price)
            statusView.text = if (currentHistory.status == 0) "買" else "賣"
            dateView.text = formatDateLong(currentHistory.date)
            priceView.text = currentHistory.price.toString()
            amountView.text = currentHistory.amount.toString()
            revenueView.text = "${revenue}%"

            if (revenue.toFloat() > 0) {
                revenueView.setTextColor(Color.RED)
            }else{
                revenueView.setTextColor(Color.GREEN)
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<InvestHistory>() {
            override fun areItemsTheSame(oldItem: InvestHistory, newItem: InvestHistory): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: InvestHistory, newItem: InvestHistory): Boolean {
                return oldItem == newItem
            }

        }
    }

    private fun formatDate():String {
        val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
        return LocalDate.now().format(formatter)

    }
    private fun formatDateLong(dateLong: Long):String {

        val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
        return Instant.ofEpochMilli(dateLong).atZone(ZoneId.systemDefault()).format(formatter)

    }
    fun setStockPrice(price: String) {
        stockPrice = price
    }
    fun calcRevenue(price: Double): String {
        var revenueStr: String = "-"
        val stockPriceDouble = stockPrice?.toDoubleOrNull()
        if (stockPrice != "-" && stockPriceDouble != null) {

            val result = (stockPriceDouble - price) / price * 100
            revenueStr = String.format("%.2f", result)

        }
        return revenueStr
    }
}