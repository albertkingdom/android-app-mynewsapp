package com.example.mynewsapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat.getColor

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mynewsapp.MsgArray
import com.example.mynewsapp.R
import com.example.mynewsapp.databinding.ItemStockinfoBinding


class StockInfoAdapter:ListAdapter<MsgArray, StockInfoAdapter.StockViewHolder>(DiffCallback) {
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
            stockPrice.text = String.format("%.2f",currentStock.z.toFloat())
            val diff = currentStock.z.toFloat() - currentStock.y.toFloat()
            stockPriceDiff.text = String.format("%.2f", diff)
            if(diff<0f){
                stockPriceDiff.setBackgroundResource(R.color.teal_700)
            }
        }

    }

}