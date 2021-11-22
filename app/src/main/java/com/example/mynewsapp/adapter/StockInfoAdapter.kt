package com.example.mynewsapp.adapter




import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mynewsapp.model.MsgArray
import com.example.mynewsapp.R
import com.example.mynewsapp.databinding.ItemStockinfoBinding


class StockInfoAdapter(val onClick: (Stock: MsgArray)->Unit, val toCandleStickChart: (Stock: MsgArray)->Unit):ListAdapter<MsgArray, StockInfoAdapter.StockViewHolder>(DiffCallback), Filterable {
    private var list = mutableListOf<MsgArray>()

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<MsgArray>(){
            override fun areItemsTheSame(oldItem: MsgArray, newItem: MsgArray): Boolean {
                return oldItem.c == newItem.c
            }

            override fun areContentsTheSame(oldItem: MsgArray, newItem: MsgArray): Boolean {
                return oldItem == newItem
            }

        }
    }

    fun setData(list: MutableList<MsgArray>?){
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
            stockNo.text = currentStock.c
            stockName.text = currentStock.n
            stockPrice.text = String.format("%.2f",handleStockPrice(currentStock).toFloat())
            val diff = handleStockPrice(currentStock).toFloat() - currentStock.y.toFloat()


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
                onClick(currentStock)
            }

            showCandleStickChart.setOnClickListener {
                toCandleStickChart(currentStock)
            }
        }


    }
    private fun handleStockPrice(currentStock: MsgArray):String{
        return if(currentStock.z == "-"){
            currentStock.y
        }else{
            currentStock.z
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
                    if (item.c.contains(constraint)) {
                        filteredList.add(item)
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results

        }

        override fun publishResults(p0: CharSequence?, filterResults: FilterResults?) {

            submitList(filterResults?.values as MutableList<MsgArray>)
        }

    }
}