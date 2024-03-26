package com.example.stockinsight.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockinsight.data.model.StockHomePage
import com.example.stockinsight.data.repository.StockRepository
import com.example.stockinsight.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StockHomePageViewModel @Inject constructor(
    private val repository: StockRepository
) : ViewModel() {
    private val _getStockHomePageData = MutableLiveData<UiState<ArrayList<StockHomePage>>>()
    val getStockHomePageData: LiveData<UiState<ArrayList<StockHomePage>>> get() = _getStockHomePageData

    fun getStockHomePage() {
        viewModelScope.launch {
            _getStockHomePageData.value = UiState.Loading
            val stockHomePage = repository.getStockHomePage { state ->
                when (state) {
                    is UiState.Success -> {
                        _getStockHomePageData.value = UiState.Success(state.data)
                    }

                    is UiState.Failure -> {
                        _getStockHomePageData.value = UiState.Failure(state.message)
                    }

                    else -> {}
                }
            }
        }
    }
}