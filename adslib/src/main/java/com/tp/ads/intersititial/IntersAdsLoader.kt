package com.tp.ads.intersititial

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.tp.ads.base.AdsConfig
import com.tp.ads.base.AdsLoaderListener
import com.tp.ads.base.AdsShowerListener
import com.tp.ads.base.BaseLoader
import com.tp.ads.utils.AdCommonUtils
import com.tp.ads.utils.AppSession
import com.tp.ads.utils.EventUtils
import com.tp.ads.utils.Logger
import com.tp.ads.utils.TrackingUtils
import java.lang.ref.WeakReference

class IntersAdsLoader constructor(
    val context: Context,
) : BaseLoader(context) {

    private var adsInterId: String = ""
    private var adsInterHigh: String = ""
    private var _interstitialAd: InterstitialAd? = null
    private var activityWeakRef: WeakReference<Activity>? = null

    var isLoadingInter = false
    private var intervalShowInter = 10000L
    private var lastTimeShowInter = 0L

    private var isForceLoadInter = false
    private var retryLoadAds = true

    fun setAdsInterId(adsInterId: String,adsInterHigh : String) {
        this.adsInterId = adsInterId
        this.adsInterHigh = adsInterHigh
    }

    fun setIntervalShowInter(intervalShowInter: Int) {
        this.intervalShowInter = intervalShowInter * 1000L
    }

    fun setActivity(activity: Activity) {
        activityWeakRef = WeakReference(activity)
    }

    override fun buildAdRequest(
        isCollapsibleBanner: Boolean, isBanner: Boolean, isInter: Boolean
    ): AdManagerAdRequest {
        return super.buildAdRequest(isCollapsibleBanner, isBanner, isInter)
    }

    override fun loadAd(listener: AdsLoaderListener, isForceLoad: Boolean) {
        if (!AppSession.canRequestAd || AppSession.isVipUser
            || _interstitialAd != null || isLoadingInter || !AdsConfig.supportInterstitial
        ) {
            Logger.w("AdManager", "InterAdsLoader-----> return inter not load")
            listener.onLoadFinish()
            return
        }

        isLoadingInter = true
        isForceLoadInter = isForceLoad
        Logger.w(TAG, "InterAdsLoader-----> handler load InterAds")
        val adRequest = buildAdRequest(isInter = true)
        InterstitialAd.load(context, AdCommonUtils.INTER_ALL_KEY, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                TrackingUtils.logEvent(EventUtils.INTERSTITIAL + "load_fail")
                _interstitialAd = null
                isLoadingInter = false
                Logger.w("AdManager", "InterAdsLoader -----> inter load fail :${adError.message}")
                listener.onLoadAdsError()
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                TrackingUtils.logEvent(EventUtils.INTERSTITIAL + "load_success")
                retryLoadAds = true
                _interstitialAd = interstitialAd
                isLoadingInter = false
                Logger.w("AdManager", "InterAdsLoader -----> inter load success")
                listener.onAdLoaded()
            }
        })
    }
    fun loadInterHighAd(listener: AdsLoaderListener, isForceLoad: Boolean) {
        if (!AppSession.canRequestAd || AppSession.isVipUser
            || _interstitialAd != null || isLoadingInter || !AdsConfig.supportInterstitial
        ) {
            Logger.w("AdManager", "InterAdsLoader-----> return inter high not load")
            listener.onLoadFinish()
            return
        }

        isLoadingInter = true
        isForceLoadInter = isForceLoad
        Logger.w(TAG, "InterAdsLoader-----> handler load InterAds HIGH adUnit = $adsInterHigh")
        val adRequest = buildAdRequest(isInter = true)
        InterstitialAd.load(context, AdCommonUtils.INTER_ALL_HIGH_KEY, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                TrackingUtils.logEvent(EventUtils.INTERSTITIAL + "load_fail")
                _interstitialAd = null
                isLoadingInter = false
                Logger.w("AdManager", "InterAdsLoader -----> inter HIGH load fail :${adError.message}")
                listener.onLoadAdsError()
                listener.onLoadFinish()
                loadAd(object : AdsLoaderListener() {}, false)
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                TrackingUtils.logEvent(EventUtils.INTERSTITIAL + "load_success")
                retryLoadAds = true
                _interstitialAd = interstitialAd
                isLoadingInter = false
                Logger.w("AdManager", "InterAdsLoader -----> inter HIGH load success")
                listener.onAdLoaded()
                listener.onLoadFinish()
            }
        })
    }

    fun updateLastTimeShowInter(){
        lastTimeShowInter = System.currentTimeMillis()
    }

    fun canShowInterInterVal() = System.currentTimeMillis() - lastTimeShowInter > intervalShowInter

    override fun showAd(
        activity: Activity, rwdTime: Int?, listener: AdsShowerListener
    ) {
        setActivity(activity)
        val condition = System.currentTimeMillis() - lastTimeShowInter > intervalShowInter
        if (_interstitialAd == null || !condition || isLoadingInter || AppSession.isVipUser || !AdsConfig.supportInterstitial) {
            val reason = if (!condition) {
                "Time interval not enough"
            } else if (_interstitialAd == null) {
                loadInterHighAd(object : AdsLoaderListener() {},false)
                "InterstitialAd is null"
            } else if (isLoadingInter) {
                "Doing load interstitial ad"
            } else "Something wrong"
            Logger.w("InterAdsLoader-----onShowAdsError :$reason")

            listener.onShowAdsError()
            return
        }
        _interstitialAd?.show(activity)
        _interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Logger.w(TAG, "-----onAdDismissedFullScreenContent")
                _interstitialAd = null
                lastTimeShowInter = System.currentTimeMillis()
                listener.onAdDismissedFullScreenContent()
                loadInterHighAd(object : AdsLoaderListener() {},false)
            }

            override fun onAdClicked() {
                listener.onAdClicked()
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                Logger.w(TAG, "-----onAdFailedToShowFullScreenContent : ${p0.message}")
                listener.onAdFailedToShowFullScreenContent(p0)
            }

            override fun onAdImpression() {
                listener.onAdImpression()
            }

            override fun onAdShowedFullScreenContent() {
                Logger.w(TAG, "-----onAdShowedFullScreenContent")
                listener.onAdShowedFullScreenContent()
            }
        }
        _interstitialAd?.onPaidEventListener = OnPaidEventListener { adValue ->
            adsPreferencesHelper.saveTroasCache(
                adValue.valueMicros / 1000000.0,
                adValue.currencyCode
            )
            val previousTroasCache = adsPreferencesHelper.troasCache
            val currentTroasCache = (previousTroasCache + adValue.valueMicros / 1000000.0).toFloat()
            if (currentTroasCache >= 0.01) {
            }
        }
    }

    override fun handleAutoLoadAds(count: Int, isDelay: Boolean) {

    }

    override fun release() {
        _interstitialAd = null
    }

    fun hasInterAds(): Boolean {
        return _interstitialAd != null
    }

    companion object {
        const val TAG = "IntersAdsLoader"
    }

}