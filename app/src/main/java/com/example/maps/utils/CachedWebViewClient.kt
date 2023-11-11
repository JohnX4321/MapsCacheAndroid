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
import java.net.URL
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.Executors

class CachedWebViewClient(val webView: WebView): WebViewClient() {


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
        Log.d(Utility.TAG,"Cache File path: ${cacheFile.absolutePath} : ${request?.requestHeaders?.get("Content-Type")}")
        /*    if (url == "https://maps.googleapis.com/maps-api-v3/api/js/53/14/marker.js") {
                Log.d(Utility.TAG,"Read marker")
                return WebResourceResponse("text/javascript","UTF-8",context.assets.open("marker.js"))
            }
        if (url == "https://maps.googleapis.com/maps-api-v3/api/js/53/14/util.js") {
            Log.d(Utility.TAG,"Read marker")
            return WebResourceResponse("text/javascript","UTF-8",context.assets.open("util.js"))
        }
        if (url=="https://maps.googleapis.com/maps-api-v3/api/js/53/14/controls.js") {
            return WebResourceResponse("text/javascript","UTF-8",context.assets.open("controls.js"))
        }
        if (url == "https://fonts.gstatic.com/s/roboto/v30/KFOmCnqEu92Fr1Mu4mxPKTU1Kg.ttf") {
            return WebResourceResponse("font/ttf","UTF-8",context.assets.open("KFOmCnqEu92Fr1Mu4mxPKTU1Kg.ttf"))
        }

        if (url.contains("QuotaService")) {
            Log.d("TG","LLS")
        }*/
        //if (!url.contains("/maps/vt")) return response
        val z = WrappedWebResourceResponse("image/png","UTF-8",null)
        response = if (cacheFile.exists()) {
            Log.d(Utility.TAG,"Cache exists")
            val z = WrappedWebResourceResponse("image/png","UTF-8",null)
            readFromCache(url, cacheFile,z)
            z
        } else {
            readFromNetwork(url, cacheFile,z)
            z
        }

        return response
    }


    private fun readFromCache(url: String?,cache: File, z: WrappedWebResourceResponse): WebResourceResponse? {
        val cachedWRR = Utility.convertToCachedWebResourceResponse(cache)
        return if (cachedWRR!=null) {
            val mime = cachedWRR.headers?.get("Content-Type") ?: "image/png"//Prefs.getMimeMap(context,cache.name) ?: "image/png"
            Log.d(Utility.TAG,"Read From file CT: ${mime}")
            if (cachedWRR.headers?.get("Cache-Control")?.toInt()!!*1000 < (System.currentTimeMillis()-cache.lastModified())) {
                cache.delete()
                readFromNetwork(url,cache,z)
            } else

                z.modifyResource(mime,"UTF-8",cachedWRR.data)
            z
        } else  {
            Log.d(Utility.TAG,"Read Network 2")
            /*readFromNetwork(url, cache,z)
            z*/
            null
        }
    }

    private fun readFromNetwork(url: String?, cache: File,z: WrappedWebResourceResponse){//: WebResourceResponse? {
        networkIOExecutor.execute {
            Log.d(Utility.TAG, "Read from network for : $url")
            if (url?.equals("file:///android_asset/maps.html") == true || url?.contains("data:")==true) return@execute
            var connectionInputStream: InputStream? = null
            var fileOutputStream: OutputStream? = null
            var headerOutputStream: OutputStream? = null
            try {
                val connection = URL(url).openConnection()

                connection.connect()
                val headerMap = connection.headerFields

                val headers = HashMap<String,String>()
                var hStr = ""
                /*for (i in headerMap.keys) {
                    var temp = ""
                    if (i!=null && headerMap[i]?.isNotEmpty() == true) {
                        for (j in headerMap[i]!!) {
                            temp+="$j,"
                        }
                        if (url?.contains("QuotaService") == true)
                            Log.d(Utility.TAG,"$i : $temp")
                        headers[i] = temp
                    }
                }*/
                val contentTypeArr = connection.contentType.split(";")
                hStr+=contentTypeArr[0]+";"+Utility.getMaxCacheAge(headerMap?.get("Cache-Control")?.get(0)).toString()
                /*headers.set("Content-Type",connection.contentType);
                headers.set("Cache-Control",
                    Utility.getMaxCacheAge(headerMap?.get("Cache-Control")?.get(0)).toString()
                )*/
                //Prefs.setMimeMap(context,cache.name,connection.contentType ?: "")
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
                headerOutputStream?.write(hStr.encodeToByteArray())
                z.modifyResource(contentTypeArr[0],"UTF-8",connectionInputStream)

            } catch (e: Exception) {
                Log.d(Utility.TAG,"Error Accessing $url")
                e.printStackTrace()
            } finally {
                connectionInputStream?.close()
                fileOutputStream?.close()
                headerOutputStream?.close()

            }

        }
       // return null
    }

    /*class WebInterface(val c: Context) {

        @JavascriptInterface
        fun processPostData(req: String,res: String) {
            Log.d("TAGXJ: REQ",req)
            Log.d("TAGXJ: RES",res)
        }



    }*/


}