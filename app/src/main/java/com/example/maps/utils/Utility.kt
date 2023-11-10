package com.example.maps.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import com.example.maps.model.CachedWebResourceResponse
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.io.ObjectInputStream

object Utility {

    const val TAG = "MapsWVTAG"

    fun convertToCachedWebResourceResponse(cache: File): CachedWebResourceResponse? {
        return try {
            val fis = FileInputStream(cache)
            var headers: HashMap<String,String>? = HashMap()
            val headerFile  = File(cache.absolutePath+".hds")
            if (headerFile.exists()) {
                headers = ObjectInputStream(FileInputStream(headerFile)).readObject() as? HashMap<String,String>
            }
            CachedWebResourceResponse(fis, headers = headers)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        try {
            val cm = context.getSystemService(ConnectivityManager::class.java)
            return cm.activeNetwork!=null && cm.getNetworkCapabilities(cm.activeNetwork)?.hasCapability(NET_CAPABILITY_INTERNET) == true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

}