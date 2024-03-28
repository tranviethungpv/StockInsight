package com.example.stockinsight.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.stockinsight.databinding.FragmentHomeBinding
import com.example.stockinsight.ui.adapter.MultiQuoteForHomePageAdapter
import com.example.stockinsight.ui.viewmodel.QuoteViewModel
import com.example.stockinsight.ui.viewmodel.UserViewModel
import com.example.stockinsight.utils.UiState
import com.example.stockinsight.utils.showDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by viewModels()
    private val quoteViewModel: QuoteViewModel by viewModels()
    private var multiQuoteForHomePageAdapter: MultiQuoteForHomePageAdapter? = null

    private val recyclerStock: RecyclerView by lazy {
        binding.recyclerStock.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(
                DividerItemDecoration(
                    context, DividerItemDecoration.VERTICAL
                )
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel.userInfo.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.txtUsername.text = user.username
                binding.imageMemojiBoysFortyOne.let {
                    Glide.with(requireContext()).load(user.profileImageUrl).into(it)
                }
            }
        }

        userViewModel.fetchUser()

        quoteViewModel.fetQuoteForHomePage.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                }

                is UiState.Success -> {
                    val multiQuoteForHomePage = state.data
                    multiQuoteForHomePageAdapter =
                        MultiQuoteForHomePageAdapter(multiQuoteForHomePage)
                    recyclerStock.adapter = multiQuoteForHomePageAdapter
                }

                is UiState.Failure -> {
                    showDialog(state.message, "error", requireContext())
                }
            }
        }
        quoteViewModel.getQuoteForHomePage()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}