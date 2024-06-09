package com.example.stockinsight.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.stockinsight.data.repository.AuthenticationRepository
import com.example.stockinsight.utils.SessionManager
import com.example.stockinsight.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignInViewModel
@Inject constructor(
    private val repository: AuthenticationRepository, private val sessionManager: SessionManager
) : ViewModel() {
    private val _signIn = MutableLiveData<UiState<String>>()
    val signIn: LiveData<UiState<String>> get() = _signIn

    private val _forgotPassword = MutableLiveData<UiState<String>>()
    val forgotPassword: LiveData<UiState<String>> get() = _forgotPassword

    private val _checkPassword = MutableLiveData<UiState<Boolean>>()
    val checkPassword: LiveData<UiState<Boolean>> get() = _checkPassword

    fun signInUser(email: String, password: String) {
        _signIn.value = UiState.Loading
        repository.signInUser(
            email,
            password,
        ) {
            if (it is UiState.Success) {
                sessionManager.saveLoginSession(it.data, email)
            }
            _signIn.value = it
        }
    }

    fun forgotPassword(email: String) {
        repository.forgotPassword(email) {
            _forgotPassword.value = it
        }
    }

    fun checkPassword(email: String, currentPassword: String) {
        repository.checkPassword(email, currentPassword) {
            _checkPassword.value = it
        }
    }
}