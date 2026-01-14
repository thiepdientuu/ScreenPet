package com.ls.petfunny.utils

import android.content.Context
import android.net.ConnectivityManager
import android.util.DisplayMetrics
import android.view.WindowManager

object AppConfig {
    lateinit var connectivityManager: ConnectivityManager
    lateinit var displayMetrics: DisplayMetrics

    val widthScreen: Int
        get() = displayMetrics.widthPixels

    val heightScreen: Int
        get() = displayMetrics.heightPixels

    fun setUp(context : Context){
        connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        displayMetrics = getScreen(context)
    }

    private fun getScreen(context: Context): DisplayMetrics {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(dm)
        return dm
    }
}