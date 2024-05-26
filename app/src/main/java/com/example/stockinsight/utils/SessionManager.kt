package com.example.stockinsight.utils

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(private val context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "com.example.stockinsight.PREF_NAME"
        private const val IS_LOGGED_IN = "IS_LOGGED_IN"
        private const val USER_ID = "USER_ID"
    }

    fun saveLoginSession(userId: String) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(IS_LOGGED_IN, true)
        editor.putString(USER_ID, userId)
        editor.apply()
    }

    fun getUserId(): String? {
        return sharedPreferences.getString(USER_ID, null)
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