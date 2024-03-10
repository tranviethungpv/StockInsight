package com.example.stockinsight

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView

class SignUpFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign_up, container, false)

        val linearColumnFullName = view.findViewById<LinearLayout>(R.id.linearColumnFullName)
        val linearColumnEmail = view.findViewById<LinearLayout>(R.id.linearColumnEmail)
        val linearColumnPassword = view.findViewById<LinearLayout>(R.id.linearColumnPassword)

        val txtTitleFullName = view.findViewById<TextView>(R.id.txtTitleFullName)
        val txtTitleEmail = view.findViewById<TextView>(R.id.txtTitleEmail)
        val txtTitlePassword = view.findViewById<TextView>(R.id.txtTitlePassword)

        val etInputsFullName = view.findViewById<EditText>(R.id.etInputsFullName)
        val etInputsEmail = view.findViewById<EditText>(R.id.etInputsEmail)
        val etInputsPassword = view.findViewById<EditText>(R.id.etInputsPassword)

        val btnArrowLeft = view.findViewById<ImageButton>(R.id.btnArrowLeft)

        etInputsFullName.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            onFocusChange(view, hasFocus, linearColumnFullName, txtTitleFullName, etInputsFullName, "Full Name")
        }

        etInputsEmail.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            onFocusChange(view, hasFocus, linearColumnEmail, txtTitleEmail, etInputsEmail, "Email")
        }

        etInputsPassword.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            onFocusChange(view, hasFocus, linearColumnPassword, txtTitlePassword, etInputsPassword, "Password")
        }

        btnArrowLeft.setOnClickListener {
            // navigate to previous fragment
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.container, OnboardingGetStartedFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        return view
    }

    private fun onFocusChange(view: View, hasFocus: Boolean, linearColumn: LinearLayout, txtTitle: TextView, etInputs: EditText, hint: String) {
        if (hasFocus) {
            linearColumn.setBackgroundResource(R.drawable.rectangle_border_light_green_500_radius_24)
            txtTitle.visibility = View.VISIBLE
            etInputs.hint = ""
        } else {
            linearColumn.setBackgroundResource(R.drawable.rectangle_border_radius_24)
            txtTitle.visibility = View.GONE
            etInputs.hint = hint
        }
    }
}