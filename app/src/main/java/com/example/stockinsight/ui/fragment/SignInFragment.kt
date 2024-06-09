package com.example.stockinsight.ui.fragment

import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.stockinsight.R
import com.example.stockinsight.databinding.FragmentSignInBinding
import com.example.stockinsight.ui.viewmodel.SignInViewModel
import com.example.stockinsight.utils.UiState
import com.example.stockinsight.utils.isValidEmail
import com.example.stockinsight.utils.showDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInFragment : Fragment() {
    private var _binding: FragmentSignInBinding? = null
    private val viewModel: SignInViewModel by viewModels()
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            etInputsEmail.setOnFocusChangeListener { _, hasFocus ->
                onFocusChange(
                    hasFocus,
                    linearColumnEmail,
                    txtTitleEmail,
                    etInputsEmail,
                    getString(R.string.lbl_email_address)
                )
            }

            etInputsPassword.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    relativeColumnPassword.setBackgroundResource(R.drawable.rectangle_border_light_green_500_radius_24)
                    txtTitlePassword.visibility = View.VISIBLE
                    etInputsPassword.hint = getString(R.string.blank)
                } else {
                    relativeColumnPassword.setBackgroundResource(R.drawable.rectangle_border_radius_24)
                    txtTitlePassword.visibility = View.GONE
                    etInputsPassword.hint = getString(R.string.lbl_password)
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
                findNavController().navigate(R.id.action_signInFragment_to_onboardingGetStartedFragment)
            }

            txtConfirmation.setOnClickListener {
                // navigate to next fragment
                findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
            }

            observer()

            btnStart.setOnClickListener {
                if (validation()) {
                    viewModel.signInUser(
                        binding.etInputsEmail.text.toString(),
                        binding.etInputsPassword.text.toString(),
                    )
                }
            }

            tvForgotPassword.setOnClickListener {
                if (binding.etInputsEmail.text.isNullOrEmpty()) {
                    showDialog(getString(R.string.email_is_required), "error", requireContext())
                    return@setOnClickListener
                }
                viewModel.forgotPassword(binding.etInputsEmail.text.toString())
            }
        }
    }

    @Suppress("SameParameterValue")
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
            etInputs.hint = getString(R.string.blank)
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
        viewModel.signIn.observe(viewLifecycleOwner) { state ->
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
                    showDialog(getString(R.string.sign_in_successful), "success", requireContext())
                    findNavController().navigate(R.id.action_signInFragment_to_homeFragment)
                }
            }
        }

        viewModel.forgotPassword.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                }

                is UiState.Failure -> {
                    showDialog(state.message, "warning", requireContext())
                }

                is UiState.Success -> {
                    showDialog(
                        getString(R.string.password_reset_email_sent_successfully),
                        "success",
                        requireContext()
                    )
                }
            }
        }
    }

    private fun validation(): Boolean {
        var isValid = true

        if (binding.etInputsEmail.text.isNullOrEmpty()) {
            isValid = false
            //binding.etInputsEmail.setBackgroundResource(R.drawable.rectangle_border_light_red_radius_24)
            showDialog(getString(R.string.email_is_required), "error", requireContext())
        } else {
            if (!binding.etInputsEmail.text.toString().isValidEmail()) {
                isValid = false
                //binding.etInputsEmail.setBackgroundResource(R.drawable.rectangle_border_light_red_radius_24)
                showDialog(getString(R.string.invalid_email), "error", requireContext())
            }
        }
        if (binding.etInputsPassword.text.isNullOrEmpty()) {
            isValid = false
            //binding.etInputsPassword.setBackgroundResource(R.drawable.rectangle_border_light_red_radius_24)
            showDialog(getString(R.string.password_is_required), "error", requireContext())
        } else {
            if (binding.etInputsPassword.text.toString().length < 8) {
                isValid = false
                //binding.etInputsPassword.setBackgroundResource(R.drawable.rectangle_border_light_red_radius_24)
                showDialog(
                    getString(R.string.password_must_be_at_least_8_characters),
                    "error",
                    requireContext()
                )
            }
        }
        return isValid
    }
}