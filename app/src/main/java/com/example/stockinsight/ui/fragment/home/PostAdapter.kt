package com.example.stockinsight.ui.fragment.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.stockinsight.R
import com.example.stockinsight.data.model.Post
import com.example.stockinsight.databinding.ItemHotStockBinding

class PostAdapter(
    private val postList: ArrayList<Post>
): RecyclerView.Adapter<PostAdapter.PostViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_hot_stock, parent, false)
        return PostViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        holder.bind(post)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    inner class PostViewHolder(private val itemView: View): RecyclerView.ViewHolder(itemView) {
        private val itemHotStockBinding: ItemHotStockBinding = ItemHotStockBinding.bind(itemView)
        fun bind(post: Post) {
            itemHotStockBinding.txtFB.text = post.id.toString()
            itemHotStockBinding.txtFacebookInc.text = post.title
        }
    }
}