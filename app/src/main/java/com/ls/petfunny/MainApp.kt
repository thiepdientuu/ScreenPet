package com.ls.petfunny

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.ls.petfunny.utils.AppLogger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instances = this
        MobileAds.initialize(this) { initializationStatus ->
            AppLogger.d("HIHI ---> AdMob initialized: $initializationStatus")
        }
    }

    companion object{
        lateinit var instances : MainApp
    }
}