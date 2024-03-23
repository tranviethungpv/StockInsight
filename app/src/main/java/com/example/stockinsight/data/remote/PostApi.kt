package com.example.stockinsight.data.remote

import com.example.stockinsight.data.model.Post
import retrofit2.Response
import retrofit2.http.GET

interface PostApi {
    @GET("/posts")
    suspend fun getPost() : Response<ArrayList<Post>>
}