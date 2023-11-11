package com.example.maps.utils

import android.content.Context

object Prefs {

    const val FIRST_TRY_KEY = "firstTryKey"

    fun isFirstLoad(context: Context): Boolean {
        return context.getSharedPreferences("MapsWV",Context.MODE_PRIVATE)
            .getBoolean(FIRST_TRY_KEY,true)
    }

    fun isFirstLoad(context: Context, value: Boolean) {
        context.getSharedPreferences("MapsWV",Context.MODE_PRIVATE)
            .edit().putBoolean(FIRST_TRY_KEY,value).commit()
    }



}