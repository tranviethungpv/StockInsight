package com.example.stockinsight.ui.fragment.signin

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
import com.example.stockinsight.ui.fragment.OnboardingGetStartedFragment
import com.example.stockinsight.ui.fragment.signup.SignUpFragment
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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
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
                    showDialog("Sign In Successful", "success", requireContext())
                    findNavController().navigate(R.id.action_signInFragment_to_homeFragment)
                }
            }
        }
    }

    private fun validation(): Boolean {
        var isValid = true

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
        return isValid
    }
}