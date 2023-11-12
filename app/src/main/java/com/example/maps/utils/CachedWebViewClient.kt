package com.example.maps.utils

import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.Executors

class CachedWebViewClient(val webView: WebView): WebViewClient() {

    companion object {
        const val ENCODING = "UTF-8"
        //for maps js api all follow this encoding. Dyanmically it can be obtained from connection.contentType. Optimized for this purpose
    }

    //required for parallel
    private val networkIOExecutor = Executors.newFixedThreadPool(5)

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {

       return processReq(request)
    }

    private fun processReq(request: WebResourceRequest?) : WebResourceResponse? {
        Log.d(Utility.TAG,"Making Request: ${request?.url}")
        val url = request?.url.toString()
        val ext = ".cf" //cache file
        val hashCode =  url.hashCode().toString()
        val cacheFile = File(webView.context?.cacheDir,hashCode+ext)
        var response: WebResourceResponse? = null

        request?.requestHeaders?.set("Access-Control-Allow-Origin","*")
        val z = WrappedWebResourceResponse("image/png","UTF-8",null)
        //changes for synchronous
        if (url.contains("QuotaService") && !cacheFile.exists()) return WebResourceResponse("text/javascript","UTF-8",webView.context?.assets?.open("QuotaService.js"))
        response = if (cacheFile.exists()) {
            Log.d(Utility.TAG,"Cache exists")
            readFromCache(url, cacheFile, z)
            z
        } else {
            readFromNetwork(url, cacheFile,z)
        }

        return response
    }


    private fun readFromCache(url: String?,cache: File, z: WrappedWebResourceResponse): WebResourceResponse? {
        val cachedWRR = Utility.convertToCachedWebResourceResponse(cache)
        return if (cachedWRR!=null) {
            val mime = cachedWRR.headers?.get("Content-Type") ?: "image/png"
            if (cachedWRR.headers?.get("Cache-Control")?.toInt()!!*1000 < (System.currentTimeMillis()-cache.lastModified())) {
                cache.delete()
                readFromNetwork(url,cache,z)
            } else
                z.modifyResource(mime,"UTF-8",cachedWRR.data)
            z
        } else  {
            Log.d(Utility.TAG,"Read Network 2")
            readFromNetwork(url, cache,z)
        }
    }

    private fun readFromNetwork(url: String?, cache: File,z: WrappedWebResourceResponse): WebResourceResponse? {
        //required for parallel
        var mimeConnection: HttpURLConnection? = null
        try {
            mimeConnection = URL(url).openConnection() as HttpURLConnection
            z.mimeType = mimeConnection.contentType.split(";")[0]
            z.encoding = ENCODING
            mimeConnection.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mimeConnection?.disconnect()
        }
        networkIOExecutor.execute {
            Log.d(Utility.TAG, "Read from network for : $url")
            //Don't use cached asset file, load the original one directly.
            if (url?.equals("file:///android_asset/maps.html") == true || url?.contains("data:")==true) return@execute
            var connectionInputStream: InputStream? = null
            var fileOutputStream: OutputStream? = null
            var headerOutputStream: OutputStream? = null
            try {
                val connection = URL(url).openConnection()

                connection.connect()
                val headerMap = connection.headerFields
                var hStr = ""
                //Headers required for Cache Expiry and MimeType
                //Stored in a separate file with .hds extension
                val contentTypeArr = connection.contentType.split(";")
                hStr+=contentTypeArr[0]+";"+Utility.getMaxCacheAge(headerMap?.get("Cache-Control")?.get(0)).toString()
                if (!cache.exists()) cache.createNewFile()
                connectionInputStream = connection.getInputStream()
                fileOutputStream = FileOutputStream(cache)
                headerOutputStream = FileOutputStream(cache.absolutePath+".hds")
                val buffer = ByteArray(1024)
                connectionInputStream.buffered().use {
                    while (true) {
                        val len = it.read(buffer)
                        if (len<=0) break
                        fileOutputStream.write(buffer,0,len)
                    }
                }
                headerOutputStream?.write(hStr.encodeToByteArray());
                //changes for synchronous
                z.modifyResource(contentTypeArr[0],"UTF-8",cache.inputStream())



            } catch (e: Exception) {
                Log.d(Utility.TAG,"Error Accessing $url")
                e.printStackTrace()
            } finally {
                connectionInputStream?.close()
                fileOutputStream?.close()
                headerOutputStream?.close()

            }
        //required for parallel
        }
        //return null
        //changes for synchronous
        return z
    }

    /*class WebInterface(val c: Context) {

        @JavascriptInterface
        fun processPostData(req: String,res: String) {
            Log.d("TAGXJ: REQ",req)
            Log.d("TAGXJ: RES",res)
        }



    }*/


}