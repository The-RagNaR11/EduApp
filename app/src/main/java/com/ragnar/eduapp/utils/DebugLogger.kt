package com.ragnar.eduapp.utils

import android.util.Log
import com.ragnar.eduapp.BuildConfig


object DebugLogger {
    fun debugLog(tag: String, message: String){
        if (BuildConfig.DEBUG){
            Log.d(tag, message)
        }
    }

    fun errorLog(tag: String, message: String){
        if (BuildConfig.DEBUG){
            Log.e(tag, message)
        }
    }
}