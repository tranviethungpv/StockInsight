package com.example.stockinsight.ui.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stockinsight.databinding.FragmentHomeBinding
import com.example.stockinsight.ui.adapter.MultiQuoteForHomePageAdapter
import com.example.stockinsight.ui.viewmodel.StockViewModel
import com.example.stockinsight.ui.viewmodel.UserViewModel
import com.example.stockinsight.utils.SessionManager
import com.example.stockinsight.utils.UiState
import com.example.stockinsight.utils.isNetworkAvailable
import com.example.stockinsight.utils.showDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    @Inject
    lateinit var sessionManager: SessionManager

    private val userViewModel: UserViewModel by viewModels()
    private val stockViewModel: StockViewModel by viewModels()
    private lateinit var multiQuoteForHomePageAdapter: MultiQuoteForHomePageAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isNetworkAvailable(requireContext())) {
            userViewModel.fetchUser()

            // Request stock data
            val symbols = listOf(
                "VNM",
                "^DJI",
                "AAPL",
                "SBUX",
                "NKE",
                "GOOG",
                "^FTSE",
                "^GDAXI",
                "^HSI",
                "^N225",
                "^GSPC"
            )
            val interval = "1m"
            val range = "1d"
            stockViewModel.getListQuotesForHomePage(symbols, interval, range)

            setupRecyclerView()
            observer()
        } else {
            showDialog("No internet connection", "error", requireContext())
        }
    }

    private fun setupRecyclerView() {
        multiQuoteForHomePageAdapter = MultiQuoteForHomePageAdapter(onItemClick = {
            val action =
                HomeFragmentDirections.actionHomeFragmentToStockDetailFragment(it.quoteInfo.symbol)
            binding.root.findNavController().navigate(action)
        })

        binding.recyclerStock.apply {
            setLayoutManager(LinearLayoutManager(requireContext()))
            setAdapter(multiQuoteForHomePageAdapter)
        }
    }

    private fun observer() {
        userViewModel.fetchUser.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> {
                    binding.txtUsername.text = state.data.username
//                        Glide.with(requireContext()).load(state.data.profileImageUrl)
//                            .into(binding.imageMemojiBoysFortyOne)
                }

                is UiState.Failure -> {
                    showDialog(state.message, "error", requireContext())
                }

                else -> {}
            }
        }

        stockViewModel.listQuotesForHomePage.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.recyclerStock.addVeiledItems(11)
                    binding.recyclerStock.veil()
                }

                is UiState.Success -> {
                    Log.d("HomeFragment", "Data: ${state.data.size}")
                    multiQuoteForHomePageAdapter.updateList(state.data)
                    binding.recyclerStock.unVeil()
                }

                is UiState.Failure -> {
                    showDialog(state.message, "error", requireContext())
                }
            }
        }
    }
}