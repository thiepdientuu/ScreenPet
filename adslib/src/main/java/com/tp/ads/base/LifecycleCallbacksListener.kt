package com.tp.ads.base

import android.app.Activity

interface LifecycleCallbacksListener {
    fun onStartLifecycle(activity: Activity?)

    fun onStopLifecycle(activity: Activity?)
}