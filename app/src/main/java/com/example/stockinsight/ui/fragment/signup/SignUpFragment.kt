package com.example.stockinsight.ui.fragment.signup

import android.app.Dialog
import android.graphics.Color.*
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import com.example.stockinsight.ui.fragment.OnboardingGetStartedFragment
import com.example.stockinsight.R
import com.example.stockinsight.data.model.User
import com.example.stockinsight.databinding.FragmentSignUpBinding
import com.example.stockinsight.utils.UiState
import com.example.stockinsight.utils.isValidEmail
import com.example.stockinsight.utils.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpFragment : Fragment() {
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SignUpViewModel by viewModels()
    private var isChecked = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            etInputsFullName.setOnFocusChangeListener { _, hasFocus ->
                onFocusChange(
                    hasFocus, linearColumnFullName, txtTitleFullName, etInputsFullName, "Full Name"
                )
            }

            etInputsEmail.setOnFocusChangeListener { _, hasFocus ->
                onFocusChange(hasFocus, linearColumnEmail, txtTitleEmail, etInputsEmail, "Email")
            }

            etInputsPassword.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    relativeColumnPassword.setBackgroundResource(R.drawable.rectangle_border_light_green_500_radius_24)
                    txtTitlePassword.visibility = View.VISIBLE
                    etInputsPassword.hint = ""
                } else {
                    relativeColumnPassword.setBackgroundResource(R.drawable.rectangle_border_radius_24)
                    txtTitlePassword.visibility = View.GONE
                    etInputsPassword.hint = "Password"
                }
            }

            etInputsFullName.addTextChangedListener {
                onTextChange(etInputsFullName, linearColumnFullName, txtTitleFullName, "Full Name")
            }

            etInputsEmail.addTextChangedListener {
                onTextChange(etInputsEmail, linearColumnEmail, txtTitleEmail, "Email")
            }

            etInputsPassword.addTextChangedListener {
                if (etInputsPassword.text.isNotEmpty()) {
                    relativeColumnPassword.setBackgroundResource(R.drawable.rectangle_border_light_green_500_radius_24)
                    txtTitlePassword.visibility = View.VISIBLE
                    etInputsPassword.hint = ""
                } else {
                    relativeColumnPassword.setBackgroundResource(R.drawable.rectangle_border_radius_24)
                    txtTitlePassword.visibility = View.GONE
                    etInputsPassword.hint = "Password"
                }
            }

            var hidePassword = true
            btnShowPassword.setImageResource(R.drawable.img_eye_slash)
            btnShowPassword.setOnClickListener {
                hidePassword = !hidePassword
                btnShowPassword.setImageResource(if (hidePassword) R.drawable.img_eye_slash else R.drawable.img_eye)

                // change etInputsPassword input type to text or password
                etInputsPassword.inputType =
                    if (hidePassword) InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD else InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            }

            btnArrowLeft.setOnClickListener {
                // navigate to previous fragment
                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                transaction.replace(R.id.container, OnboardingGetStartedFragment())
                transaction.addToBackStack(null)
                transaction.commit()
            }

            txtConfirmation.setOnClickListener {
                // navigate to next fragment
                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                transaction.replace(R.id.container, SignUpFragment())
                transaction.addToBackStack(null)
                transaction.commit()
            }

            btnCheck.setOnClickListener {
                isChecked = !isChecked
                if (isChecked) {
                    btnCheck.setBackgroundResource(R.drawable.rectangle_bg_light_green_500_radius_tl_24_tr_24_bl_4_br_4)
                    btnCheck.setImageResource(R.drawable.img_check)
                } else {
                    btnCheck.setBackgroundResource(R.drawable.rectangle_bg_white_radius_tl_24_tr_24_bl_4_br_4)
                    btnCheck.setImageResource(0)
                }
            }

            observer()

            btnStart.setOnClickListener {
                if (validation()) {
                    viewModel.registerUser(
                        binding.etInputsPassword.text.toString(), getUserObject()
                    )
                }
            }
        }
    }

    private fun onFocusChange(
        hasFocus: Boolean,
        linearColumn: LinearLayout,
        txtTitle: TextView,
        etInputs: EditText,
        hint: String

    ) {
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
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observer() {
        viewModel.register.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.btnStart.text = getString(R.string.please_wait)
                }

                is UiState.Failure -> {
                    binding.btnStart.text = getString(R.string.lbl_start)
                    showErrorDialog(state.message, "warning")
                }

                is UiState.Success -> {
                    binding.btnStart.text = getString(R.string.lbl_start)
                    showErrorDialog("Registration Successful", "success")
//                    findNavController().navigate(R.id.action_registerFragment_to_home_navigation)
                }
            }
        }
    }

    private fun getUserObject(): User {
        return User(
            id = "",
            username = binding.etInputsFullName.text.toString(),
            male = null,
            dateOfBirth = null,
            address = null,
            phoneNumber = null,
            email = binding.etInputsEmail.text.toString(),
            profileImageUrl = null,
            isVerified = false,
            provider = null,
            providerId = null,
            token = null
        )
    }

    private fun validation(): Boolean {
        var isValid = true

        if (binding.etInputsFullName.text.isNullOrEmpty()) {
            isValid = false
            //binding.etInputsFullName.setBackgroundResource(R.drawable.rectangle_border_light_red_radius_24)
            showErrorDialog("Full Name is required", "error")
        }

        if (binding.etInputsEmail.text.isNullOrEmpty()) {
            isValid = false
            //binding.etInputsEmail.setBackgroundResource(R.drawable.rectangle_border_light_red_radius_24)
            showErrorDialog("Email is required", "error")
        } else {
            if (!binding.etInputsEmail.text.toString().isValidEmail()) {
                isValid = false
                //binding.etInputsEmail.setBackgroundResource(R.drawable.rectangle_border_light_red_radius_24)
                showErrorDialog("Invalid Email", "error")
            }
        }
        if (binding.etInputsPassword.text.isNullOrEmpty()) {
            isValid = false
            //binding.etInputsPassword.setBackgroundResource(R.drawable.rectangle_border_light_red_radius_24)
            showErrorDialog("Password is required", "error")
        } else {
            if (binding.etInputsPassword.text.toString().length < 8) {
                isValid = false
                //binding.etInputsPassword.setBackgroundResource(R.drawable.rectangle_border_light_red_radius_24)
                showErrorDialog("Password must be at least 8 characters", "error")
            }
        }

        if (!isChecked) {
            isValid = false
            showErrorDialog("Please accept the terms and conditions", "warning")
        }
        return isValid
    }

    // Function on text change
    private fun onTextChange(
        etInputs: EditText,
        linearColumn: LinearLayout,
        txtTitle: TextView,
        hint: String
    ) {
        if (etInputs.text.isNotEmpty()) {
            linearColumn.setBackgroundResource(R.drawable.rectangle_border_light_green_500_radius_24)
            txtTitle.visibility = View.VISIBLE
            etInputs.hint = ""
        } else {
            linearColumn.setBackgroundResource(R.drawable.rectangle_border_radius_24)
            txtTitle.visibility = View.GONE
            etInputs.hint = hint
        }
    }

    // Function to show Error Dialog
    private fun showErrorDialog(message: String, type: String) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.notice_dialog)

        val window = dialog.window ?: return

        window.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window.setBackgroundDrawable(ColorDrawable(TRANSPARENT))

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
}