package com.ls.petfunny

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.ls.petfunny.databinding.ActivitySplashBinding
import com.ls.petfunny.ui.ads.AdCommonUtils
import com.ls.petfunny.ui.ads.AdManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : ComponentActivity() {

    lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this,R.layout.activity_splash)
        loadBannerAd()

        lifecycleScope.launch {
            delay(3000)
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        }
    }

    private fun loadBannerAd() {
        AdManager.loadBannerAd(
            adUnitHigh = AdCommonUtils.BANNER_SPLASH_HIGH_KEY,
            adUnitNormal = AdCommonUtils.BANNER_SPLASH_KEY,
            container = binding.containerAd,
            showCollapsible = true
        )
    }
}