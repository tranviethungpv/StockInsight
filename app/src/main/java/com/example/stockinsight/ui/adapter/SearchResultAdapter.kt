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

class SearchResultAdapter(
    private val onItemClick: (FullStockInfo) -> Unit
) : ListAdapter<FullStockInfo, SearchResultAdapter.SearchResultViewHolder>(
    FullStockInfoDiffCallback()
) {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): SearchResultViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_page_stock, parent, false)
        return SearchResultViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        val multiQuoteForHomePage = getItem(position)
        holder.bind(multiQuoteForHomePage)
        holder.itemView.setOnClickListener {
            onItemClick(multiQuoteForHomePage)
        }
    }

    inner class SearchResultViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val itemHomePageStockBinding: ItemHomePageStockBinding =
            ItemHomePageStockBinding.bind(itemView)

        @Suppress("DEPRECATION")
        @SuppressLint("DefaultLocale")
        fun bind(multiQuoteForHomePage: FullStockInfo) {
            itemHomePageStockBinding.txtTWTR.text = multiQuoteForHomePage.quoteInfo.symbol
            itemHomePageStockBinding.txtTwitterInc.text = multiQuoteForHomePage.quoteInfo.longName
            if (multiQuoteForHomePage.quoteInfo.diff > 0) {
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
            if (multiQuoteForHomePage.quoteInfo.currentPrice != 0.0) {
                itemHomePageStockBinding.txtPrice.text =
                    String.format("%.2f", multiQuoteForHomePage.quoteInfo.currentPrice)
            } else {
                itemHomePageStockBinding.txtPrice.text =
                    String.format("%.2f", multiQuoteForHomePage.quoteInfo.today)
            }

            // bound stockHomePage.diff to 2 decimal places
            itemHomePageStockBinding.txtTwentyThreeOne.text =
                String.format("%.2f", multiQuoteForHomePage.quoteInfo.diff)

            val chartData = multiQuoteForHomePage.historicData.close

            val entries = mutableListOf<Entry>()
            chartData.let { closeData ->
                for ((timestamp, value) in closeData) {
                    entries.add(Entry(timestamp.toFloat(), value.toFloat()))
                }
            }
            if (multiQuoteForHomePage.quoteInfo.diff > 0) {
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
        submitList(null)
        submitList(newList)
    }
}