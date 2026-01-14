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
import com.ls.petfunny.utils.AppLogger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : ComponentActivity() {

    @Inject
    lateinit var adManager: com.tp.ads.base.AdManager

    lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_splash)
        enableEdgeToEdge()
        checkConsent()
    }

    private fun checkConsent(){
        adManager.checkConsent(this@SplashActivity, gatheringCompleteListener = {
            AppLogger.d("HIHI ---> gatheringCompleteListener")
            if (!adManager.googleMobileAdsConsentManager.canRequestAds) {
                handleFinishShowInterSplash()
            }
        }, beforeInitMobileAds = {
            AppLogger.d("HIHI ---> beforeInitMobileAds")
        }, initMobileAdSuccess = {
            AppLogger.d("HIHI ---> initMobileAdSuccess")
            loadBannerAd()
            startCountDownTimer()
        })
    }

    private fun startCountDownTimer(){
        lifecycleScope.launch {
            var count = 0
            while (count < TIMEOUT_SPLASH && !adManager.interSplashLoadFail) {
                delay(1000)
                count++
                if (adManager.interSplash == null) {
                    AppLogger.d("HIHI ---> inter splash null")
                } else {
                    AppLogger.d("HIHI ---> inter splash available")
                    adManager.showInterSplash(this@SplashActivity) {
                        handleFinishShowInterSplash()
                    }
                    return@launch
                }
            }
            handleFinishShowInterSplash()
        }
    }

    private fun handleFinishShowInterSplash(){
        startMainActivity()
    }

    private fun startMainActivity(){
        try {
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        } catch (e: Exception) {
            val otherIntent  = Intent(this@SplashActivity, MainActivity::class.java)
            otherIntent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        or Intent.FLAG_ACTIVITY_NEW_TASK
            )
            startActivity(otherIntent)
        }
        finishAfterTransition()
    }

    private fun loadBannerAd() {
        AdManager.loadBannerAd(
            adUnitHigh = AdCommonUtils.BANNER_SPLASH_HIGH_KEY,
            adUnitNormal = AdCommonUtils.BANNER_SPLASH_KEY,
            container = binding.containerAd,
            showCollapsible = true
        )
    }

    companion object{
        const val TIMEOUT_SPLASH = 15
    }
}