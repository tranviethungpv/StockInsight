package com.example.stockinsight.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import com.example.stockinsight.databinding.FragmentThemeSettingBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ThemeSettingFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentThemeSettingBinding
    private val sharedPreferences by lazy {
        requireActivity().getSharedPreferences(
            "theme_prefs", Context.MODE_PRIVATE
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentThemeSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var selectedThemeMode =
            sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        binding.roSystemDefault.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedThemeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        }

        binding.roLight.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedThemeMode = AppCompatDelegate.MODE_NIGHT_NO
            }
        }

        binding.roDark.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedThemeMode = AppCompatDelegate.MODE_NIGHT_YES
            }
        }

        when (selectedThemeMode) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> binding.roSystemDefault.isChecked = true
            AppCompatDelegate.MODE_NIGHT_NO -> binding.roLight.isChecked = true
            AppCompatDelegate.MODE_NIGHT_YES -> binding.roDark.isChecked = true
        }

        binding.btnOk.setOnClickListener {
            sharedPreferences.edit().putInt("theme_mode", selectedThemeMode).apply()
            AppCompatDelegate.setDefaultNightMode(selectedThemeMode)
            dismiss()
        }
    }
}