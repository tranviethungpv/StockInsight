package com.example.stockinsight.ui.fragment.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.stockinsight.R
import com.example.stockinsight.data.model.StockHomePage
import com.example.stockinsight.databinding.ItemHomePageStockBinding

class StockHomePageAdapter(
    private val stockHomePageList: ArrayList<StockHomePage>
): RecyclerView.Adapter<StockHomePageAdapter.StockHomePageViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockHomePageViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_home_page_stock, parent, false)
        return StockHomePageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: StockHomePageViewHolder, position: Int) {
        val stockHomePage = stockHomePageList[position]
        holder.bind(stockHomePage)
    }

    override fun getItemCount(): Int {
        return stockHomePageList.size
    }

    inner class StockHomePageViewHolder(private val itemView: View): RecyclerView.ViewHolder(itemView) {
        private val itemHomePageStockBinding: ItemHomePageStockBinding = ItemHomePageStockBinding.bind(itemView)
        fun bind(stockHomePage: StockHomePage) {
            itemHomePageStockBinding.txtTWTR.text = stockHomePage.symbol
            itemHomePageStockBinding.txtTwitterInc.text = stockHomePage.companyName
            if (stockHomePage.diff > 0) {
                itemHomePageStockBinding.txtTwentyThreeOne.setTextColor(itemView.context.resources.getColor(R.color.light_green_500))
                itemHomePageStockBinding.imageChart.setImageResource(R.drawable.img_chart_light_green_500_32x80)
                itemHomePageStockBinding.imageSettings.setImageResource(R.drawable.img_arrowup_light_green_500)
            } else {
                itemHomePageStockBinding.txtTwentyThreeOne.setTextColor(itemView.context.resources.getColor(R.color.red_700))
                itemHomePageStockBinding.imageChart.setImageResource(R.drawable.img_chart_deep_orange_a200)
                itemHomePageStockBinding.imageSettings.setImageResource(R.drawable.img_settings_deep_orange_a200)
            }
            // bound stockHomePage.today to 2 decimal places
            itemHomePageStockBinding.txtPrice.text = String.format("%.2f", stockHomePage.today)
            // bound stockHomePage.diff to 2 decimal places
            itemHomePageStockBinding.txtTwentyThreeOne.text = String.format("%.2f", stockHomePage.diff)
        }
    }
}