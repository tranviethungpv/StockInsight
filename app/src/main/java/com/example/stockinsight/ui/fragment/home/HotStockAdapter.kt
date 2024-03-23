package com.example.stockinsight.ui.fragment.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.stockinsight.R
import com.example.stockinsight.data.model.Stock

class HotStockAdapter(
    private val hotStockList: List<Stock>
): RecyclerView.Adapter<HotStockAdapter.HotStockViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotStockViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_hot_stock, parent, false)
        return HotStockViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HotStockViewHolder, position: Int) {
        val hotStock = hotStockList[position]
        holder.bind(hotStock)
    }

    override fun getItemCount(): Int {
        return hotStockList.size
    }

    inner class HotStockViewHolder(private val itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(hotStock: Stock) {
//            binding.txtStockName.text = hotStock.stockName
//            binding.txtStockPrice.text = hotStock.stockPrice
//            binding.txtStockChange.text = hotStock.stockChange
//            binding.txtStockChangePercentage.text = hotStock.stockChangePercentage
        }
    }
}