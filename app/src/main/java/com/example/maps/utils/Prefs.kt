package com.example.maps.utils

import android.content.Context

object Prefs {

    const val FIRST_TRY_KEY = "firstTryKey"
    const val FIRST_TRY_KEY_INT = "firstTryKeyInt"
    const val SHARED_PREFS_NAME = "MapsWV"

    fun isFirstLoad(context: Context): Boolean {
        return context.getSharedPreferences(SHARED_PREFS_NAME,Context.MODE_PRIVATE)
            .getBoolean(FIRST_TRY_KEY,true)
    }

    fun isFirstLoad(context: Context, value: Boolean) {
        context.getSharedPreferences(SHARED_PREFS_NAME,Context.MODE_PRIVATE)
            .edit().putBoolean(FIRST_TRY_KEY,value).commit()
    }

    //Device Specific Issue
    fun isFirstLoadInt(context: Context): Int {
        return context.getSharedPreferences(SHARED_PREFS_NAME,Context.MODE_PRIVATE)
            .getInt(FIRST_TRY_KEY_INT,0)
    }

    fun isFirstLoadInt(context: Context,value: Int) {
        context.getSharedPreferences(SHARED_PREFS_NAME,Context.MODE_PRIVATE)
            .edit().putInt(FIRST_TRY_KEY_INT,value).commit()
    }



}