package com.ls.petfunny.utils


import com.ls.petfunny.BuildConfig
import timber.log.Timber

object AppLogger {
	val TAG = "appInfo"
	fun d(s: String?, vararg objects: Any?) {
		Timber.tag(TAG).d(s, *objects)
	}

	fun d(throwable: Throwable?, s: String?, vararg objects: Any?) {
		Timber.tag(TAG).d(throwable, s, *objects)
	}

	fun e(s: String?, vararg objects: Any?) {
		Timber.tag(TAG).e(s, *objects)
	}

	fun e(throwable: Throwable?, s: String?, vararg objects: Any?) {
		Timber.tag(TAG).e(throwable, s, *objects)
	}

	fun i(s: String?, vararg objects: Any?) {
		Timber.tag(TAG).i(s, *objects)
	}

	fun i(throwable: Throwable?, s: String?, vararg objects: Any?) {
		Timber.tag(TAG).i(throwable, s, *objects)
	}

	fun init() {
		if (BuildConfig.DEBUG) {
			Timber.plant(Timber.DebugTree())
		}
	}

	fun w(s: String?, vararg objects: Any?) {
		Timber.w(s, *objects)
	}

	fun w(throwable: Throwable?, s: String?, vararg objects: Any?) {
		Timber.w(throwable, s, *objects)
	}
}