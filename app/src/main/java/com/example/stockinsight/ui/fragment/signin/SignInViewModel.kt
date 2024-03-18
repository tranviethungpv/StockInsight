package com.example.stockinsight.ui.fragment.signin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.stockinsight.data.repository.AuthenticationRepository
import com.example.stockinsight.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignInViewModel
@Inject constructor(
    private val repository: AuthenticationRepository
): ViewModel() {
    private val _signIn = MutableLiveData<UiState<String>>()
    val signIn: LiveData<UiState<String>> get() = _signIn

    fun signInUser(email: String, password: String) {
        _signIn.value = UiState.Loading
        repository.signInUser(
            email,
            password,
        ) {
            _signIn.value = it
        }
    }
}