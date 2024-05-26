package com.example.stockinsight.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.stockinsight.R
import com.example.stockinsight.databinding.FragmentStockDetailBinding
import com.example.stockinsight.ui.viewmodel.StockViewModel
import com.example.stockinsight.ui.viewmodel.UserViewModel
import com.example.stockinsight.utils.SessionManager
import com.example.stockinsight.utils.UiState
import com.example.stockinsight.utils.drawFullLineChart
import com.example.stockinsight.utils.formatNumber
import com.example.stockinsight.utils.showDialog
import com.github.mikephil.charting.data.Entry
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
@SuppressLint("DefaultLocale")
class StockDetailFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentStockDetailBinding
    private val args: StockDetailFragmentArgs by navArgs()
    private val stockViewModel: StockViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private lateinit var stockSymbol: String
    private var chartMode: String = "1d"
    private var isInWatchlist: Boolean = false

    @Inject
    lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stockSymbol = args.stockSymbol
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentStockDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        veil()
        binding.rgChartMode.check(R.id.ro_1d)
        stockViewModel.getStockInfoBySymbol(stockSymbol, "1m", "1d")
        userViewModel.checkSymbolInWatchlist(session.getUserId() ?: "", stockSymbol)

        observerStockInfo()
        observerIsSymbolInWatchlist()

        binding.ivClose.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.ivFavorite.setOnClickListener {
            session.getUserId()?.let { userId ->
                if (isInWatchlist) {
                    stockViewModel.removeStockFromWatchlist(userId, stockSymbol)
                } else {
                    stockViewModel.addStockToWatchlist(userId, stockSymbol)
                }
            }
        }

        stockViewModel.addStockResult.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    veil()
                }

                is UiState.Success -> {
                    showDialog("Add to watchlist successfully", "success", requireContext())
                    userViewModel.checkSymbolInWatchlist(session.getUserId() ?: "", stockSymbol)
                    unVeil()
                }

                is UiState.Failure -> {
                    showDialog(state.message, "error", requireContext())
                    unVeil()
                }
            }
        }

        stockViewModel.removeStockResult.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    veil()
                }

                is UiState.Success -> {
                    showDialog("Removed from watchlist successfully", "success", requireContext())
                    userViewModel.checkSymbolInWatchlist(session.getUserId() ?: "", stockSymbol)
                    unVeil()
                }

                is UiState.Failure -> {
                    showDialog(state.message, "error", requireContext())
                    unVeil()
                }
            }
        }

        setupRadioGroup()
    }

    private fun setupRadioGroup() {
        binding.rgChartMode.setOnCheckedChangeListener { _, checkedId ->
            chartMode = when (checkedId) {
                R.id.ro_1d -> "1d"
                R.id.ro_1w -> "5d"
                R.id.ro_1m -> "1mo"
                R.id.ro_3m -> "3mo"
                R.id.ro_6m -> "6mo"
                R.id.ro_ytd -> "ytd"
                R.id.ro_1y -> "1y"
                R.id.ro_2y -> "2y"
                R.id.ro_5y -> "5y"
                R.id.ro_10y -> "10y"
                R.id.ro_all -> "max"
                else -> "1d"
            }
            updateChartData(chartMode)
        }
    }

    private fun getIntervalForPeriod(period: String): String {
        return when (period) {
            "1d" -> "1m"
            "5d" -> "5m"
            "1mo" -> "1h"
            "3mo" -> "1d"
            "6mo" -> "1d"
            "ytd" -> "1d"
            "1y" -> "1d"
            "2y" -> "1d"
            "5y" -> "1d"
            "10y" -> "1d"
            "max" -> "1mo"
            else -> "1d"
        }
    }

    private fun updateChartData(period: String) {
        // Update the stock information based on the selected period
        veil()
        stockViewModel.getStockInfoBySymbol(stockSymbol, getIntervalForPeriod(period), period)
    }

    @Suppress("DEPRECATION")
    @SuppressLint("DefaultLocale")
    private fun observerStockInfo() {
        stockViewModel.stockInfo.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    veil()
                }

                is UiState.Success -> {
                    binding.tvSymbol.text = state.data.quoteInfo.symbol
                    binding.tvCompanyName.text = state.data.quoteInfo.shortName

                    binding.tvPrice.text =
                        formatValue(state.data.quoteInfo.currentPrice, state.data.quoteInfo.today)
                    binding.txtTwentyThreeOne.text = formatValue(state.data.quoteInfo.diff)

                    if (state.data.quoteInfo.diff > 0) {
                        binding.txtTwentyThreeOne.setTextColor(
                            requireContext().resources.getColor(R.color.light_green_500)
                        )
                        binding.imageSettings.setImageResource(R.drawable.img_arrowup_light_green_500)
                    } else {
                        binding.txtTwentyThreeOne.setTextColor(
                            requireContext().resources.getColor(R.color.red_700)
                        )
                        binding.imageSettings.setImageResource(R.drawable.img_settings_deep_orange_a200)
                    }

                    binding.tvOpenValue.text = formatValue(state.data.quoteInfo.open)
                    binding.tvHighValue.text = formatValue(state.data.quoteInfo.dayHigh)
                    binding.tvLowValue.text = formatValue(state.data.quoteInfo.dayLow)
                    binding.tvVolumeValue.text = formatNumber(state.data.quoteInfo.volume)
                    binding.tvRatioValue.text = formatValue(state.data.quoteInfo.trailingPE)
                    binding.tvMarketCapValue.text = formatNumber(state.data.quoteInfo.marketCap)
                    binding.tv52WeekHighValue.text =
                        formatValue(state.data.quoteInfo.fiftyTwoWeekHigh)
                    binding.tv52WeekLowValue.text =
                        formatValue(state.data.quoteInfo.fiftyTwoWeekLow)
                    binding.tvAverageVolumeValue.text =
                        formatNumber(state.data.quoteInfo.averageVolume)

                    val dividendYield =
                        if (state.data.quoteInfo.dividendYield != 0.0) state.data.quoteInfo.dividendYield else state.data.quoteInfo.yield
                    binding.tvYieldValue.text = formatPercentage(dividendYield)

                    val beta =
                        if (state.data.quoteInfo.beta != 0.0) state.data.quoteInfo.beta else state.data.quoteInfo.beta3Year
                    binding.tvBetaValue.text = formatValue(beta)
                    binding.tvEarningsPerShareValue.text =
                        formatValue(state.data.quoteInfo.trailingEps)

                    val chartData = state.data.historicData.close

                    val entries = mutableListOf<Entry>()
                    chartData.let { closeData ->
                        for ((timestamp, value) in closeData) {
                            entries.add(Entry(timestamp.toFloat(), value.toFloat()))
                        }
                    }
                    if (state.data.quoteInfo.diff > 0) {
                        drawFullLineChart(binding.imageChart, entries, "up", chartMode)
                    } else {
                        // if the stock price is down, draw the line chart in red
                        drawFullLineChart(binding.imageChart, entries, "down", chartMode)
                    }
                    unVeil()
                }

                is UiState.Failure -> {
                    showDialog(state.message, "error", requireContext())
                    unVeil()
                }
            }
        }
    }

    private fun observerIsSymbolInWatchlist() {
        userViewModel.isSymbolInWatchlist.observe(viewLifecycleOwner) { isInWatchlist ->
            this.isInWatchlist = isInWatchlist
            if (isInWatchlist) {
                binding.ivFavorite.setImageResource(R.drawable.ic_heart_flat)
            } else {
                binding.ivFavorite.setImageResource(R.drawable.ic_heart_thin)
            }
        }
    }

    private fun formatValue(value: Double?, fallback: Double? = null): String {
        return when {
            value != null && value != 0.0 -> String.format("%.2f", value)
            fallback != null && fallback != 0.0 -> String.format("%.2f", fallback)
            else -> "-"
        }
    }

    private fun formatPercentage(value: Double?): String {
        return if (value != null && value != 0.0) {
            String.format("%.2f%%", value * 100)
        } else {
            "-"
        }
    }

    private fun veil() {
        binding.veilLlStockPrice.veil()
        binding.veilLlStockNameLeft.veil()
        binding.pbChartLoading.visibility = View.VISIBLE
        binding.imageChart.visibility = View.GONE
    }

    private fun unVeil() {
        binding.veilLlStockPrice.unVeil()
        binding.veilLlStockNameLeft.unVeil()
        binding.pbChartLoading.visibility = View.GONE
        binding.imageChart.visibility = View.VISIBLE
    }
}
