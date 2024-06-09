package com.example.stockinsight.data.repository

import android.content.Context
import com.example.stockinsight.R
import com.example.stockinsight.data.model.Issue
import com.example.stockinsight.data.model.User
import com.example.stockinsight.utils.UiState
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserImpl @Inject constructor(
    private val context: Context,
    private val database: FirebaseFirestore,
    private val auth: FirebaseAuth
) : UserRepository {
    override fun fetchUser(
        userId: String, result: (UiState<User?>) -> Unit
    ): Task<DocumentSnapshot> {
        return database.collection("users").document(userId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = task.result?.toObject(User::class.java)
                result.invoke(UiState.Success(user))
            } else {
                result.invoke(UiState.Failure(context.getString(R.string.error_fetching_user)))
            }
        }
    }

    override suspend fun isSymbolInWatchlist(userId: String, symbol: String): Boolean {
        val userRef = database.collection("users").document(userId)
        val watchlistRef = userRef.collection("watchlist").document(symbol)

        val snapshot = watchlistRef.get().await()
        return snapshot.exists()
    }

    override suspend fun sendIssue(issue: Issue): UiState<String> {
        val issueRef = database.collection("issues").document()
        return try {
            issueRef.set(issue).await()
            UiState.Success(context.getString(R.string.issue_sent_successfully))
        } catch (e: Exception) {
            UiState.Failure(context.getString(R.string.error_sending_issue))
        }
    }

    override suspend fun changePassword(email: String, newPassword: String): UiState<String> {
        auth.currentUser?.let { user ->
            return try {
                user.updatePassword(newPassword).await()
                UiState.Success(context.getString(R.string.password_changed_successfully))
            } catch (e: Exception) {
                UiState.Failure(context.getString(R.string.error_changing_password))
            }
        } ?: return UiState.Failure(context.getString(R.string.user_not_found))
    }
}