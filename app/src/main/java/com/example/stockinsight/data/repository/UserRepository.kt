package com.example.stockinsight.data.repository

import com.example.stockinsight.data.model.User
import com.example.stockinsight.utils.UiState
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot

interface UserRepository {
    fun fetchUser(userId: String, result: (UiState<User?>) -> Unit): Task<DocumentSnapshot>
}