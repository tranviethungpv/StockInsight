package com.example.stockinsight.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RawRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.stockinsight.R
import com.example.stockinsight.service.StockPriceService
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun String.isValidEmail() =
    isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()

// Function to show Dialog
fun showDialog(message: String, type: String, context: Context) {
    val dialog = Dialog(context)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(R.layout.notice_dialog)

    val window = dialog.window ?: return

    window.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    )
    window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    val txtMessage = dialog.findViewById<TextView>(R.id.textMessage)
    txtMessage.text = message

    val imgError = dialog.findViewById<ImageView>(R.id.imgStatus)
    when (type) {
        "error" -> {
            imgError.setImageResource(R.drawable.img_error)
        }

        "warning" -> {
            imgError.setImageResource(R.drawable.img_warning)
        }

        "success" -> {
            imgError.setImageResource(R.drawable.img_success)
        }
    }

    val btnError = dialog.findViewById<Button>(R.id.btnOk)
    btnError.setOnClickListener {
        dialog.dismiss()
    }

    dialog.show()
}

fun drawSimpleLineChart(chart: LineChart, entries: List<Entry>, type: String) {
    val dataSet = LineDataSet(entries, "Label") // add entries to dataset
    dataSet.setDrawValues(false)
    dataSet.setDrawCircles(false)
    dataSet.setDrawFilled(true)
    dataSet.lineWidth = 2f

    if (type == "down") {
        dataSet.color = ContextCompat.getColor(chart.context, R.color.red_700)
        dataSet.fillColor = ContextCompat.getColor(chart.context, R.color.red_100)
    } else if (type == "up") {
        dataSet.color = ContextCompat.getColor(chart.context, R.color.light_green_500)
        dataSet.fillColor = ContextCompat.getColor(chart.context, R.color.light_green_300)
    }

    val lineData = LineData(dataSet)
    chart.data = lineData

    // Hide all labels, grid lines, and other chart decorations
    chart.axisLeft.isEnabled = false
    chart.axisRight.isEnabled = false
    chart.xAxis.isEnabled = false
    chart.description.isEnabled = false
    chart.legend.isEnabled = false
    chart.setDrawGridBackground(false)
    chart.setDrawBorders(false)
    chart.setDrawMarkers(false)
    chart.setTouchEnabled(false)
    chart.isDragEnabled = false
    chart.setScaleEnabled(false)
    chart.setPinchZoom(false)
    chart.isDoubleTapToZoomEnabled = false
    chart.isHighlightPerDragEnabled = false

    chart.invalidate()
}

fun drawFullLineChart(chart: LineChart, entries: List<Entry>, type: String, period: String) {
    val dataSet = LineDataSet(entries, "Label") // add entries to dataset
    dataSet.setDrawValues(false)
    dataSet.setDrawCircles(false)
    dataSet.setDrawFilled(true)
    dataSet.lineWidth = 2f

    if (type == "down") {
        dataSet.color = ContextCompat.getColor(chart.context, R.color.red_700)
        dataSet.fillColor = ContextCompat.getColor(chart.context, R.color.red_100)
    } else if (type == "up") {
        dataSet.color = ContextCompat.getColor(chart.context, R.color.light_green_500)
        dataSet.fillColor = ContextCompat.getColor(chart.context, R.color.light_green_300)
    }

    val lineData = LineData(dataSet)
    chart.data = lineData

    // Configure the left axis (Y axis)
    val leftAxis: YAxis = chart.axisLeft
    leftAxis.setDrawGridLines(true)
    leftAxis.setDrawAxisLine(true)
    leftAxis.textColor = ContextCompat.getColor(chart.context, R.color.text_color)

    // Disable the right axis (Y axis)
    chart.axisRight.isEnabled = false

    // Configure the bottom axis (X axis)
    val xAxis: XAxis = chart.xAxis
    xAxis.position = XAxis.XAxisPosition.BOTTOM
    xAxis.setDrawGridLines(true)
    xAxis.setDrawAxisLine(true)
    xAxis.textColor = ContextCompat.getColor(chart.context, R.color.text_color)
    xAxis.granularity = 1f // one unit interval
    xAxis.setLabelCount(5, true) // Show only 5 values evenly distributed

    // Customize the x-axis labels based on the period
    xAxis.valueFormatter = object : ValueFormatter() {
        @SuppressLint("ConstantLocale")
        private val dateFormat = when (period) {
            "1d" -> SimpleDateFormat("HH:mm", Locale.getDefault()) // Display hours
            "5d", "1mo" -> SimpleDateFormat("MM/dd", Locale.getDefault()) // Display day
            "3mo", "6mo", "ytd", "1y", "2y" -> SimpleDateFormat(
                "MMM", Locale.getDefault()
            ) // Display month
            else -> SimpleDateFormat("yyyy", Locale.getDefault()) // Display year for 5y, 10y, all
        }

        override fun getFormattedValue(value: Float): String {
            val date = Date(value.toLong())
            return dateFormat.format(date)
        }
    }

    // Enable chart decorations
    chart.description.isEnabled = false
    chart.legend.isEnabled = false
    chart.setDrawGridBackground(false)
    chart.setDrawBorders(false)
    chart.setTouchEnabled(true)
    chart.isDragEnabled = true
    chart.setScaleEnabled(true)
    chart.setPinchZoom(true)
    chart.isDoubleTapToZoomEnabled = true
    chart.isHighlightPerDragEnabled = true

    // Add animation
    // chart.animateX(1500)

    // Refresh the chart
    chart.invalidate()
}

fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
    return when {
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        else -> false
    }
}

@SuppressLint("DefaultLocale")
fun formatNumber(number: Long): String {
    return when {
        number >= 1_000_000_000_000 -> String.format("%.2fT", number / 1_000_000_000_000.0)
        number >= 1_000_000_000 -> String.format("%.2fB", number / 1_000_000_000.0)
        number >= 1_000_000 -> String.format("%.2fM", number / 1_000_000.0)
        number >= 1_000 -> String.format("%.2fK", number / 1_000.0)
        else -> number.toString()
    }
}

fun requestManyPermissions(activity: Activity, permissions: Array<String>, requestCode: Int) {
    val notGrantedPermissions = permissions.filter {
        ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
    }

    if (notGrantedPermissions.isNotEmpty()) {
        ActivityCompat.requestPermissions(
            activity, notGrantedPermissions.toTypedArray(), requestCode
        )
    }
}

fun startStockPriceService(context: Context) {
    val serviceIntent = Intent(context, StockPriceService::class.java)
    context.startForegroundService(serviceIntent)
}

fun applyLocale(context: Context): Context {
    val prefs = context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
    var selectedLanguage = prefs.getString("selected_language", "SYSTEM_DEFAULT")

    if (selectedLanguage == "SYSTEM_DEFAULT") {
        selectedLanguage = Locale.getDefault().language
    }

    val locale = Locale(selectedLanguage!!)
    Locale.setDefault(locale)

    val config = Configuration(context.resources.configuration)
    config.setLocale(locale)

    return context.createConfigurationContext(config)
}

fun getRandomItemsFromFile(context: Context, @RawRes resourceId: Int, itemCount: Int = 10): List<String> {
    val inputStream = context.resources.openRawResource(resourceId)
    val reader = BufferedReader(InputStreamReader(inputStream))

    val items = mutableListOf<String>()
    reader.useLines { lines ->
        lines.forEach { line ->
            if (line.isNotBlank()) {
                items.add(line)
            }
        }
    }

    return if (items.size <= itemCount) {
        items
    } else {
        items.shuffled().take(itemCount)
    }
}