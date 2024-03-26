package com.example.stockinsight.ui.fragment

import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.stockinsight.R
import com.example.stockinsight.data.model.User
import com.example.stockinsight.databinding.FragmentSignUpBinding
import com.example.stockinsight.ui.viewmodel.SignUpViewModel
import com.example.stockinsight.utils.UiState
import com.example.stockinsight.utils.isValidEmail
import dagger.hilt.android.AndroidEntryPoint
import com.example.stockinsight.utils.showDialog

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
                findNavController().navigate(R.id.action_signUpFragment_to_onboardingGetStartedFragment)
            }

            txtConfirmation.setOnClickListener {
                // navigate to next fragment
                findNavController().navigate(R.id.action_signUpFragment_to_signInFragment)
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
                    showDialog(state.message, "warning", requireContext())
                }

                is UiState.Success -> {
                    binding.btnStart.text = getString(R.string.lbl_start)
                    showDialog("Registration Successful", "success", requireContext())
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
            showDialog("Full Name is required", "error", requireContext())
        }

        if (binding.etInputsEmail.text.isNullOrEmpty()) {
            isValid = false
            //binding.etInputsEmail.setBackgroundResource(R.drawable.rectangle_border_light_red_radius_24)
            showDialog("Email is required", "error", requireContext())
        } else {
            if (!binding.etInputsEmail.text.toString().isValidEmail()) {
                isValid = false
                //binding.etInputsEmail.setBackgroundResource(R.drawable.rectangle_border_light_red_radius_24)
                showDialog("Invalid Email", "error", requireContext())
            }
        }
        if (binding.etInputsPassword.text.isNullOrEmpty()) {
            isValid = false
            //binding.etInputsPassword.setBackgroundResource(R.drawable.rectangle_border_light_red_radius_24)
            showDialog("Password is required", "error", requireContext())
        } else {
            if (binding.etInputsPassword.text.toString().length < 8) {
                isValid = false
                //binding.etInputsPassword.setBackgroundResource(R.drawable.rectangle_border_light_red_radius_24)
                showDialog("Password must be at least 8 characters", "error", requireContext())
            }
        }

        if (!isChecked) {
            isValid = false
            showDialog("Please accept the terms and conditions", "warning", requireContext())
        }
        return isValid
    }
}