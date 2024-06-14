package com.example.stockinsight.data.repository

import android.content.Context
import android.util.Log
import com.example.stockinsight.R
import com.example.stockinsight.data.model.WatchlistItem
import com.example.stockinsight.data.model.stock.FullStockInfo
import com.example.stockinsight.data.socket.SocketManager
import com.example.stockinsight.utils.UiState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import kotlin.coroutines.resume

class StockImpl @Inject constructor(
    private val context: Context,
    private val database: FirebaseFirestore,
    private val socketManager: SocketManager
) : StockRepository {

    init {
        socketManager.addSocket("homepage")
        socketManager.addSocket("watchlist")
        socketManager.addSocket("specific")
        socketManager.addSocket("search")
    }

    override fun getStockInfo(
        symbols: List<String>, range: String, result: (UiState<ArrayList<FullStockInfo>>) -> Unit
    ) {
        requestDataFromSocket(
            "homepage", symbols, range, "stock_request_homepage", "stock_update_homepage", result
        )
    }

    override fun fetchWatchlist(
        userId: String?, result: (UiState<ArrayList<FullStockInfo>>) -> Unit
    ) {
        if (userId == null) {
            result(UiState.Failure(context.getString(R.string.user_id_null)))
            return
        }

        val watchlistRef = database.collection("users").document(userId).collection("watchlist")
        watchlistRef.get().addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                val watchlistItems = querySnapshot.toObjects(WatchlistItem::class.java)
                val symbols = watchlistItems.map { it.symbol }

                if (symbols.isEmpty()) {
                    result(UiState.Success(ArrayList()))
                    return@addOnSuccessListener
                }

                requestDataFromSocket(
                    "watchlist",
                    symbols,
                    "1d",
                    "stock_request_watchlist",
                    "stock_update_watchlist",
                    result
                )
            } else {
                result(UiState.Failure(context.getString(R.string.no_watchlist_items_found)))
            }
        }.addOnFailureListener {
            result(UiState.Failure(it.message ?: context.getString(R.string.an_error_occurred)))
        }
    }

    override suspend fun getStockInfoBySymbol(
        symbol: String, range: String
    ): UiState<FullStockInfo> {
        val data = JSONObject().apply {
            put("symbol", symbol)
            put("range", range)
        }

        socketManager.reopenSocket("specific")
        socketManager.emit("specific", "stock_request_specific", data)

        return suspendCancellableCoroutine { continuation ->
            var isResumed = false
            socketManager.on("specific", "stock_update_specific") { args ->
                if (!isResumed) {
                    isResumed = true
                    if (args.isNotEmpty()) {
                        val stockDataFromServer =
                            Gson().fromJson(args[0].toString(), FullStockInfo::class.java)
                        continuation.resume(UiState.Success(stockDataFromServer))
                    } else {
                        continuation.resume(UiState.Failure(context.getString(R.string.no_data_received_from_server)))
                    }
                }
            }
        }
    }

    override suspend fun addStockToWatchlist(
        userId: String, symbol: String, threshold: Double, lastNotifiedPrice: Double
    ): UiState<String> {
        return try {
            val userRef = database.collection("users").document(userId)
            val watchlistRef = userRef.collection("watchlist").document(symbol)

            val snapshot = watchlistRef.get().await()
            if (!snapshot.exists()) {
                val newWatchlistItem = WatchlistItem(symbol, threshold, lastNotifiedPrice)
                watchlistRef.set(newWatchlistItem).await()
                UiState.Success(context.getString(R.string.stock_added_to_watchlist))
            } else {
                UiState.Failure(context.getString(R.string.stock_already_in_watchlist))
            }
        } catch (e: Exception) {
            UiState.Failure(e.message ?: context.getString(R.string.an_error_occurred))
        }
    }

    override suspend fun removeStockFromWatchlist(userId: String, symbol: String): UiState<String> {
        return try {
            val userRef = database.collection("users").document(userId)
            val watchlistRef = userRef.collection("watchlist").document(symbol)

            val snapshot = watchlistRef.get().await()
            if (snapshot.exists()) {
                watchlistRef.delete().await()
                UiState.Success(context.getString(R.string.stock_removed_from_watchlist))
            } else {
                UiState.Failure(context.getString(R.string.stock_not_in_watchlist))
            }
        } catch (e: Exception) {
            UiState.Failure(e.message ?: context.getString(R.string.an_error_occurred))
        }
    }

    override fun searchStocksByKeyword(
        keyword: String, range: String, result: (UiState<ArrayList<FullStockInfo>>) -> Unit
    ) {
        val data = JSONObject().apply {
            put("keyword", keyword)
            put("range", range)
        }

        // Reopen the socket connection if needed
        socketManager.reopenSocket("search")

        // Emit the search request to the server
        socketManager.emit("search", "stock_request_search", data)

        // Listen for the response from the server
        socketManager.on("search", "stock_update_search") { args ->
            if (args.isNotEmpty()) {
                val type = object : TypeToken<List<FullStockInfo>>() {}.type
                try {
                    val stockDataListFromServer =
                        Gson().fromJson<List<FullStockInfo>>(args[0].toString(), type)
                    Log.d("StockImpl", "Search Stock data: ${stockDataListFromServer.size}")
                    result(UiState.Success(ArrayList(stockDataListFromServer)))
                } catch (e: JsonSyntaxException) {
                    Log.e("StockImpl", "Json parsing error: ${e.localizedMessage}")
                    result(UiState.Failure(context.getString(R.string.error_parsing_data)))
                }
            } else {
                result(UiState.Failure(context.getString(R.string.no_data_received_from_server)))
            }
        }

    }

    override suspend fun updateThreshold(
        userId: String, symbol: String, threshold: Double
    ): UiState<String> {
        return try {
            val userRef = database.collection("users").document(userId)
            val watchlistRef = userRef.collection("watchlist").document(symbol)

            val snapshot = watchlistRef.get().await()
            if (snapshot.exists()) {
                watchlistRef.update("threshold", threshold).await()
                UiState.Success(context.getString(R.string.threshold_updated_successfully))
            } else {
                UiState.Failure(context.getString(R.string.stock_not_in_watchlist))
            }
        } catch (e: Exception) {
            UiState.Failure(e.message ?: context.getString(R.string.an_error_occurred))
        }
    }

    private fun requestDataFromSocket(
        socketName: String,
        symbols: List<String>,
        range: String,
        requestEvent: String,
        updateEvent: String,
        result: (UiState<ArrayList<FullStockInfo>>) -> Unit
    ) {
        val data = JSONObject().apply {
            put("symbols", JSONArray(symbols))
            put("range", range)
        }

        socketManager.reopenSocket(socketName)
        socketManager.emit(socketName, requestEvent, data)

        socketManager.on(socketName, updateEvent) { args ->
            val type = object : TypeToken<List<FullStockInfo>>() {}.type
            val stockDataListFromServer =
                Gson().fromJson<List<FullStockInfo>>(args[0].toString(), type)
            Log.d("StockImpl", "Stock data: ${stockDataListFromServer.size}")
            result(UiState.Success(ArrayList(stockDataListFromServer)))
        }
    }

    override fun closeSocket(name: String) {
        socketManager.closeSocket(name)
    }

    override fun disconnect() {
        socketManager.closeAllSockets()
    }
}
