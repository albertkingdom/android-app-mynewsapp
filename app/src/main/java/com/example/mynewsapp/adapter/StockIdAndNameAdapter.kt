package com.example.mynewsapp.adapter


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mynewsapp.R
import com.example.mynewsapp.databinding.ItemStockIdNameBinding
import com.example.mynewsapp.model.StockIdNameStar



class StockIdAndNameAdapter: ListAdapter<StockIdNameStar,StockIdAndNameAdapter.StockIdAndNameViewHolder>(
    DiffCallback) {
    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<StockIdNameStar>(){
            override fun areItemsTheSame(oldItem: StockIdNameStar, newItem: StockIdNameStar): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: StockIdNameStar, newItem: StockIdNameStar): Boolean {
                return oldItem.stockName == newItem.stockName && oldItem.isFollowing == newItem.isFollowing
            }

        }
        val TAG = "StockIdAndNameAdapter"
    }
    lateinit var onClickStarListener: ClickOnStarListener

    class StockIdAndNameViewHolder(view: View): RecyclerView.ViewHolder(view) {

    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StockIdAndNameAdapter.StockIdAndNameViewHolder {
        return StockIdAndNameViewHolder(
            ItemStockIdNameBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            ).root
        )
    }

    override fun onBindViewHolder(
        holder: StockIdAndNameAdapter.StockIdAndNameViewHolder,
        position: Int
    ) {
        val currentItem = getItem(position)
        ItemStockIdNameBinding.bind(holder.itemView).apply {
            stockIdName.text = currentItem.stockName
            // change star icon if stockId is already in following list
            if (currentItem.isFollowing) {
                Log.d(TAG, "is following current item= $currentItem")
                starIcon.setImageDrawable(
                   ResourcesCompat.getDrawable(
                       this.starIcon.resources,
                        R.drawable.ic_baseline_star,
                        null
                    )
                )
            }
            starIcon.setOnClickListener {
                val stockId = currentItem.stockName.split(" ").first()
                onClickStarListener.singleClick(stockId)
                starIcon.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        this.starIcon.resources,
                        R.drawable.ic_baseline_star,
                        null
                    )
                )
            }
        }
    }

    override fun onViewRecycled(holder: StockIdAndNameViewHolder) {
        super.onViewRecycled(holder)
        ItemStockIdNameBinding.bind(holder.itemView).apply {
            starIcon.setImageDrawable(
                ResourcesCompat.getDrawable(
                    this.starIcon.resources,
                    R.drawable.ic_baseline_star_border,
                    null
                )
            )
        }

    }



}
interface ClickOnStarListener {
    fun singleClick(stockId: String)
}