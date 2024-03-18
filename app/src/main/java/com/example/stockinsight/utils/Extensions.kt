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
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import com.example.stockinsight.R

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