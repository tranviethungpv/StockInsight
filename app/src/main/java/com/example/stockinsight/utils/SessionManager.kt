package com.example.stockinsight.utils

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(private val context: Context) {

    companion object {
        private const val PREF_NAME = "com.example.stockinsight.PREF_NAME"
        private const val IS_LOGGED_IN = "IS_LOGGED_IN"
    }

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveLoginSession() {
        val editor = sharedPreferences.edit()
        editor.putBoolean(IS_LOGGED_IN, true)
        editor.apply()
    }

    fun clearLoginSession() {
        val editor = sharedPreferences.edit()
        editor.putBoolean(IS_LOGGED_IN, false)
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(IS_LOGGED_IN, false)
    }
}