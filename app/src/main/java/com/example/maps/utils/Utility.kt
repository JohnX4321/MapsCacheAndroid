package com.example.maps.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import com.example.maps.model.CachedWebResourceResponse
import java.io.File
import java.io.FileInputStream


object Utility {

    const val TAG = "MapsWVTAG"

    fun convertToCachedWebResourceResponse(cache: File): CachedWebResourceResponse? {
        return try {
            val fis = FileInputStream(cache)
            var headers: HashMap<String,String> = HashMap()
            val headerFile  = File(cache.absolutePath+".hds")
            try {
                if (headerFile.exists()) {
                    val z = headerFile.readText().split(";")
                    headers["Content-Type"] = z[0]
                    if (z.size>1) {
                        headers["Cache-Control"] = z[1]
                    } else headers["Cache-Control"] = "22222222"
                }
            } catch (x: Exception) {
                x.printStackTrace()
            }
            finally {
                if (!headers.containsKey("Content-Type")) {
                    headers["Content-Type"] = "image/png"
                }
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

    //Cache-Control
    fun getMaxCacheAge(headers: String? = null): Int {
        var age = 22222222
        if (headers!=null) {
            val s = headers.split("=")
            if (s.size>1) {
                age=s[1].toInt()
            }
        }
        return age
    }
}