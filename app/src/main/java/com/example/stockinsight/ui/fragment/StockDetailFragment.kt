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
import com.example.stockinsight.utils.startStockPriceService
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
        stockViewModel.getStockInfoBySymbol(stockSymbol, "1d")
        if(session.isLoggedIn()) {
            userViewModel.checkSymbolInWatchlist(
                session.getUserId() ?: getString(R.string.blank), stockSymbol
            )
        }

        observerStockInfo()
        observerIsSymbolInWatchlist()

        binding.ivClose.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.ivFavorite.setOnClickListener {
            if (session.isLoggedIn()) {
                session.getUserId()?.let { userId ->
                    if (isInWatchlist) {
                        stockViewModel.removeStockFromWatchlist(userId, stockSymbol)
                    } else {
                        // Assuming you have some predefined values for threshold and lastNotifiedPrice
                        val threshold = 0.0  // Example threshold value
                        val lastNotifiedPrice =
                            0.0  // Example initial value for last notified price

                        // If these values come from user input, you can fetch them from the respective UI elements
                        stockViewModel.addStockToWatchlist(
                            userId, stockSymbol, threshold, lastNotifiedPrice
                        )
                    }
                }
            } else {
                showDialog(
                    getString(R.string.please_login_to_add_to_watchlist),
                    "warning",
                    requireContext()
                )
            }
        }


        stockViewModel.addStockResult.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    veil()
                }

                is UiState.Success -> {
                    showDialog(
                        getString(R.string.add_to_watchlist_successfully),
                        "success",
                        requireContext()
                    )
                    userViewModel.checkSymbolInWatchlist(
                        session.getUserId() ?: getString(R.string.blank), stockSymbol
                    )
                    startStockPriceService(requireContext())
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
                    showDialog(
                        getString(R.string.removed_from_watchlist_successfully),
                        "success",
                        requireContext()
                    )
                    userViewModel.checkSymbolInWatchlist(
                        session.getUserId() ?: getString(R.string.blank), stockSymbol
                    )
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
                R.id.ro_3m -> "3mo"
                R.id.ro_6m -> "6mo"
                R.id.ro_ytd -> "ytd"
                R.id.ro_1y -> "1y"
                R.id.ro_all -> "max"
                else -> "1d"
            }
            updateChartData(chartMode)
        }
    }

    private fun updateChartData(period: String) {
        // Update the stock information based on the selected period
        veil()
        stockViewModel.getStockInfoBySymbol(stockSymbol, period)
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
                    val quoteInfo = state.data.quoteInfo
                    val historicData = state.data.historicData

                    // Check if quoteInfo and historicData are not null before accessing their properties
                    if (quoteInfo != null && historicData != null) {
                        binding.tvSymbol.text = quoteInfo.symbol ?: ""
                        binding.tvCompanyName.text = quoteInfo.shortName ?: ""

                        binding.tvPrice.text = formatValue(quoteInfo.currentPrice, quoteInfo.today)
                        binding.txtTwentyThreeOne.text = formatValue(quoteInfo.diff)

                        if (quoteInfo.diff > 0) {
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

                        binding.tvOpenValue.text = formatValue(quoteInfo.open)
                        binding.tvHighValue.text = formatValue(quoteInfo.dayHigh)
                        binding.tvLowValue.text = formatValue(quoteInfo.dayLow)
                        binding.tvVolumeValue.text = formatNumber(quoteInfo.volume)
                        binding.tvRatioValue.text = formatValue(quoteInfo.trailingPE)
                        binding.tvMarketCapValue.text = formatNumber(quoteInfo.marketCap)
                        binding.tv52WeekHighValue.text = formatValue(quoteInfo.fiftyTwoWeekHigh)
                        binding.tv52WeekLowValue.text = formatValue(quoteInfo.fiftyTwoWeekLow)
                        binding.tvAverageVolumeValue.text = formatNumber(quoteInfo.averageVolume)

                        val dividendYield =
                            if (quoteInfo.dividendYield != 0.0) quoteInfo.dividendYield else quoteInfo.yield
                        binding.tvYieldValue.text = formatPercentage(dividendYield)

                        val beta =
                            if (quoteInfo.beta != 0.0) quoteInfo.beta else quoteInfo.beta3Year
                        binding.tvBetaValue.text = formatValue(beta)
                        binding.tvEarningsPerShareValue.text = formatValue(quoteInfo.trailingEps)

                        val chartData = historicData.close

                        val entries = mutableListOf<Entry>()
                        chartData?.let { closeData ->
                            for ((timestamp, value) in closeData) {
                                value?.toFloat()?.let { Entry(timestamp.toFloat(), it) }
                                    ?.let { entries.add(it) }
                            }
                        }

                        if (quoteInfo.diff > 0) {
                            drawFullLineChart(binding.imageChart, entries, "up", chartMode)
                        } else {
                            drawFullLineChart(binding.imageChart, entries, "down", chartMode)
                        }
                        unVeil()
                    } else {
                        // Handle the case where quoteInfo or historicData is null
                        showDialog(
                            getString(R.string.error_parsing_data), "error", requireContext()
                        )
                        unVeil()
                    }
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
