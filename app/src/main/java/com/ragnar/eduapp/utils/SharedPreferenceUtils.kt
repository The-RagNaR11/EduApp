package com.ragnar.eduapp.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object GoogleUserPrefs {

    private const val PREF_NAME = "user_prefs"

    // Keys for each field
    const val KEY_ID = "id"
    const val KEY_EMAIL = "email"
    const val KEY_DISPLAY_NAME = "display_name"
    const val KEY_PROFILE_PIC = "profile_picture_uri"
    const val KEY_LANGUAGE = "selected_language"

    // Initialize the sharedPreference Instance
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // to save user info with key
    fun saveUserInfo(context: Context, key: String, value: String?) {
        getPrefs(context).edit { putString(key, value) }
    }

    // to get user info with key
    fun getUserInfo(context: Context, key: String): String? {
        return getPrefs(context).getString(key, null)
    }

    // to perform logout or clear session after a particular time period
    fun clearUserInfo(context: Context) {
        getPrefs(context).edit().clear().apply()
    }

    // to check if user is logged in
    fun isUserLoggedIn(context: Context): Boolean {
        val prefs = getPrefs(context)
        return prefs.contains(KEY_ID) && prefs.contains(KEY_EMAIL)
    }
}