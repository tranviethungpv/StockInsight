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

        var selectedLanguage = sharedPreferences.getString("selected_language", "SYSTEM_DEFAULT")
        when (selectedLanguage) {
            "SYSTEM_DEFAULT" -> binding.roSystemDefault.isChecked = true
            "vi" -> binding.roVietnamese.isChecked = true
            "en" -> binding.roEnglish.isChecked = true
            "ja" -> binding.roJapanese.isChecked = true
            "ko" -> binding.roKorean.isChecked = true
            "zh" -> binding.roChinese.isChecked = true
        }

        binding.btnOk.setOnClickListener {
            selectedLanguage = when (binding.rgChartMode.checkedRadioButtonId) {
                R.id.ro_system_default -> "SYSTEM_DEFAULT"
                R.id.ro_vietnamese -> "vi"
                R.id.ro_english -> "en"
                R.id.ro_japanese -> "ja"
                R.id.ro_korean -> "ko"
                R.id.ro_chinese -> "zh"
                else -> "SYSTEM_DEFAULT"
            }

            sharedPreferences.edit().putString("selected_language", selectedLanguage).apply()
            changeLocale(selectedLanguage!!)
        }
    }

    @Suppress("DEPRECATION")
    private fun changeLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        requireActivity().applicationContext.resources?.updateConfiguration(
            config, context?.resources?.displayMetrics
        )

        // Restart the activity to apply changes
        activity?.recreate()
    }
}