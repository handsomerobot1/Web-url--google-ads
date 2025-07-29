package com.handsomerobot.urlapp

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

class webviewactivity : AppCompatActivity() {

    private lateinit var adView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set layout
        setContentView(R.layout.activity_webviewactivity)

        // Initialize AdMob
        MobileAds.initialize(this) {}

        // Load Banner Ad
        adView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        // Load WebView
        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val savedUrl = sharedPreferences.getString("saved_url", "https://www.example.com")

        val webView = findViewById<WebView>(R.id.myWebView)
        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(savedUrl ?: "https://www.example.com")
    }
}
