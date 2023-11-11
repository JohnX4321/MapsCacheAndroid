package com.example.maps

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
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
        WebView.setWebContentsDebuggingEnabled(true)
        webView!!.settings.javaScriptEnabled = true
        webView!!.settings.allowContentAccess = true
        webView!!.settings.allowFileAccess = true
        webView?.webViewClient = CachedWebViewClient(webView!!)
        webView?.webChromeClient = WebChromeClient()
        //webView?.addJavascriptInterface(CachedWebViewClient.WebInterface(this),"Android")
        Thread.sleep(5000)
        webView?.loadUrl("file:///android_asset/maps.html")
        if (Utility.isNetworkAvailable(this) && Prefs.isFirstLoad(this)) {
            webView?.postDelayed({
                webView?.reload()
            }, 2000)
            Prefs.isFirstLoad(this,false)
        }
    }
}