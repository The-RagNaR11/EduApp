package com.ragnar.eduapp.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object SharedPreferenceUtils {

    private const val PREF_NAME = "user_prefs"

    // Keys for each
    const val KEY_LANGUAGE = "selected_language"
    const val KEY_ID = "id"
    const val KEY_EMAIL = "email"
    const val KEY_DISPLAY_NAME = "display_name"
    const val KEY_PROFILE_PIC = "profile_picture_uri"
    const val KEY_PHONE_NUMBER = "phone_number"
    const val KEY_SCHOOL_NAME = "school_name"
    const val KEY_AMBITION = "ambition"
    const val KEY_LEARNING_INTENT = "learning_intent"
    const val KEY_CLASS = "student_class"
    const val KEY_PACE = "student_pace"
    const val KEY_CHAPTER_LIST = "chapter_list"
    const val KEY_SUBJECT = "selected_subject"
    const val KEY_SYLLABUS = "selected_syllabus"


    // Initialize the sharedPreference Instance
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // to save user info with key
    fun saveUserInfo(context: Context, key: String, value: String?) {
        val editor = getPrefs(context).edit()
        editor.putString(key, value)
        editor.commit()
    }

    // to get user info with key
    fun getUserInfo(context: Context, key: String): String? {
        return getPrefs(context).getString(key, "")
    }

    // to perform logout or clear session after a particular time period
    fun clearUserInfo(context: Context) {
        getPrefs(context).edit { clear() }
    }

    // to check if user is logged in
    fun isUserLoggedIn(context: Context): Boolean {
        val prefs = getPrefs(context)
        return prefs.contains(KEY_ID) && prefs.contains(KEY_EMAIL)
    }

    // to check if user has entered the details or not
    fun isUserDetailAvailable(context: Context): Boolean {
        val prefs = getPrefs(context)
        return prefs.contains(KEY_PHONE_NUMBER)
    }

}