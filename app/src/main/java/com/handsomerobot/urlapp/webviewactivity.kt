package com.handsomerobot.urlapp

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

class webviewactivity : AppCompatActivity() {

    private lateinit var adView: AdView
    private lateinit var webView: WebView
    private var backPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webviewactivity)

        // Initialize AdMob
        MobileAds.initialize(this) {}

        // Load Banner Ad
        adView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        // Configure WebView
        webView = findViewById(R.id.myWebView)
        configureWebView()

        // Load URL
        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val savedUrl = sharedPreferences.getString("saved_url", "https://www.example.com")
        webView.loadUrl(savedUrl ?: "https://www.example.com")
    }

    private fun configureWebView() {
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return when {
                    url.startsWith("http://") || url.startsWith("https://") -> {
                        view.loadUrl(url)
                        false
                    }
                    url.startsWith("tel:") || url.startsWith("mailto:") || url.startsWith("sms:") -> {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(this@webviewactivity, "Cannot open this link", Toast.LENGTH_SHORT).show()
                        }
                        true
                    }
                    else -> {
                        // Handle file downloads
                        if (url.contains(".pdf") || url.contains(".doc") ||
                            url.contains(".docx") || url.contains(".xls") ||
                            url.contains(".xlsx") || url.contains(".ppt") ||
                            url.contains(".pptx") || url.contains(".zip") ||
                            url.contains(".rar") || url.contains(".apk")) {
                            handleDownload(url, webView.settings.userAgentString, "", "")
                            true
                        } else {
                            view.loadUrl(url)
                            false
                        }
                    }
                }
            }
        }

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            setSupportMultipleWindows(true)
            javaScriptCanOpenWindowsAutomatically = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
        }
    }

    @SuppressLint("GestureBackNavigation")
    override fun onBackPressed() {
        when {
            webView.canGoBack() -> webView.goBack()
            backPressedTime + 2000 > System.currentTimeMillis() -> super.onBackPressed()
            else -> {
                Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()
                backPressedTime = System.currentTimeMillis()
            }
        }
    }

    private fun handleDownload(url: String, userAgent: String, contentDisposition: String, mimeType: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                val cookies = CookieManager.getInstance().getCookie(url)
                addRequestHeader("cookie", cookies)
                addRequestHeader("User-Agent", userAgent)
                setDescription("Downloading file...")
                setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType))
                allowScanningByMediaScanner()
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    URLUtil.guessFileName(url, contentDisposition, mimeType)
                )
            }

            val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
            Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onPause() {
        adView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        adView.resume()
    }

    override fun onDestroy() {
        adView.destroy()
        webView.destroy()
        super.onDestroy()
    }
}