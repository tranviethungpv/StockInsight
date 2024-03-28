package com.example.stockinsight.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockinsight.data.model.stock.StockInfo
import com.example.stockinsight.data.repository.StockRepository
import com.example.stockinsight.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuoteViewModel @Inject constructor(
    private val repository: StockRepository
) : ViewModel() {
    private val _fetQuoteForHomePage = MutableLiveData<UiState<ArrayList<StockInfo>>>()
    val fetQuoteForHomePage: LiveData<UiState<ArrayList<StockInfo>>> get() = _fetQuoteForHomePage

    fun getQuoteForHomePage() {
        val symbols = listOf(
            "VNM",
            "^DJI",
            "AAPL",
            "SBUX",
            "NKE",
            "GOOG",
            "^FTSE",
            "^GDAXI",
            "^HSI",
            "^N225",
            "^GSPC"
        )
        val interval = "1h"
        val range = "36h"
        _fetQuoteForHomePage.value = UiState.Loading
        viewModelScope.launch {
            val data = ArrayList<StockInfo>()
            for (symbol in symbols) {
                repository.fetchQuoteInformation(symbol, interval, range) { state ->
                    when (state) {
                        is UiState.Success -> {
                            data.add(state.data)
                        }

                        is UiState.Failure -> {
                            _fetQuoteForHomePage.value = UiState.Failure(state.message)
                        }

                        else -> {}
                    }
                }
            }
            _fetQuoteForHomePage.value = UiState.Success(data)
        }
    }
}