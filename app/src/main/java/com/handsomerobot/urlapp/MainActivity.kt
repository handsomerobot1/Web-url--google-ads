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
    private var adLoadCallback: RewardedAdLoadCallback? = null
    private lateinit var submitButton: Button
    private lateinit var editText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)

        // If URL already saved, skip
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

            // Validate URL format
            if (url.isEmpty()) {
                editText.error = "Please enter a URL"
                return@setOnClickListener
            }

            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                editText.error = "URL must start with http:// or https://"
                return@setOnClickListener
            }

            when {
                rewardedAd != null -> {
                    rewardedAd?.show(this) { rewardItem ->
                        // User watched full ad → save and continue
                        sharedPreferences.edit().putString("saved_url", url).apply()
                        proceedToWebView()
                    } ?: run {
                        Toast.makeText(this, "Ad failed to show", Toast.LENGTH_SHORT).show()
                        loadRewardedAd() // Try loading again
                    }
                }
                else -> {
                    Toast.makeText(this, "Ad not ready yet, please wait", Toast.LENGTH_SHORT).show()
                    loadRewardedAd() // Try loading again
                }
            }
        }
    }

    private fun proceedToWebView() {
        startActivity(Intent(this, webviewactivity::class.java))
        finish()
    }

    private fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()

        adLoadCallback = object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
                submitButton.isEnabled = true
                Toast.makeText(this@MainActivity, "Ad loaded - Submit now!", Toast.LENGTH_SHORT).show()
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                rewardedAd = null
                submitButton.isEnabled = false
                Toast.makeText(
                    this@MainActivity,
                    "Ad failed: ${adError.message}",
                    Toast.LENGTH_SHORT
                ).show()

                // Retry after delay
                submitButton.postDelayed({ loadRewardedAd() }, 5000)
            }
        }

        RewardedAd.load(
            this,
            "ca-app-pub-3940256099942544/5224354917", // Test Rewarded Ad Unit ID
            adRequest,
            adLoadCallback as RewardedAdLoadCallback
        )
    }

    override fun onDestroy() {
        adLoadCallback = null
        rewardedAd = null
        super.onDestroy()
    }
}