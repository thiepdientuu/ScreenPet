package com.ls.petfunny

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.ls.petfunny.databinding.ActivitySplashBinding
import com.ls.petfunny.ui.ads.AdManager
import com.ls.petfunny.utils.AllEvents
import com.ls.petfunny.utils.AppLogger
import com.ls.petfunny.utils.TrackingHelper
import com.tp.ads.utils.AdCommonUtils
import com.tp.ads.utils.AppSession
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var adManager: com.tp.ads.base.AdManager

    lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TrackingHelper.logEvent(AllEvents.VIEW_SPLASH)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_splash)
        adManager.setIdOpenAd(
            idOpenAdNormal = AdCommonUtils.OPEN_AD_KEY,
            idOpenAdHigh = AdCommonUtils.OPEN_AD_HIGH_KEY
        )
        adManager.setActivity(this)
        loadConfig()
        checkConsent()
    }

    private fun loadConfig(){
        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    TrackingHelper.logEvent(AllEvents.CONFIG_LOAD + "success")
                    val showAds = remoteConfig.getBoolean("configAds")
                    val intervalInter = remoteConfig.getLong("intervalInter")
                    adManager.setInterAdsTimeDelay(intervalInter.toInt())
                    MainActivity.showCollapsibleBannerHome = remoteConfig.getBoolean("showCollapsibleHome")
                    val versionCode = remoteConfig.getLong("vscodereview")
                    if (showAds || versionCode.toInt() == BuildConfig.VERSION_CODE) {
                        adManager.setVipUser(false)
                    } else {
                        adManager.setVipUser(true)
                    }
                    AppLogger.d("Fetch and activate succeeded show ads: $showAds")
                } else {
                    TrackingHelper.logEvent(AllEvents.CONFIG_LOAD + "fail")
                }
            }
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
            while (count < TIMEOUT_SPLASH && !adManager.interSplashLoadFail && !AppSession.isVipUser) {
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

    private fun handleFinishShowInterSplash() {
        gotoHome()
    }

    fun addFragment(fragment : Fragment){
        supportFragmentManager.beginTransaction()
            .add(R.id.containerIntro, fragment)
            .addToBackStack(fragment::class.java.simpleName)
            .commit()
    }

    fun gotoHome(){
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

    override fun onBackPressed() {

    }

    companion object{
        const val TIMEOUT_SPLASH = 15
    }
}