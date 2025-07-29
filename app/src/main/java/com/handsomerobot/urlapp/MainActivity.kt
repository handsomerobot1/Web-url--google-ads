package com.handsomerobot.urlapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private var rewardedAd: RewardedAd? = null
    private lateinit var submitButton: Button
    private lateinit var editText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)

        // If URL already saved, skip to WebView
        val savedUrl = sharedPreferences.getString("saved_url", null)
        if (!savedUrl.isNullOrEmpty()) {
            startActivity(Intent(this, webviewactivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        MobileAds.initialize(this) {}

        submitButton = findViewById(R.id.submit)
        editText = findViewById(R.id.weburl)

        submitButton.isEnabled = false // Disable until ad loads
        loadRewardedAd()

        submitButton.setOnClickListener {
            val url = editText.text.toString().trim()

            if (url.isEmpty()) {
                editText.error = "Please enter a URL"
                return@setOnClickListener
            }

            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                editText.error = "URL must start with http:// or https://"
                return@setOnClickListener
            }

            submitButton.isEnabled = false // Prevent multiple clicks

            if (rewardedAd != null) {
                rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        rewardedAd = null
                        loadRewardedAd() // Load next ad
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Toast.makeText(this@MainActivity, "Ad failed to show", Toast.LENGTH_SHORT).show()
                        submitButton.isEnabled = true
                        loadRewardedAd()
                    }
                }

                rewardedAd?.show(this) { rewardItem ->
                    // Only triggered when ad is watched completely
                    sharedPreferences.edit().putString("saved_url", url).apply()
                    proceedToWebView()
                }

            } else {
                Toast.makeText(this, "Ad not ready yet, please wait", Toast.LENGTH_SHORT).show()
                submitButton.isEnabled = true
                loadRewardedAd()
            }
        }
    }

    private fun proceedToWebView() {
        startActivity(Intent(this, webviewactivity::class.java))
        finish()
    }

    private fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            this,
            "ca-app-pub-6960915010996613/2547698677", // Test Ad Unit
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    submitButton.isEnabled = true
                    Toast.makeText(this@MainActivity, "Ad loaded - Submit now!", Toast.LENGTH_SHORT).show()
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardedAd = null
                    submitButton.isEnabled = false
                    Toast.makeText(this@MainActivity, "Ad failed: ${adError.message}", Toast.LENGTH_SHORT).show()
                    // Retry loading after delay
                    submitButton.postDelayed({ loadRewardedAd() }, 5000)
                }
            }
        )
    }

    override fun onDestroy() {
        rewardedAd = null
        super.onDestroy()
    }
}
