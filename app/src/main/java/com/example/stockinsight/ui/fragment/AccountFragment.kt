package com.example.stockinsight.ui.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.stockinsight.R
import com.example.stockinsight.databinding.FragmentAccountBinding
import com.example.stockinsight.ui.viewmodel.SignInViewModel
import com.example.stockinsight.ui.viewmodel.UserViewModel
import com.example.stockinsight.utils.SessionManager
import com.example.stockinsight.utils.UiState
import com.example.stockinsight.utils.showDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AccountFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentAccountBinding
    private val userViewModel: UserViewModel by viewModels()
    private val signInViewModel: SignInViewModel by viewModels()

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel.fetchUser()
        userViewModel.fetchUser.observe(viewLifecycleOwner) {
            when (it) {
                is UiState.Loading -> {
                }

                is UiState.Success -> {
                    binding.tvUsername.text = it.data.username
                    binding.tvEmail.text = it.data.email
                }

                is UiState.Failure -> {
                    showDialog(it.message, "error", requireContext())
                }
            }
        }

        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext()).setTitle(getString(R.string.logout))
                .setMessage(getString(R.string.logout_message))
                .setPositiveButton(getString(R.string.yes)) { _, _ ->
                    userViewModel.signOut()
                    sessionManager.clearLoginSession()

                    // Verify that the session is cleared
                    if (!sessionManager.isLoggedIn()) {
                        Log.d("Logout", "Session cleared successfully")
                    } else {
                        Log.d("Logout", "Failed to clear session")
                    }

                    // Navigate to home fragment only if session is cleared
                    if (!sessionManager.isLoggedIn()) {
                        findNavController().navigate(R.id.action_accountFragment_to_homeFragment)
                        dismiss()
                    }
                }.setNegativeButton(getString(R.string.no), null).show()
        }

        binding.btnChangePassword.setOnClickListener {
            val dialog = Dialog(requireContext())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_change_password)

            val window = dialog.window

            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val etCurrentPassword = dialog.findViewById<EditText>(R.id.etInputCurrentPassword)
            val etNewPassword = dialog.findViewById<EditText>(R.id.etInputNewPassword)
            val etRepeatPassword = dialog.findViewById<EditText>(R.id.etInputRepeatPassword)

            dialog.findViewById<Button>(R.id.btnCancel).setOnClickListener {
                dialog.dismiss()
            }

            dialog.findViewById<Button>(R.id.btnOk).setOnClickListener {
                val currentPassword = etCurrentPassword.text.toString()
                val newPassword = etNewPassword.text.toString()
                val repeatPassword = etRepeatPassword.text.toString()

                if (newPassword != repeatPassword) {
                    showDialog(
                        getString(R.string.new_password_and_repeat_password_do_not_match),
                        "error",
                        requireContext()
                    )
                } else if (newPassword.length < 8) {
                    showDialog(
                        getString(R.string.new_password_must_be_at_least_8_characters_long),
                        "error",
                        requireContext()
                    )
                } else {
                    sessionManager.getUserEmail()?.let { userEmail ->
                        signInViewModel.checkPassword(
                            userEmail, currentPassword
                        )
                    }
                    signInViewModel.checkPassword.observe(viewLifecycleOwner) { result ->
                        when (result) {
                            is UiState.Loading -> {
                            }

                            is UiState.Success -> {
                                if (result.data) {
                                    sessionManager.getUserEmail()?.let { userEmail ->
                                        userViewModel.changePassword(
                                            userEmail, newPassword
                                        )
                                    }
                                    userViewModel.changePassword.observe(viewLifecycleOwner) {
                                        when (it) {
                                            is UiState.Loading -> {
                                            }

                                            is UiState.Success -> {
                                                showDialog(it.data, "success", requireContext())
                                                dialog.dismiss()
                                            }

                                            is UiState.Failure -> {
                                                showDialog(it.message, "error", requireContext())
                                            }
                                        }
                                    }
                                } else {
                                    showDialog(
                                        getString(R.string.current_password_is_incorrect),
                                        "error",
                                        requireContext()
                                    )
                                }
                            }

                            is UiState.Failure -> {
                                showDialog(result.message, "error", requireContext())
                            }
                        }
                    }
                    dialog.dismiss()
                }
            }
            dialog.show()
        }
    }
}