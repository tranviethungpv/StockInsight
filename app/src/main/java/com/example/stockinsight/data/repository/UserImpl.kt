package com.example.stockinsight.data.repository

import com.example.stockinsight.data.model.User
import com.example.stockinsight.utils.UiState
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserImpl @Inject constructor(
    private val database: FirebaseFirestore
) : UserRepository {
    override fun fetchUser(
        userId: String, result: (UiState<User?>) -> Unit
    ): Task<DocumentSnapshot> {
        return database.collection("users").document(userId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = task.result?.toObject(User::class.java)
                result.invoke(UiState.Success(user))
            } else {
                result.invoke(UiState.Failure("Error occurred when fetching user"))
            }
        }
    }

    override suspend fun isSymbolInWatchlist(userId: String, symbol: String): Boolean {
        val userRef = database.collection("users").document(userId)
        val watchlistRef = userRef.collection("watchlist").document(symbol)

        val snapshot = watchlistRef.get().await()
        return snapshot.exists()
    }
}