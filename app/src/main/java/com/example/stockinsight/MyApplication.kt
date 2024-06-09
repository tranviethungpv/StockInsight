package com.example.stockinsight

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale

@Suppress("DEPRECATION")
@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val sharedPreferences = getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
        val language =
            sharedPreferences.getString("selected_language", Locale.getDefault().language)
        val locale = Locale(language!!)
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)

        val res: Resources = resources
        res.configuration.setLocale(locale)
        res.updateConfiguration(res.configuration, res.displayMetrics)
    }
}