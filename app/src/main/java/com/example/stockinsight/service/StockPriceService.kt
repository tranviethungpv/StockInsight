package com.example.stockinsight.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.stockinsight.R
import com.google.firebase.firestore.FirebaseFirestore
import com.example.stockinsight.data.model.WatchlistItem
import com.example.stockinsight.data.model.stock.FullStockInfo
import com.example.stockinsight.data.socket.SocketManager
import com.example.stockinsight.ui.activity.MainActivity
import com.example.stockinsight.utils.SessionManager
import com.example.stockinsight.utils.applyLocale
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import kotlin.math.abs

@AndroidEntryPoint
class StockPriceService : Service() {

    @Inject
    lateinit var firestore: FirebaseFirestore

    @Inject
    lateinit var socketManager: SocketManager

    @Inject
    lateinit var sessionManager: SessionManager

    private val channelId = "StockPriceServiceChannel"
    private val thresholds = mutableMapOf<String, Double>()
    private val lastPriceChange = mutableMapOf<String, Double>()
    private lateinit var updatedContext: Context

    override fun onCreate() {
        super.onCreate()

        updatedContext = applyLocale(this)

        createNotificationChannel()
        startForeground(
            1, createNotification(updatedContext.getString(R.string.stock_price_service_running))
        )
        socketManager.addSocket("notification")
        Log.d("StockPriceService", "Service created and socket added")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        updatedContext = applyLocale(this)

        val userId = sessionManager.getUserId()
        Log.d("StockPriceService", "onStartCommand called with userId: $userId")

        fetchWatchlistForNotification(userId) { watchlist ->
            if (watchlist.isEmpty()) {
                Log.d("StockPriceService", "Watchlist is empty, stopping service.")
                stopSelf()
                return@fetchWatchlistForNotification
            }

            Log.d("StockPriceService", "Watchlist data: ${watchlist.size}")
            val symbols = watchlist.map { it.quoteInfo.symbol }
            requestDataFromSocket(
                "notification",
                symbols,
                "1d",
                "stock_request_notification",
                "stock_update_notification"
            ) {
                handleStockData(it)
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            channelId, "Stock Price Service Channel", NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
        Log.d("StockPriceService", "Notification channel created")
    }

    private fun createNotification(content: String): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(updatedContext.getString(R.string.app_name)).setContentText(content)
            .setSmallIcon(R.mipmap.ic_launcher).setContentIntent(pendingIntent).build()
    }

    private fun fetchWatchlistForNotification(
        userId: String?, result: (ArrayList<FullStockInfo>) -> Unit
    ) {
        Log.d("StockPriceService", "Fetching watchlist for userId: $userId")

        if (userId == null) {
            Log.e("StockPriceService", "User ID is null")
            result(ArrayList())
            return
        }

        val watchlistRef = firestore.collection("users").document(userId).collection("watchlist")
        watchlistRef.get().addOnSuccessListener { querySnapshot ->
            Log.d(
                "StockPriceService",
                "Watchlist query success, documents found: ${querySnapshot.documents.size}"
            )
            if (!querySnapshot.isEmpty) {
                val watchlistItems = querySnapshot.toObjects(WatchlistItem::class.java)
                val symbols = watchlistItems.map { it.symbol }

                watchlistItems.forEach {
                    thresholds[it.symbol] = it.threshold
                }

                if (symbols.isEmpty()) {
                    Log.d("StockPriceService", "No symbols in watchlist.")
                    result(ArrayList())
                    return@addOnSuccessListener
                }

                requestDataFromSocket(
                    "watchlist",
                    symbols,
                    "1d",
                    "stock_request_notification",
                    "stock_update_notification",
                    result
                )
            } else {
                Log.d("StockPriceService", "Watchlist is empty.")
                result(ArrayList())
            }
        }.addOnFailureListener { exception ->
            Log.e("StockPriceService", "Error fetching watchlist: ${exception.message}")
            result(ArrayList())
        }
    }

    private fun requestDataFromSocket(
        socketName: String,
        symbols: List<String>,
        range: String,
        requestEvent: String,
        updateEvent: String,
        result: (ArrayList<FullStockInfo>) -> Unit
    ) {
        val data = JSONObject().apply {
            put("symbols", JSONArray(symbols))
            put("range", range)
        }

        socketManager.reopenSocket(socketName)
        Log.d("StockPriceService", "Socket reopened for $socketName")
        socketManager.emit(socketName, requestEvent, data)
        Log.d("StockPriceService", "Emitted $requestEvent for symbols: $symbols")

        socketManager.on(socketName, updateEvent) { args ->
            Log.d("StockPriceService", "Received $updateEvent: ${args[0]}")
            val type = object : TypeToken<List<FullStockInfo>>() {}.type
            val stockDataListFromServer =
                Gson().fromJson<List<FullStockInfo>>(args[0].toString(), type)
            result(ArrayList(stockDataListFromServer))
        }
    }

    private fun handleStockData(stockDataList: ArrayList<FullStockInfo>) {
        for (stockData in stockDataList) {
            val symbol = stockData.quoteInfo.symbol
            val currentPrice: Double = if (stockData.quoteInfo.currentPrice.toInt() != 0) {
                stockData.quoteInfo.currentPrice
            } else {
                stockData.quoteInfo.today
            }
            val closePrice = stockData.quoteInfo.previousClose
            val threshold = thresholds[symbol]

            Log.d(
                "StockPriceService",
                "Handling stock data for $symbol: currentPrice=$currentPrice, closePrice=$closePrice, threshold=$threshold"
            )

            if (threshold != null && threshold != 0.0) {
                val priceChange = currentPrice - closePrice
                val priceChangePercent = abs(priceChange) / closePrice * 100

                // Check if the price change percentage exceeds the threshold and the price change is different from last recorded
                if (priceChangePercent >= threshold && priceChange != lastPriceChange[symbol]) {
                    Log.d(
                        "StockPriceService",
                        "Price change for $symbol exceeds threshold. Sending notification."
                    )
                    sendNotification(symbol, currentPrice, closePrice)
                    // Update lastPriceChange map with new change
                    lastPriceChange[symbol] = priceChange
                } else {
                    Log.d(
                        "StockPriceService",
                        "No significant change or same change as before. No notification sent for $symbol."
                    )
                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun sendNotification(symbol: String, newPrice: Double, closePrice: Double) {
        val priceChange = newPrice - closePrice
        val percentPriceChange = priceChange / closePrice * 100
        val formattedNewPrice = String.format("%.2f", newPrice)
        val formattedPriceChange = String.format("%.2f", priceChange)
        val formattedPercentPriceChange = String.format("%.2f", percentPriceChange)

        val notification = createNotification(
            updatedContext.getString(R.string.the_price_of) + " " + symbol + " " + updatedContext.getString(
                R.string.has_changed_to
            ) + " " + formattedNewPrice + updatedContext.getString(R.string.dot) + " " + updatedContext.getString(
                R.string.change
            ) + " " + formattedPriceChange + " (" + formattedPercentPriceChange + "%)"
        )
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(symbol.hashCode(), notification)
        Log.d(
            "StockPriceService",
            "Sent notification for $symbol: newPrice=$formattedNewPrice, change=$formattedPriceChange"
        )
    }
}