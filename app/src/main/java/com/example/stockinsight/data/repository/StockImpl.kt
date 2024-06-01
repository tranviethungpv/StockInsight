package com.example.stockinsight.data.repository

import android.util.Log
import com.example.stockinsight.data.model.User
import com.example.stockinsight.data.model.stock.FullStockInfo
import com.example.stockinsight.data.socket.SocketManager
import com.example.stockinsight.utils.UiState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import kotlin.coroutines.resume

class StockImpl @Inject constructor(
    private val database: FirebaseFirestore, private val socketManager: SocketManager
) : StockRepository {

    init {
        socketManager.addSocket("homepage")
        socketManager.addSocket("watchlist")
        socketManager.addSocket("specific")
        socketManager.addSocket("search")
    }

    override fun getStockInfo(
        symbols: List<String>,
        interval: String,
        range: String,
        result: (UiState<ArrayList<FullStockInfo>>) -> Unit
    ) {
        requestDataFromSocket(
            "homepage",
            symbols,
            interval,
            range,
            "stock_request_homepage",
            "stock_update_homepage",
            result
        )
    }

    override fun fetchWatchlist(
        userId: String?, result: (UiState<ArrayList<FullStockInfo>>) -> Unit
    ) {
        if (userId == null) {
            result(UiState.Failure("User ID is null"))
            return
        }

        val userRef = database.collection("users").document(userId)
        userRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val user = documentSnapshot.toObject<User>()
                val watchlist = user?.watchlist ?: emptyList()

                if (watchlist.isEmpty()) {
                    result(UiState.Success(ArrayList()))
                    return@addOnSuccessListener
                }

                requestDataFromSocket(
                    "watchlist",
                    watchlist,
                    "1m",
                    "1d",
                    "stock_request_watchlist",
                    "stock_update_watchlist",
                    result
                )

            } else {
                result(UiState.Failure("User does not exist"))
            }
        }.addOnFailureListener {
            result(UiState.Failure(it.message ?: "An error occurred"))
        }
    }

    override suspend fun getStockInfoBySymbol(
        symbol: String, interval: String, range: String
    ): UiState<FullStockInfo> {
        val data = JSONObject().apply {
            put("symbol", symbol)
            put("interval", interval)
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
                        continuation.resume(UiState.Failure("No data received from server"))
                    }
                }
            }
        }
    }

    override suspend fun addStockToWatchlist(userId: String, symbol: String): UiState<String> {
        return try {
            val userRef = database.collection("users").document(userId)
            val snapshot = userRef.get().await()
            val user = snapshot.toObject<User>()
            if (user != null) {
                val watchlist = user.watchlist?.toMutableList()
                if (!watchlist?.contains(symbol)!!) {
                    watchlist.add(symbol)
                    userRef.update("watchlist", watchlist).await()
                    UiState.Success("Stock added to watchlist")
                } else {
                    UiState.Failure("Stock already in watchlist")
                }
            } else {
                UiState.Failure("User not found")
            }
        } catch (e: Exception) {
            UiState.Failure(e.message ?: "An error occurred")
        }
    }

    override suspend fun removeStockFromWatchlist(userId: String, symbol: String): UiState<String> {
        return try {
            val userRef = database.collection("users").document(userId)
            val snapshot = userRef.get().await()
            val user = snapshot.toObject<User>()
            if (user != null) {
                val watchlist = user.watchlist?.toMutableList()
                if (watchlist?.contains(symbol)!!) {
                    watchlist.remove(symbol)
                    userRef.update("watchlist", watchlist).await()
                    UiState.Success("Stock removed from watchlist")
                } else {
                    UiState.Failure("Stock not in watchlist")
                }
            } else {
                UiState.Failure("User not found")
            }
        } catch (e: Exception) {
            UiState.Failure(e.message ?: "An error occurred")
        }
    }

    override fun searchStocksByKeyword(
        keyword: String,
        interval: String,
        range: String,
        result: (UiState<ArrayList<FullStockInfo>>) -> Unit
    ) {
        val data = JSONObject().apply {
            put("keyword", keyword)
            put("interval", interval)
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
                val stockDataListFromServer =
                    Gson().fromJson<List<FullStockInfo>>(args[0].toString(), type)
                Log.d("StockImpl", "Search Stock data: ${stockDataListFromServer.size}")
                result(UiState.Success(ArrayList(stockDataListFromServer)))
            } else {
                result(UiState.Failure("No data received from server"))
            }
        }
    }

    private fun requestDataFromSocket(
        socketName: String,
        symbols: List<String>,
        interval: String,
        range: String,
        requestEvent: String,
        updateEvent: String,
        result: (UiState<ArrayList<FullStockInfo>>) -> Unit
    ) {
        val data = JSONObject().apply {
            put("symbols", JSONArray(symbols))
            put("interval", interval)
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

    override fun disconnect() {
        socketManager.closeAllSockets()
    }
}
