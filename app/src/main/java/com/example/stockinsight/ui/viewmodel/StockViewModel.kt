package com.example.stockinsight.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockinsight.data.model.stock.FullStockInfo
import com.example.stockinsight.data.repository.StockRepository
import com.example.stockinsight.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StockViewModel @Inject constructor(
    private val repository: StockRepository
) : ViewModel() {

    private val _listQuotesForHomePage = MutableLiveData<UiState<ArrayList<FullStockInfo>>>()
    val listQuotesForHomePage: LiveData<UiState<ArrayList<FullStockInfo>>> get() = _listQuotesForHomePage

    private val _watchlist = MutableLiveData<UiState<ArrayList<FullStockInfo>>>()
    val watchlist: LiveData<UiState<ArrayList<FullStockInfo>>> get() = _watchlist

    private val _stockInfo = MutableLiveData<UiState<FullStockInfo>>()
    val stockInfo: LiveData<UiState<FullStockInfo>> get() = _stockInfo

    private val _addStockResult = MutableLiveData<UiState<String>>()
    val addStockResult: LiveData<UiState<String>> get() = _addStockResult

    private val _removeStockResult = MutableLiveData<UiState<String>>()
    val removeStockResult: LiveData<UiState<String>> get() = _removeStockResult

    private val _searchResult = MutableLiveData<UiState<ArrayList<FullStockInfo>>>()
    val searchResult: LiveData<UiState<ArrayList<FullStockInfo>>> get() = _searchResult

    fun getListQuotesForHomePage(symbols: List<String>, interval: String, range: String) {
        _listQuotesForHomePage.value = UiState.Loading
        repository.getStockInfo(symbols, interval, range) {
            _listQuotesForHomePage.postValue(it)
        }
    }

    fun getListQuotesForWatchlist(userId: String?) {
        _watchlist.value = UiState.Loading
        repository.fetchWatchlist(userId) {
            _watchlist.postValue(it)
        }
    }

    fun getStockInfoBySymbol(symbol: String, interval: String, range: String) {
        _stockInfo.value = UiState.Loading
        viewModelScope.launch {
            val result = repository.getStockInfoBySymbol(symbol, interval, range)
            _stockInfo.postValue(result)
        }
    }

    fun searchStocksByKeyword(keyword: String, interval: String, range: String) {
        _searchResult.value = UiState.Loading
        repository.searchStocksByKeyword(keyword, interval, range) {
            _searchResult.postValue(it)
        }
    }

    fun addStockToWatchlist(userId: String, symbol: String) {
        _addStockResult.value = UiState.Loading
        viewModelScope.launch {
            val result = repository.addStockToWatchlist(userId, symbol)
            _addStockResult.postValue(result)
        }
    }

    fun removeStockFromWatchlist(userId: String, symbol: String) {
        _removeStockResult.value = UiState.Loading
        viewModelScope.launch {
            val result = repository.removeStockFromWatchlist(userId, symbol)
            _removeStockResult.postValue(result)
        }
    }
}
