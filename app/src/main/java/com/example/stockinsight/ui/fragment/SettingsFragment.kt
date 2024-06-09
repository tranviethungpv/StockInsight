package com.example.stockinsight.ui.fragment

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.stockinsight.R
import com.example.stockinsight.data.model.Issue
import com.example.stockinsight.databinding.FragmentSettingsBinding
import com.example.stockinsight.ui.viewmodel.UserViewModel
import com.example.stockinsight.utils.SessionManager
import com.example.stockinsight.utils.UiState
import com.example.stockinsight.utils.showDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private val userViewModel: UserViewModel by viewModels()

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel.sendIssue.observe(viewLifecycleOwner) {
            when (it) {
                is UiState.Loading -> {
                }

                is UiState.Success -> {
                    showDialog(getString(R.string.issue_sent_successfully), "success", requireContext())
                }

                is UiState.Failure -> {
                    showDialog(it.message, "error", requireContext())
                }
            }
        }

        binding.txtSupport.setOnClickListener {
            val dialog = Dialog(requireContext())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_send_issue)

            val window = dialog.window

            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            dialog.findViewById<Button>(R.id.btnCancel).setOnClickListener {
                dialog.dismiss()
            }

            dialog.findViewById<Button>(R.id.btnOk).setOnClickListener {
                val etIssue = dialog.findViewById<EditText>(R.id.etInputIssue)

                if (etIssue.text.toString().isEmpty()) {
                    showDialog(getString(R.string.please_fill_all_fields), "error", requireContext())
                } else {
                    val newIssue = sessionManager.getUserEmail()?.let { email ->
                            Issue(
                                email, etIssue.text.toString(), System.currentTimeMillis()
                            )
                        }
                    if (newIssue != null) {
                        userViewModel.sendIssue(newIssue)
                    }
                    dialog.dismiss()
                }
            }

            dialog.show()
        }

        binding.llTheme.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_themeSettingFragment)
        }

        binding.llAccount.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_accountFragment)
        }

        binding.llLanguage.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_languageSettingFragment)
        }
    }
}