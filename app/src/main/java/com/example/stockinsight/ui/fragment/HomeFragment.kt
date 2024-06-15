package com.example.stockinsight.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stockinsight.R
import com.example.stockinsight.databinding.FragmentHomeBinding
import com.example.stockinsight.ui.adapter.MultiQuoteForHomePageAdapter
import com.example.stockinsight.ui.adapter.SearchResultAdapter
import com.example.stockinsight.ui.viewmodel.StockViewModel
import com.example.stockinsight.ui.viewmodel.UserViewModel
import com.example.stockinsight.utils.UiState
import com.example.stockinsight.utils.getRandomItemsFromFile
import com.example.stockinsight.utils.isNetworkAvailable
import com.example.stockinsight.utils.showDialog
import com.example.stockinsight.utils.startStockPriceService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    private val userViewModel: UserViewModel by viewModels()
    private val stockViewModel: StockViewModel by viewModels()
    private lateinit var multiQuoteForHomePageAdapter: MultiQuoteForHomePageAdapter
    private lateinit var searchResultAdapter: SearchResultAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.swipeRefreshLayout.setOnRefreshListener {
            stockViewModel.getListQuotesForHomePage(
                getRandomItemsFromFile(
                    requireContext(), R.raw.list_symbols, 10
                ), "1d"
            )
            binding.swipeRefreshLayout.isRefreshing = false
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isNetworkAvailable(requireContext())) {
            userViewModel.fetchUser()

            stockViewModel.getListQuotesForHomePage(
                getRandomItemsFromFile(
                    requireContext(), R.raw.list_symbols, 10
                ), "1d"
            )

            binding.searchEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    binding.progressSearchResults.visibility = View.GONE
                    binding.recyclerSearchResults.visibility = View.GONE
                    if (s.toString().isNotEmpty()) {
                        binding.recyclerSearchResults.visibility = View.VISIBLE
                        binding.recyclerStock.visibility = View.GONE

                        binding.recyclerSearchResults.visibility = View.VISIBLE
                        searchResultAdapter = SearchResultAdapter(onItemClick = {
                            val action =
                                HomeFragmentDirections.actionHomeFragmentToStockDetailFragment(it.quoteInfo.symbol)
                            binding.root.findNavController().navigate(action)
                        })
                        binding.recyclerSearchResults.apply {
                            layoutManager = LinearLayoutManager(requireContext())
                            adapter = searchResultAdapter
                        }
                        stockViewModel.searchStocksByKeyword(s.toString(), "1d")
                        stockViewModel.searchResult.observe(viewLifecycleOwner) {
                            when (it) {
                                is UiState.Loading -> {
                                    binding.progressSearchResults.visibility = View.VISIBLE
                                }

                                is UiState.Success -> {
                                    if (it.data.isEmpty()) {
                                        binding.txtNoResults.visibility = View.VISIBLE
                                    } else {
                                        binding.txtNoResults.visibility = View.GONE
                                    }
                                    binding.progressSearchResults.visibility = View.GONE
                                    searchResultAdapter.updateList(it.data)
                                }

                                is UiState.Failure -> {
                                    stockViewModel.searchStocksByKeyword(s.toString(), "5d")
                                }
                            }
                        }
                    } else {
                        binding.recyclerSearchResults.visibility = View.GONE
                        binding.recyclerStock.visibility = View.VISIBLE
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            setupRecyclerView()
            observer()

            startStockPriceService(requireContext())

        } else {
            showDialog(getString(R.string.no_internet_connection), "error", requireContext())
        }

    }

    private fun setupRecyclerView() {
        multiQuoteForHomePageAdapter = MultiQuoteForHomePageAdapter(onItemClick = {
            val action =
                HomeFragmentDirections.actionHomeFragmentToStockDetailFragment(it.quoteInfo.symbol)
            binding.root.findNavController().navigate(action)
        })

        binding.recyclerStock.apply {
            setLayoutManager(LinearLayoutManager(this@HomeFragment.requireContext()))
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

    override fun onDestroy() {
        super.onDestroy()
        stockViewModel.closeSocket("homepage")
    }
}