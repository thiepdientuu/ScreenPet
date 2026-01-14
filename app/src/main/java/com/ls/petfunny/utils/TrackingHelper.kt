package com.ls.petfunny.utils

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.ls.petfunny.BuildConfig
import com.ls.petfunny.MainApp


object TrackingHelper {
    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    fun init() {
        try {
            if (mFirebaseAnalytics == null) {
                mFirebaseAnalytics =
                    FirebaseAnalytics.getInstance(MainApp.instances.applicationContext)
            }
        } catch (e : Exception) {
            AppLogger.d("Fail init because: " + e.message)
        }
    }

    fun logEvent(eventName: String) {
        if (mFirebaseAnalytics == null) init()
        val bundle = Bundle()
        bundle.putString("app version", BuildConfig.VERSION_NAME)
        try {
            mFirebaseAnalytics?.logEvent(eventName, bundle)
            AppLogger.d("Firebase Event: $eventName")
        } catch (e: Exception) {
            AppLogger.d("Fail log event because: " + e.message)
        }
    }
}