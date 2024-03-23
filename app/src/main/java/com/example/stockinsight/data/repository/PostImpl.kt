package com.example.stockinsight.data.repository

import com.example.stockinsight.data.model.Post
import com.example.stockinsight.data.remote.PostApi
import com.example.stockinsight.utils.UiState
import javax.inject.Inject

class PostImpl @Inject constructor(
    private val postApi: PostApi
)
: PostRepository {
    override suspend fun getPosts(result: (UiState<ArrayList<Post>>) -> Unit) {
        val response = postApi.getPost()
        if (response.isSuccessful) {
            result(UiState.Success(ArrayList(response.body()!!)))
        } else {
            result(UiState.Failure(response.message()))
        }
    }
}