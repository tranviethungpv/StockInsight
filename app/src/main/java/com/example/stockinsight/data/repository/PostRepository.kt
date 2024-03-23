package com.example.stockinsight.data.repository

import com.example.stockinsight.data.model.Post
import com.example.stockinsight.utils.UiState

interface PostRepository {
    suspend fun getPosts(result: (UiState<ArrayList<Post>>) -> Unit)
}