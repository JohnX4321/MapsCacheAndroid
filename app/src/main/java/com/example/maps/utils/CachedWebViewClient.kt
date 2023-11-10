package com.example.maps.utils

import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.ObjectOutputStream
import java.io.OutputStream
import java.net.URL
import java.util.concurrent.Executors

class CachedWebViewClient: WebViewClient() {


    private val networkIOExecutor = Executors.newFixedThreadPool(10)

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        Log.d(Utility.TAG,"Making Request: ${request?.url}")
        val url = request?.url.toString()
        val ext = ".png"
        val cacheFile = File(view?.context?.cacheDir,url.hashCode().toString()+ext)
        var response: WebResourceResponse? = null


        Log.d(Utility.TAG,"Cache File path: ${cacheFile.absolutePath} : ${request?.requestHeaders?.get("Content-Type")}")

            response = if (cacheFile.exists()) {
                Log.d(Utility.TAG,"Cache exists")
                readFromCache(url, cacheFile)
            } else {
                readFromNetwork(url, cacheFile)
            }

        return response
    }

    override fun onLoadResource(view: WebView?, url: String?) {
        super.onLoadResource(view, url)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return super.shouldOverrideUrlLoading(view, request)
    }

    private fun readFromCache(url: String?,cache: File): WebResourceResponse? {
        val cachedWRR = Utility.convertToCachedWebResourceResponse(cache)
        return if (cachedWRR!=null) {
            val mime = cachedWRR.headers?.get("Content-Type") ?: "text/javascript"
            //cachedWRR.headers?.put("Access-Control-Allow-Origin","*")
            Log.d(Utility.TAG,"Read From file CT: ${mime}")
            WebResourceResponse(/*cachedWRR.headers?.get("Content-Type") ?: cachedWRR.mimeType,cachedWRR.encoding*/mime,"UTF-8",cachedWRR.data).apply {
                //responseHeaders = (cachedWRR.headers ?: HashMap())
            }
        } else  {
            Log.d(Utility.TAG,"Read Network 2")
            readFromNetwork(url, cache)
        }
    }

    private fun readFromNetwork(url: String?, cache: File): WebResourceResponse? {
        networkIOExecutor.execute {
            Log.d(Utility.TAG, "Read from network for : $url")
            if (url?.equals("file:///android_asset/maps.html") == true) return@execute
            var connectionInputStream: InputStream? = null
            var fileOutputStream: OutputStream? = null
            var headerOutputStream: ObjectOutputStream? = null
            try {
                val connection = URL(url).openConnection()

                connection.connect()
                val headerMap = connection.headerFields

                val headers = HashMap<String,String>()
                /*for (i in headerMap.keys) {
                    var temp = ""
                    if (i!=null && headerMap[i]?.isNotEmpty() == true) {
                        for (j in headerMap[i]!!) {
                            temp+="$j,"
                        }
                        if (url?.contains("google.internal.maps.mapsjs.v1.MapsJsInternalService/GetViewportInfo") == true)
                            Log.d(Utility.TAG,"$i : $temp")
                        headers[i] = temp
                    }
                }*/
                headers.put("Content-Type",connection.contentType)
                if (!cache.exists()) cache.createNewFile()
                connectionInputStream = connection.getInputStream()
                fileOutputStream = FileOutputStream(cache)
                headerOutputStream = ObjectOutputStream(FileOutputStream(cache.absolutePath+".hds"))
                val buffer = ByteArray(1024)
                connectionInputStream.buffered().use {
                    while (true) {
                        val len = it.read(buffer)
                        if (len<=0) break
                        fileOutputStream.write(buffer,0,len)
                    }
                }
                headerOutputStream?.writeObject(headers)

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                connectionInputStream?.close()
                fileOutputStream?.close()
                headerOutputStream?.close()

            }
        }
        return null
    }

    /*class WebInterface(val c: Context) {

        @JavascriptInterface
        fun processPostData(req: String,res: String) {
            Log.d("TAGXJ: REQ",req)
            Log.d("TAGXJ: RES",res)
        }



    }*/


}