package com.example.stockinsight.ui.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.stockinsight.databinding.FragmentHomeBinding
import com.example.stockinsight.ui.adapter.MultiQuoteForHomePageAdapter
import com.example.stockinsight.ui.viewmodel.QuoteViewModel
import com.example.stockinsight.ui.viewmodel.UserViewModel
import com.example.stockinsight.utils.UiState
import com.example.stockinsight.utils.isNetworkAvailable
import com.example.stockinsight.utils.showDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by viewModels()
    private val quoteViewModel: QuoteViewModel by viewModels()
    private var multiQuoteForHomePageAdapter: MultiQuoteForHomePageAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isNetworkAvailable(requireContext())) {
            userViewModel.fetchUser.observe(viewLifecycleOwner) { state ->
                when (state) {
                    is UiState.Success -> {
                        binding.txtUsername.text = state.data.username
                        Glide.with(requireContext()).load(state.data.profileImageUrl)
                            .into(binding.imageMemojiBoysFortyOne)
                    }

                    is UiState.Failure -> {
                        Log.e("HomeFragment", "Error fetching user: ${state.message}")
                        showDialog(state.message, "error", requireContext())
                    }

                    else -> {}
                }
            }
            if (userViewModel.fetchUser.value == null) {
                userViewModel.fetchUser()
            }

            binding.recyclerStock.setLayoutManager(
                LinearLayoutManager(
                    requireContext()
                )
            )
            // Check if the quote data is already cached
            quoteViewModel.getCachedQuoteForHomePage()?.let { data ->
                // Use the cached data to update the UI
                binding.recyclerStock.setAdapter(MultiQuoteForHomePageAdapter(data))
            } ?: run {
                // Fetch the quote data if it's not cached
                quoteViewModel.fetchQuoteForHomePage()
            }
            quoteViewModel.fetchQuoteForHomePage.observe(viewLifecycleOwner) { state ->
                when (state) {
                    is UiState.Loading -> {
                        binding.recyclerStock.addVeiledItems(11)
                        binding.recyclerStock.veil()
                    }

                    is UiState.Success -> {
                        multiQuoteForHomePageAdapter = MultiQuoteForHomePageAdapter(state.data)

                        binding.recyclerStock.setAdapter(multiQuoteForHomePageAdapter)
                        binding.recyclerStock.unVeil()
                    }

                    is UiState.Failure -> {
                        Log.e("HomeFragment", "Error fetching quotes: ${state.message}")
                        showDialog(state.message, "error", requireContext())
                    }
                }
            }
            if (quoteViewModel.fetchQuoteForHomePage.value == null) {
                quoteViewModel.fetchQuoteForHomePage()
            }

        } else {
            showDialog("No internet connection", "error", requireContext())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}