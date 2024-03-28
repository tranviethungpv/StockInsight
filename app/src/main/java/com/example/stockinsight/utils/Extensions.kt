package com.example.stockinsight.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.stockinsight.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

fun Fragment.toast(msg: String?){
    Toast.makeText(requireContext(),msg,Toast.LENGTH_LONG).show()
}

fun String.isValidEmail() =
    isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()

// Function to show Dialog
fun showDialog(message: String, type: String, context: Context) {
    val dialog = Dialog(context)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(R.layout.notice_dialog)

    val window = dialog.window ?: return

    window.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
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

fun drawLineChart(chart: LineChart, entries: List<Entry>, type: String) {
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