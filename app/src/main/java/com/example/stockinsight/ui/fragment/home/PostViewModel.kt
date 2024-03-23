package com.example.stockinsight.ui.fragment.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockinsight.data.model.Post
import com.example.stockinsight.data.repository.PostRepository
import com.example.stockinsight.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {
    private val _getPostData = MutableLiveData<UiState<ArrayList<Post>>>()
    val getPostData: LiveData<UiState<ArrayList<Post>>> get() = _getPostData

    fun getPosts() {
        viewModelScope.launch {
            _getPostData.value = UiState.Loading
            val posts = repository.getPosts { state ->
                when (state) {
                    is UiState.Success -> {
                        _getPostData.value = UiState.Success(state.data)
                    }

                    is UiState.Failure -> {
                        _getPostData.value = UiState.Failure(state.message)
                    }

                    else -> {}
                }
            }
        }
    }
}