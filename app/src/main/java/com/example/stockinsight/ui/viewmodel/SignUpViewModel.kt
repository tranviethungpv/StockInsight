package com.example.stockinsight.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.stockinsight.data.model.User
import com.example.stockinsight.data.repository.AuthenticationRepository
import com.example.stockinsight.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel
@Inject constructor(
    private val repository: AuthenticationRepository
): ViewModel() {
    private val _register = MutableLiveData<UiState<String>>()
    val register: LiveData<UiState<String>> get() = _register

    fun registerUser(password: String, user: User) {
        _register.value = UiState.Loading
        repository.registerUser(
            password,
            user,
        ) {
            _register.value = it
        }
    }
}