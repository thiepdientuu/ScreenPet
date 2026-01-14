package com.tp.ads.utils

import android.util.Log
import com.tp.ads.BuildConfig

class Logger {
    companion object {
        private const val TAG = "Tracking"
        fun d(msg: String) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, msg)
            }
        }

        fun i(msg: String) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, msg)
            }
        }

        fun w(msg: String) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, msg)
            }
        }

        fun w(tag: String?, msg: String) {
            if (BuildConfig.DEBUG) {
                Log.w(tag, msg)
            }
        }

        fun e(msg: String, error: Throwable) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, msg, error)
            }
        }
    }
}