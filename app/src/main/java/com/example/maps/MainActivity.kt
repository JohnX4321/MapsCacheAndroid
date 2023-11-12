package com.example.maps

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.example.maps.databinding.ActivityMainBinding
import com.example.maps.utils.CachedWebViewClient
import com.example.maps.utils.Prefs
import com.example.maps.utils.Utility

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var binding: ActivityMainBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        webView = binding.webview
        //WebView.setWebContentsDebuggingEnabled(true)
        webView.settings.javaScriptEnabled = true
        webView.settings.allowContentAccess = true
        webView.settings.allowFileAccess = true
        webView.webViewClient = CachedWebViewClient(webView)
        //webView?.webChromeClient = WebChromeClient()
        //webView?.addJavascriptInterface(CachedWebViewClient.WebInterface(this),"Android")
        //Thread.sleep(5000)
        //When the asset is loaded for the first time, Google Maps Javascript makes a request to server with a token and on subsequent launches uses a different token
        //Hence reloading it twice to fetch the new token
        webView.loadUrl("file:///android_asset/maps.html")
        if (Utility.isNetworkAvailable(this) && Prefs.isFirstLoad(this)) {
            webView.postDelayed({
                webView.reload()
            }, 2000)
            Prefs.isFirstLoad(this,false)
        }

        //As observed in Samsung phones, Any activity with webview restarts the first time, hence this logic
        /*if (Prefs.isFirstLoadInt(this)==0)
            Prefs.isFirstLoadInt(this,1)
        else if (Prefs.isFirstLoadInt(this)==1)
            Prefs.isFirstLoadInt(this,2)
        else if (Utility.isNetworkAvailable(this) && Prefs.isFirstLoadInt(this)==3) {
            webView?.postDelayed({
                webView?.reload()
            }, 2000)
            Prefs.isFirstLoad(this,false)
        }*/
    }
}