package com.example.mynewsapp.adapter




import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mynewsapp.model.MsgArray
import com.example.mynewsapp.R
import com.example.mynewsapp.databinding.ItemStockinfoBinding


class StockInfoAdapter(val onClick: (Stock: MsgArray)->Unit, val toCandleStickChart: (Stock: MsgArray)->Unit):ListAdapter<MsgArray, StockInfoAdapter.StockViewHolder>(DiffCallback), Filterable {
    private var list = listOf<MsgArray>()

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<MsgArray>(){
            override fun areItemsTheSame(oldItem: MsgArray, newItem: MsgArray): Boolean {
                return oldItem.stockNo == newItem.stockNo
            }

            override fun areContentsTheSame(oldItem: MsgArray, newItem: MsgArray): Boolean {
                return oldItem == newItem
            }

        }
    }
    // set data for this adapter
    fun setData(list: List<MsgArray>?){
        this.list = list!!
        submitList(list)
    }
    class StockViewHolder(view: View):RecyclerView.ViewHolder(view){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
      return StockViewHolder(ItemStockinfoBinding.inflate(LayoutInflater.from(parent.context),parent,false).root)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        val currentStock = getItem(position)

        ItemStockinfoBinding.bind(holder.itemView).apply {
            stockNo.text = currentStock.stockNo
            stockName.text = currentStock.stockName
            stockPrice.text = String.format("%.2f",handleStockPrice(currentStock).toFloat())
            val diff = handleStockPrice(currentStock).toFloat() - currentStock.lastDayPrice.toFloat()


            if(diff<0f) {
                stockPriceDiff.setBackgroundResource(R.color.green)
                stockPriceDiff.text = holder.itemView.context.getString(
                    R.string.stockprice_diff,
                    "",
                    String.format("%.2f", diff)
                )
            }else if(diff>0f) {
                stockPriceDiff.setBackgroundResource(R.color.red)
                stockPriceDiff.text = holder.itemView.context.getString(
                    R.string.stockprice_diff,
                    "+",
                    String.format("%.2f", diff)
                )
            }else{
                stockPriceDiff.setBackgroundResource(R.color.white)
                stockPriceDiff.setTextColor(Color.GRAY)
                stockPriceDiff.text = holder.itemView.context.getString(
                    R.string.stockprice_diff,
                    "",
                    "0.00"
                )
            }

            root.setOnClickListener { it ->
//                onClick(currentStock)
                toCandleStickChart(currentStock)

            }

//            showCandleStickChart.setOnClickListener {
//                toCandleStickChart(currentStock)
//            }
        }


    }
    private fun handleStockPrice(currentStock: MsgArray):String{
        return if(currentStock.currentPrice == "-"){
            currentStock.lastDayPrice
        }else{
            currentStock.currentPrice
        }
    }
// to filter with searchview in list fragment
    override fun getFilter(): Filter {
       return customFilter
    }

    private val customFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList = ArrayList<MsgArray>()
            if (constraint == null || constraint.isEmpty()) {
                filteredList.addAll(list)

            } else {
                for (item in list) {
                    if (item.stockNo.contains(constraint)) {
                        filteredList.add(item)
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results

        }

        override fun publishResults(p0: CharSequence?, filterResults: FilterResults?) {
            println("publishResults ${filterResults?.values}")
            submitList(filterResults?.values as MutableList<MsgArray>)
        }

    }
}