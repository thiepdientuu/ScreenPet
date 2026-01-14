package com.tp.ads.rewardedinters

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.tp.ads.base.AdsConfig
import com.tp.ads.base.AdsLoaderListener
import com.tp.ads.base.AdsShowerListener
import com.tp.ads.base.BaseLoader
import com.tp.ads.utils.AppSession
import com.tp.ads.utils.EventUtils
import com.tp.ads.utils.Logger
import com.tp.ads.utils.TrackingUtils
import java.lang.ref.WeakReference

class RewardedInterAdsLoader(
    val context: Context,
) : BaseLoader(context) {

    private var adsRewardedInterId: String = ""
    private var _rewardedInterAds: RewardedInterstitialAd? = null
    private var activityWeakRef: WeakReference<Activity>? = null

    var isLoadingRewardedInter = false
    private var retryLoadAds = true
    private var isEarned = false

    fun setAdsRewardedInterId(adsRewardedInterId: String) {
        this.adsRewardedInterId = adsRewardedInterId
    }

    fun setActivity(activity: Activity) {
        activityWeakRef = WeakReference(activity)
    }

    override fun buildAdRequest(
        isCollapsibleBanner: Boolean, isBanner: Boolean, isInter: Boolean
    ): AdManagerAdRequest {
        // Todo override or add extra for ad request here
        return super.buildAdRequest(isCollapsibleBanner, isBanner, isInter)
    }

    override fun loadAd(listener: AdsLoaderListener, isForceLoad: Boolean) {
        if (!AppSession.canRequestAd || AppSession.isVipUser
            || _rewardedInterAds != null || isLoadingRewardedInter || !AdsConfig.supportRewardedInterstitial
        ) {
            Logger.w("AdManager", "RewardInter -----rewarded inter not load")
            listener.onLoadFinish()
            return
        }
        isLoadingRewardedInter = true

        Logger.w("AdManager ----> RewardInter handler loadAd RewardedInter key = $adsRewardedInterId")
        RewardedInterstitialAd.load(context,
            adsRewardedInterId,
            buildAdRequest(),
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(rewardedAd: RewardedInterstitialAd) {
                    _rewardedInterAds = rewardedAd
                    isLoadingRewardedInter = false
                    retryLoadAds = true
                    Logger.w("AdManager ---->RewardInter handler RewardedInter load success")
                    listener.onAdLoaded()
                    listener.onLoadFinish()
                    TrackingUtils.logEvent(EventUtils.REWARD_INTERSTITIAL + "load_success")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingRewardedInter = false
                    Logger.w("AdManager ----> RewardInter handler RewardedInter load fail")
                    listener.onLoadAdsError()
                    listener.onLoadFinish()
                    TrackingUtils.logEvent(EventUtils.REWARD_INTERSTITIAL + "load_fail")
                }
            })
    }

    override fun showAd(
        activity: Activity,
        rwdTime: Int?,
        listener: AdsShowerListener
    ) {
        setActivity(activity)
        val condition = true
        if (_rewardedInterAds == null || !condition || isLoadingRewardedInter || !AdsConfig.supportRewardedInterstitial) {
            listener.onShowAdsError()
            return
        }
        _rewardedInterAds?.setOnPaidEventListener {
            adsPreferencesHelper.saveTroasCache(
                it.valueMicros / 1000000.0,
                it.currencyCode
            )
        }
        _rewardedInterAds?.show(activity, onUserEarnedListener(listener))
        _rewardedInterAds?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                release()
                listener.onAdDismissedFullScreenContent()
            }

            override fun onAdClicked() {
                listener.onAdClicked()
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                release()
                listener.onAdFailedToShowFullScreenContent(p0)
            }

            override fun onAdImpression() {
                listener.onAdImpression()
            }

            override fun onAdShowedFullScreenContent() {
                listener.onAdShowedFullScreenContent()
            }
        }
        _rewardedInterAds?.onPaidEventListener = OnPaidEventListener { adValue ->
            adsPreferencesHelper.saveTroasCache(
                adValue.valueMicros / 1000000.0,
                adValue.currencyCode
            )
        }
    }

    override fun handleAutoLoadAds(count: Int, isDelay: Boolean) {
    }

    override fun release() {
        _rewardedInterAds = null
    }

    fun hasRewardedInterAds(): Boolean {
        return _rewardedInterAds != null
    }

    private fun onUserEarnedListener(listener: AdsShowerListener) = OnUserEarnedRewardListener {
        isEarned = true
        listener.onRewardedEarned(it)
    }

}