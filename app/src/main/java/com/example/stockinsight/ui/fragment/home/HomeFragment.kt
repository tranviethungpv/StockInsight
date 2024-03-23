package com.example.stockinsight.ui.fragment.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.stockinsight.R
import com.example.stockinsight.data.model.Stock
import com.example.stockinsight.databinding.FragmentHomeBinding
import com.example.stockinsight.databinding.FragmentSignInBinding
import com.example.stockinsight.ui.fragment.user.UserViewModel
import com.example.stockinsight.utils.UiState
import com.example.stockinsight.utils.showDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by viewModels()
    private val postViewModel: PostViewModel by viewModels()


    private val recyclerStock: RecyclerView by lazy {
        binding.recyclerStock.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
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
                    Glide.with(requireContext())
                        .load(user.profileImageUrl)
                        .into(it)
                }
            }
        }

        userViewModel.fetchUser()

        postViewModel.getPostData.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                }

                is UiState.Success -> {
                    val post = state.data
                    val postAdapter = PostAdapter(post)
                    recyclerStock.adapter = postAdapter
                }

                is UiState.Failure -> {
                    showDialog(state.message, "error", requireContext())
                }
            }
        }
        postViewModel.getPosts()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}