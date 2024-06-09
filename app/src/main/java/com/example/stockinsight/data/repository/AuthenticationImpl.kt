package com.example.stockinsight.data.repository

import android.content.Context
import com.example.stockinsight.R
import com.example.stockinsight.data.model.User
import com.example.stockinsight.utils.UiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class AuthenticationImpl @Inject constructor(
    private val context: Context,
    private val auth: FirebaseAuth,
    private val database: FirebaseFirestore,
) : AuthenticationRepository {
    override fun registerUser(
        password: String, user: User, result: (UiState<String>) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(user.email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                user.id = task.result?.user?.uid.toString()
                updateUserInfo(user) { updateResult ->
                    when (updateResult) {
                        is UiState.Success -> {
                        }

                        is UiState.Failure -> {
                            result.invoke(
                                UiState.Failure(
                                    updateResult.message
                                )
                            )
                        }

                        else -> {
                            result.invoke(
                                UiState.Failure(
                                    context.getString(R.string.error_update_user_info)
                                )
                            )
                        }
                    }
                }
                result.invoke(
                    UiState.Success(
                        context.getString(R.string.user_registered_successfully)
                    )
                )
            } else {
                try {
                    throw task.exception ?: Exception(context.getString(R.string.error_occurred))
                } catch (e: FirebaseAuthWeakPasswordException) {
                    result.invoke(
                        UiState.Failure(
                            context.getString(R.string.weak_password)
                        )
                    )
                } catch (e: FirebaseAuthInvalidCredentialsException) {
                    result.invoke(
                        UiState.Failure(
                            context.getString(R.string.invalid_email)
                        )
                    )
                } catch (e: FirebaseAuthUserCollisionException) {
                    result.invoke(
                        UiState.Failure(
                            context.getString(R.string.user_already_exists)
                        )
                    )
                } catch (e: Exception) {
                    result.invoke(
                        UiState.Failure(
                            e.message.toString()
                        )
                    )
                }
            }
        }.addOnFailureListener {
//            result.invoke(
//                UiState.Failure(
//                    it.localizedMessage ?: "Error occurred"
//                )
//            )
        }
    }

    override fun updateUserInfo(user: User, result: (UiState<String>) -> Unit) {
        database.collection("users").document(user.id).set(user).addOnSuccessListener {
            result.invoke(
                UiState.Success(
                    context.getString(R.string.user_info_updated_successfully)
                )
            )
        }.addOnFailureListener {
            result.invoke(
                UiState.Failure(
                    it.localizedMessage ?: context.getString(R.string.error_occurred)
                )
            )
        }
    }

    override fun signInUser(email: String, password: String, result: (UiState<String>) -> Unit) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = task.result?.user?.uid
                if (userId != null) {
                    result.invoke(
                        UiState.Success(userId)
                    )
                } else {
//                        result.invoke(
//                            UiState.Failure("User ID is null")
//                        )
                }
            } else {
                result.invoke(
                    UiState.Failure(
                        task.exception?.message ?: context.getString(R.string.error_occurred)
                    )
                )
            }
        }.addOnFailureListener {
            result.invoke(
                UiState.Failure(
                    it.localizedMessage ?: context.getString(R.string.error_occurred)
                )
            )
        }
    }

    override fun forgotPassword(email: String, result: (UiState<String>) -> Unit) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                result.invoke(
                    UiState.Success(
                        context.getString(R.string.password_reset_email_sent_successfully)
                    )
                )
            } else {
                result.invoke(
                    UiState.Failure(
                        task.exception?.message ?: context.getString(R.string.error_occurred)
                    )
                )
            }
        }
    }

    override fun checkPassword(
        email: String, currentPassword: String, result: (UiState<Boolean>) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, currentPassword).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                result.invoke(UiState.Success(true))
            } else {
                result.invoke(UiState.Success(false))
            }
        }
    }

    override fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}