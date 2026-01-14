package com.ls.petfunny

import android.app.Application
import com.ls.petfunny.utils.AppConfig
import com.ls.petfunny.utils.AppLogger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instances = this
        AppConfig.setUp(this)
        AppLogger.init()
    }

    companion object{
        lateinit var instances : MainApp
    }
}