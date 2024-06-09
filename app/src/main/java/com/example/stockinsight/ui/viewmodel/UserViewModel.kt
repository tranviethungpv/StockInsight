package com.example.stockinsight.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockinsight.data.model.Issue
import com.example.stockinsight.data.model.User
import com.example.stockinsight.data.repository.UserRepository
import com.example.stockinsight.utils.UiState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository, private val auth: FirebaseAuth
) : ViewModel() {
    private val _fetchUser = MutableLiveData<UiState<User>>()
    val fetchUser: LiveData<UiState<User>> get() = _fetchUser

    private val _isSymbolInWatchlist = MutableLiveData<Boolean>()
    val isSymbolInWatchlist: LiveData<Boolean> get() = _isSymbolInWatchlist

    private val _sendIssue = MutableLiveData<UiState<String>>()
    val sendIssue: LiveData<UiState<String>> get() = _sendIssue

    private val _changePassword = MutableLiveData<UiState<String>>()
    val changePassword: LiveData<UiState<String>> get() = _changePassword

    fun fetchUser() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            _fetchUser.value = UiState.Loading
            userRepository.fetchUser(userId) { state ->
                when (state) {
                    is UiState.Success -> {
                        state.data?.let {
                            _fetchUser.value = UiState.Success(it)
                        }
                    }

                    is UiState.Failure -> {
                        _fetchUser.value = UiState.Failure(state.message)
                    }

                    else -> {}
                }
            }
        }
    }

    fun checkSymbolInWatchlist(userId: String, symbol: String) {
        viewModelScope.launch {
            val isInWatchlist = userRepository.isSymbolInWatchlist(userId, symbol)
            _isSymbolInWatchlist.postValue(isInWatchlist)
        }
    }

    fun sendIssue(issue: Issue) {
        viewModelScope.launch {
            val result = userRepository.sendIssue(issue)
            _sendIssue.postValue(result)
        }
    }

    fun changePassword(email: String, newPassword: String) {
        viewModelScope.launch {
            val result = userRepository.changePassword(email, newPassword)
            _changePassword.postValue(result)
        }
    }
}