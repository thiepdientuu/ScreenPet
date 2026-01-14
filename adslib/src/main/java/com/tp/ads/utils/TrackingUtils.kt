package com.tp.ads.utils

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object TrackingUtils {
    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    fun init(activity: Activity) {
        try {
            if (mFirebaseAnalytics == null) {
                mFirebaseAnalytics =
                    FirebaseAnalytics.getInstance(activity)
            }
        } catch (e : Exception) {
            Logger.w("Fail init because: " + e.message)
        }
    }

    fun init(context: Context) {
        try {
            if (mFirebaseAnalytics == null) {
                mFirebaseAnalytics =
                    FirebaseAnalytics.getInstance(context)
            }
        } catch (e : Exception) {
            Logger.w("Fail init because: " + e.message)
        }
    }

    fun logEvent(eventName: String) {
        if (mFirebaseAnalytics == null) return
        val bundle = Bundle()
        try {
            mFirebaseAnalytics?.logEvent(eventName, bundle)
            Logger.w("Firebase Event: $eventName")
        } catch (e: Exception) {
            Logger.w("Fail log event because: " + e.message)
        }
    }
}