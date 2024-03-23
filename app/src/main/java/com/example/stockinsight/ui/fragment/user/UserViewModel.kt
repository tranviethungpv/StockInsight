package com.example.stockinsight.ui.fragment.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.stockinsight.data.model.User
import com.example.stockinsight.data.repository.UserRepository
import com.example.stockinsight.utils.UiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
): ViewModel() {
    private val _userInfo = MutableLiveData<User?>()
    val userInfo: LiveData<User?> get() = _userInfo

    private val _fetchUser = MutableLiveData<UiState<String>>()
    val fetchUser: LiveData<UiState<String>> get() = _fetchUser

    fun fetchUser() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            _fetchUser.value = UiState.Loading
            userRepository.fetchUser(userId) { state ->
                when (state) {
                    is UiState.Success -> {
                        _userInfo.value = state.data
                        _fetchUser.value = UiState.Success("User fetched successfully")
                    }
                    is UiState.Failure -> {
                        _fetchUser.value = UiState.Failure(state.message)
                    }
                    else -> {}
                }
            }
        }
    }
}