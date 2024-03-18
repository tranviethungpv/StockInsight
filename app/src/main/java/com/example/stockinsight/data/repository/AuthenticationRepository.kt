package com.example.stockinsight.data.repository

import com.example.stockinsight.data.model.User
import com.example.stockinsight.utils.UiState

interface AuthenticationRepository {
    fun registerUser(password: String, user: User, result: (UiState<String>) -> Unit)
    fun updateUserInfo(user: User, result: (UiState<String>) -> Unit)
    fun signInUser(email: String, password: String, result: (UiState<String>) -> Unit)
}