package com.example.petfunny

import android.app.Application
import android.util.Log
import com.google.android.gms.ads.MobileAds

class MainApp : Application() {

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this) { initializationStatus ->
            Log.d("HIHI","AdMob initialized: $initializationStatus")
        }
    }
}