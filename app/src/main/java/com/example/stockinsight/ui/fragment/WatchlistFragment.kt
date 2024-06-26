package com.example.stockinsight.ui.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stockinsight.R
import com.example.stockinsight.databinding.FragmentWatchlistBinding
import com.example.stockinsight.ui.adapter.WatchlistAdapter
import com.example.stockinsight.ui.viewmodel.StockViewModel
import com.example.stockinsight.utils.SessionManager
import com.example.stockinsight.utils.UiState
import com.example.stockinsight.utils.isNetworkAvailable
import com.example.stockinsight.utils.showDialog
import com.example.stockinsight.utils.startStockPriceService
import dagger.hilt.android.AndroidEntryPoint
import java.lang.reflect.Method
import javax.inject.Inject

@AndroidEntryPoint
class WatchlistFragment : Fragment() {
    private lateinit var binding: FragmentWatchlistBinding

    @Inject
    lateinit var sessionManager: SessionManager

    private val stockViewModel: StockViewModel by viewModels()
    private lateinit var watchListAdapter: WatchlistAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentWatchlistBinding.inflate(inflater, container, false)

        binding.swipeRefreshLayout.setOnRefreshListener {
            if (isNetworkAvailable(this.requireContext())) {
                stockViewModel.getListQuotesForWatchlist(sessionManager.getUserId())
            } else {
                showDialog(getString(R.string.no_internet_connection), "error", this.requireContext())
            }
            binding.swipeRefreshLayout.isRefreshing = false
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isNetworkAvailable(this.requireContext())) {
            stockViewModel.getListQuotesForWatchlist(sessionManager.getUserId())

            setupRecyclerView()
            observer()
        } else {
            showDialog(getString(R.string.no_internet_connection), "error", this.requireContext())
        }
    }

    private fun setupRecyclerView() {
        watchListAdapter = WatchlistAdapter(onItemClick = {
            val action =
                WatchlistFragmentDirections.actionWatchlistFragmentToStockDetailFragment(it.quoteInfo.symbol)
            binding.root.findNavController().navigate(action)
        }, onItemLongClick = { stockInfo, view ->
            val popupMenu = PopupMenu(this.requireContext(), view)
            popupMenu.menuInflater.inflate(R.menu.watchlist_context_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.set_notification -> {
                        val dialog = Dialog(this.requireContext())
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                        dialog.setContentView(R.layout.dialog_set_threshold)

                        val window = dialog.window

                        window?.setLayout(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                        dialog.findViewById<Button>(R.id.btnCancel).setOnClickListener {
                            dialog.dismiss()
                        }

                        dialog.findViewById<Button>(R.id.btnOk).setOnClickListener {
                            val thresholdText =
                                dialog.findViewById<EditText>(R.id.etInputsThreshold).text.toString()
                            try {
                                val threshold = thresholdText.toDouble()
                                stockViewModel.updateThreshold(
                                    sessionManager.getUserId() ?: "",
                                    stockInfo.quoteInfo.symbol,
                                    threshold
                                )
                                dialog.dismiss()
                            } catch (e: NumberFormatException) {
                                showDialog(
                                    getString(R.string.please_enter_a_valid_number_for_the_threshold),
                                    "error",
                                    this.requireContext()
                                )
                            }
                        }

                        dialog.show()
                        true
                    }

                    R.id.delete -> {
                        AlertDialog.Builder(this.requireContext())
                            .setTitle(getString(R.string.delete_watchlist))
                            .setMessage(getString(R.string.delete_watchlist_message))
                            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                                stockViewModel.removeStockFromWatchlist(
                                    sessionManager.getUserId() ?: getString(R.string.blank),
                                    stockInfo.quoteInfo.symbol
                                )
                            }.setNegativeButton(getString(R.string.no), null).show()
                        true
                    }

                    else -> false
                }
            }
            showPopupMenuIcons(popupMenu)
            popupMenu.show()
        })

        binding.recyclerWatchlist.apply {
            setLayoutManager(LinearLayoutManager(this@WatchlistFragment.requireContext()))
            setAdapter(watchListAdapter)
        }
    }

    private fun observer() {
        stockViewModel.watchlist.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.recyclerWatchlist.visibility = View.GONE
                    binding.txtWatchlistDataStatus.visibility = View.GONE
                    binding.progressBarWatchlist.visibility = View.VISIBLE
                }

                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        Log.d("WatchlistFragment", "Data: ${state.data.size}")
                        binding.txtWatchlistDataStatus.visibility = View.VISIBLE
                        binding.recyclerWatchlist.visibility = View.GONE
                    } else {
                        binding.txtWatchlistDataStatus.visibility = View.GONE
                        binding.recyclerWatchlist.visibility = View.VISIBLE
                    }
                    watchListAdapter.updateList(state.data)
                    binding.progressBarWatchlist.visibility = View.GONE
                }

                is UiState.Failure -> {
                    binding.recyclerWatchlist.visibility = View.GONE
                    binding.progressBarWatchlist.visibility = View.GONE
                    binding.txtWatchlistDataStatus.visibility = View.VISIBLE
                    showDialog(state.message, "error", this.requireContext())
                }
            }
        }

        stockViewModel.removeStockResult.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBarWatchlist.visibility = View.VISIBLE
                }

                is UiState.Success -> {
                    showDialog(
                        getString(R.string.removed_from_watchlist_successfully),
                        "success",
                        this.requireContext()
                    )
                    binding.progressBarWatchlist.visibility = View.GONE
                    stockViewModel.getListQuotesForWatchlist(sessionManager.getUserId())
                    startStockPriceService(this.requireContext())
                }

                is UiState.Failure -> {
                    showDialog(state.message, "error", this.requireContext())
                    binding.progressBarWatchlist.visibility = View.GONE
                }
            }
        }

        stockViewModel.updateThresholdResult.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                }

                is UiState.Success -> {
                    showDialog(
                        getString(R.string.threshold_updated_successfully),
                        "success",
                        this.requireContext()
                    )
                    startStockPriceService(this.requireContext())
                }

                is UiState.Failure -> {
                    showDialog(state.message, "error", this.requireContext())
                }
            }
        }
    }

    private fun showPopupMenuIcons(popupMenu: PopupMenu) {
        try {
            val fields = popupMenu.javaClass.declaredFields
            for (field in fields) {
                if ("mPopup" == field.name) {
                    field.isAccessible = true
                    val menuPopupHelper = field.get(popupMenu)
                    val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
                    val setForceIcons: Method = classPopupHelper.getMethod(
                        "setForceShowIcon", Boolean::class.javaPrimitiveType
                    )
                    setForceIcons.invoke(menuPopupHelper, true)
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stockViewModel.closeSocket("watchlist")
    }
}