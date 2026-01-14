package com.tp.ads.base

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.tp.ads.pref.AdsPreferencesHelper
import com.tp.ads.utils.AppSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

open class BaseLoader(context: Context) {

    val adsPreferencesHelper = AdsPreferencesHelper(context)
    var timeDelayLoadAds = 5000L


    open fun buildAdRequest(
        isCollapsibleBanner: Boolean = false,
        isBanner: Boolean = false,
        isInter: Boolean = false,
    ): AdManagerAdRequest {

        val extras = Bundle()

        if (isCollapsibleBanner) {
            extras.putString("collapsible", "bottom")
            extras.putString("collapsible_request_id", UUID.randomUUID().toString())
        }
        val request = AdManagerAdRequest.Builder()
        val adapterAds = AdMobAdapter::class.java
        request.addNetworkExtrasBundle(adapterAds, extras)
        return request.build()
    }

    open fun buildAdRequestBanner(
        isCollapsibleBanner: Boolean = false,
        isBanner: Boolean = false,
        isInter: Boolean = false,
        isRefreshCollapsible : Boolean = false
    ): AdManagerAdRequest {

        val extras = Bundle()

        if (!AppSession.isActiveServer) {
            extras.putString("max_ad_content_rating", "PG")
        }

        if (isCollapsibleBanner) {
            extras.putString("collapsible", "bottom")
            if(!isRefreshCollapsible){
                extras.putString("collapsible_request_id", UUID.randomUUID().toString())
            }
        }
        val request = AdManagerAdRequest.Builder()
        val adapterAds = AdMobAdapter::class.java
        request.addNetworkExtrasBundle(adapterAds, extras)
        return request.build()
    }

    open fun loadAd(listener: AdsLoaderListener, isForceLoad: Boolean = false) {}

    open fun showAd(
        activity: Activity,
        rwdTime: Int?,
        listener: AdsShowerListener
    ) {
    }

    open fun handleAutoLoadAds(count: Int, isDelay:Boolean) {}

    fun runOnDelay(timeMs: Long = timeDelayLoadAds, onRun: suspend () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            delay(timeMs)
            withContext(Dispatchers.Main) {
                onRun()
            }
        }
    }

    open fun release() {}

    open fun reLoadAd() {}

}