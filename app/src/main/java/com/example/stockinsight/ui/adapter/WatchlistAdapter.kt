package com.example.stockinsight.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.stockinsight.R
import com.example.stockinsight.data.model.stock.FullStockInfo
import com.example.stockinsight.databinding.ItemHomePageStockBinding
import com.example.stockinsight.utils.drawSimpleLineChart
import com.github.mikephil.charting.data.Entry

class WatchlistAdapter(
    private val onItemClick: (FullStockInfo) -> Unit,
    private val onItemLongClick: (FullStockInfo, View) -> Unit
) : ListAdapter<FullStockInfo, WatchlistAdapter.WatchlistAdapterViewHolder>(
    FullStockInfoDiffCallback()
) {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): WatchlistAdapterViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_page_stock, parent, false)
        return WatchlistAdapterViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: WatchlistAdapterViewHolder, position: Int) {
        val watchlistQuote = getItem(position)
        holder.bind(watchlistQuote)
        holder.itemView.setOnClickListener {
            onItemClick(watchlistQuote)
        }
        holder.itemView.setOnLongClickListener { view ->
            onItemLongClick(watchlistQuote, view)
            true
        }
    }

    inner class WatchlistAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemHomePageStockBinding: ItemHomePageStockBinding =
            ItemHomePageStockBinding.bind(itemView)

        @SuppressLint("DefaultLocale")
        fun bind(fullStockInfo: FullStockInfo) {
            itemHomePageStockBinding.txtTWTR.text = fullStockInfo.quoteInfo.symbol
            itemHomePageStockBinding.txtTwitterInc.text = fullStockInfo.quoteInfo.longName
            if (fullStockInfo.quoteInfo.diff > 0) {
                itemHomePageStockBinding.txtTwentyThreeOne.setTextColor(
                    itemView.context.resources.getColor(
                        R.color.light_green_500
                    )
                )
                itemHomePageStockBinding.imageSettings.setImageResource(R.drawable.img_arrowup_light_green_500)
            } else {
                itemHomePageStockBinding.txtTwentyThreeOne.setTextColor(
                    itemView.context.resources.getColor(
                        R.color.red_700
                    )
                )
                itemHomePageStockBinding.imageSettings.setImageResource(R.drawable.img_settings_deep_orange_a200)
            }
            if (fullStockInfo.quoteInfo.currentPrice != 0.0) {
                itemHomePageStockBinding.txtPrice.text =
                    String.format("%.2f", fullStockInfo.quoteInfo.currentPrice)
            } else {
                itemHomePageStockBinding.txtPrice.text =
                    String.format("%.2f", fullStockInfo.quoteInfo.today)
            }

            // bound stockHomePage.diff to 2 decimal places
            itemHomePageStockBinding.txtTwentyThreeOne.text =
                String.format("%.2f", fullStockInfo.quoteInfo.diff)

            val chartData = fullStockInfo.historicData.close

            val entries = mutableListOf<Entry>()
            chartData.let { closeData ->
                for ((timestamp, value) in closeData) {
                    entries.add(Entry(timestamp.toFloat(), value.toFloat()))
                }
            }
            if (fullStockInfo.quoteInfo.diff > 0) {
                drawSimpleLineChart(itemHomePageStockBinding.imageChart, entries, "up")
            } else {
                // if the stock price is down, draw the line chart in red
                drawSimpleLineChart(itemHomePageStockBinding.imageChart, entries, "down")
            }
        }
    }

    class FullStockInfoDiffCallback : DiffUtil.ItemCallback<FullStockInfo>() {
        override fun areItemsTheSame(oldItem: FullStockInfo, newItem: FullStockInfo): Boolean {
            return oldItem.quoteInfo.symbol == newItem.quoteInfo.symbol
        }

        override fun areContentsTheSame(oldItem: FullStockInfo, newItem: FullStockInfo): Boolean {
            return oldItem == newItem
        }
    }

    fun updateList(newList: List<FullStockInfo>) {
        submitList(newList)
    }
}
