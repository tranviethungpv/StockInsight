package com.example.stockinsight.data.repository

import com.example.stockinsight.data.model.User
import com.example.stockinsight.utils.UiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore

class AuthenticationImpl(
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
                                    "Error occurred when update user info"
                                )
                            )
                        }
                    }
                }
                result.invoke(
                    UiState.Success(
                        "User registered successfully"
                    )
                )
            } else {
                try {
                    throw task.exception ?: Exception("Error occurred")
                } catch (e: FirebaseAuthWeakPasswordException) {
                    result.invoke(
                        UiState.Failure(
                            "Weak password"
                        )
                    )
                } catch (e: FirebaseAuthInvalidCredentialsException) {
                    result.invoke(
                        UiState.Failure(
                            "Invalid email"
                        )
                    )
                } catch (e: FirebaseAuthUserCollisionException) {
                    result.invoke(
                        UiState.Failure(
                            "User already exists"
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
                    "User info updated successfully"
                )
            )
        }.addOnFailureListener {
            result.invoke(
                UiState.Failure(
                    it.localizedMessage ?: "Error occurred"
                )
            )
        }
    }
}