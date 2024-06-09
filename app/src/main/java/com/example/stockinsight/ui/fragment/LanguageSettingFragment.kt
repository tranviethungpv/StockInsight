package com.example.stockinsight.ui.fragment

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.stockinsight.R
import com.example.stockinsight.databinding.FragmentLanguageSettingBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Locale

class LanguageSettingFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentLanguageSettingBinding
    private val sharedPreferences by lazy {
        requireActivity().getSharedPreferences(
            "language_prefs", Context.MODE_PRIVATE
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentLanguageSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnOk.setOnClickListener {
            val selectedLanguage = when (binding.rgChartMode.checkedRadioButtonId) {
                R.id.ro_system_default -> Locale.getDefault().language
                R.id.ro_vietnamese -> "vi"
                R.id.ro_english -> "en"
                else -> Locale.getDefault().language
            }

            sharedPreferences.edit().putString("selected_language", selectedLanguage).apply()
            changeLocale(selectedLanguage)
        }
    }

    @Suppress("DEPRECATION")
    private fun changeLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        context?.resources?.updateConfiguration(config, context?.resources?.displayMetrics)

        // Restart the activity to apply changes
        activity?.recreate()
    }
}