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

        binding.roSystemDefault.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sharedPreferences.edit()
                    .putInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM).apply()
            }
        }

        binding.roLight.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sharedPreferences.edit().putInt("theme_mode", AppCompatDelegate.MODE_NIGHT_NO)
                    .apply()
            }
        }

        binding.roDark.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sharedPreferences.edit().putInt("theme_mode", AppCompatDelegate.MODE_NIGHT_YES)
                    .apply()
            }
        }

        binding.btnOk.setOnClickListener {
            val themeMode =
                sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            AppCompatDelegate.setDefaultNightMode(themeMode)
            dismiss()
        }
    }
}