package com.example.stockinsight.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.example.stockinsight.R
import com.example.stockinsight.data.model.stock.StockInfo
import com.example.stockinsight.databinding.ItemHomePageStockBinding
import com.example.stockinsight.utils.drawLineChart
import com.github.mikephil.charting.data.Entry

class MultiQuoteForHomePageAdapter(
    private val multiSimpleQuoteForHomePageList: ArrayList<StockInfo>,
) : RecyclerView.Adapter<MultiQuoteForHomePageAdapter.MultiQuoteForHomePageViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): MultiQuoteForHomePageViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_page_stock, parent, false)
        return MultiQuoteForHomePageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MultiQuoteForHomePageViewHolder, position: Int) {
        val multiQuoteForHomePage = multiSimpleQuoteForHomePageList[position]
        holder.bind(multiQuoteForHomePage)
    }

    override fun getItemCount(): Int {
        return multiSimpleQuoteForHomePageList.size
    }

    inner class MultiQuoteForHomePageViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val itemHomePageStockBinding: ItemHomePageStockBinding =
            ItemHomePageStockBinding.bind(itemView)

        fun bind(multiQuoteForHomePage: StockInfo) {
            itemHomePageStockBinding.txtTWTR.text = multiQuoteForHomePage.quoteInfo.symbol
            itemHomePageStockBinding.txtTwitterInc.text =
                multiQuoteForHomePage.quoteInfo.companyName
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
            // bound stockHomePage.today to 2 decimal places
            itemHomePageStockBinding.txtPrice.text =
                String.format("%.2f", multiQuoteForHomePage.quoteInfo.today)
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
                drawLineChart(itemHomePageStockBinding.imageChart, entries, "up")
            } else {
                // if the stock price is down, draw the line chart in red
                drawLineChart(itemHomePageStockBinding.imageChart, entries, "down")
            }
        }
    }
}