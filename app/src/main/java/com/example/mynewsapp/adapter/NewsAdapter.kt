package com.example.mynewsapp.adapter

import Article
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mynewsapp.R

class NewsAdapter : ListAdapter<Article, NewsAdapter.ArticleViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Article>() {
            override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
                return oldItem.url == newItem.url
            }

            override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
                return oldItem == newItem
            }

        }
    }

    class ArticleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleView = view.findViewById<TextView>(R.id.tvTitle)
        val descriptionView = view.findViewById<TextView>(R.id.tvDescription)
        val sourceView = view.findViewById<TextView>(R.id.tvSource)
        val publishedAtView = view.findViewById<TextView>(R.id.tvPublishedAt)
        val articleImageView = view.findViewById<ImageView>(R.id.ivArticleImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_article, parent, false)
        return ArticleViewHolder(view)
    }

    private var onNewsClickListener: ((Article) -> Unit)? = null

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val currentArticle = getItem(position)

        holder.apply {
            titleView.text = currentArticle.title
            descriptionView.text = currentArticle.description
            sourceView.text = currentArticle.source.name
            publishedAtView.text = currentArticle.publishedAt
            Glide.with(this.itemView).load(currentArticle.urlToImage).into(articleImageView)

            itemView.setOnClickListener {

                onNewsClickListener?.let { it1 -> it1(currentArticle) }
            }

        }
    }


    fun setClickListener(listener: (Article) -> Unit) {
        onNewsClickListener = listener
    }

}