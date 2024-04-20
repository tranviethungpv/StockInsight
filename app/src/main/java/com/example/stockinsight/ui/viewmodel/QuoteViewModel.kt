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
    private val _fetchQuoteForHomePage = MutableLiveData<UiState<ArrayList<StockInfo>>>()
    val fetchQuoteForHomePage: LiveData<UiState<ArrayList<StockInfo>>> get() = _fetchQuoteForHomePage
    private var cachedQuoteForHomePage: ArrayList<StockInfo>? = null

    fun fetchQuoteForHomePage() {
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
        _fetchQuoteForHomePage.value = UiState.Loading
        viewModelScope.launch {
            val data = ArrayList<StockInfo>()
            for (symbol in symbols) {
                repository.fetchQuoteInformation(symbol, interval, range) { state ->
                    when (state) {
                        is UiState.Success -> {
                            data.add(state.data)
                        }

                        is UiState.Failure -> {
                            _fetchQuoteForHomePage.value = UiState.Failure(state.message)
                        }

                        else -> {}
                    }
                }
            }
            _fetchQuoteForHomePage.value = UiState.Success(data)
            cachedQuoteForHomePage = data
        }
    }

    fun getCachedQuoteForHomePage(): ArrayList<StockInfo>? {
        return cachedQuoteForHomePage
    }
}