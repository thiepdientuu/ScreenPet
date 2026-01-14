package com.tp.ads.rewarded

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.tp.ads.base.AdsConfig
import com.tp.ads.base.AdsLoaderListener
import com.tp.ads.base.AdsShowerListener
import com.tp.ads.base.BaseLoader
import com.tp.ads.utils.AppSession
import com.tp.ads.utils.EventUtils
import com.tp.ads.utils.Logger
import com.tp.ads.utils.TrackingUtils
import java.lang.ref.WeakReference

class RewardedAdsLoader(
    val context: Context,
) : BaseLoader(context) {

    private var adsRewardedId: String = ""
    private var adsRewardedHighId: String = ""
    private var _rewardedAd: RewardedAd? = null
    private var _rewardedAdWithKey: RewardedAd? = null
    private var activityWeakRef: WeakReference<Activity>? = null
    private var rwdEarned: Boolean? = null
    private var rwdTimeShow: Int = 0

    var isLoadingRewarded = false
    private var isForceLoadRewarded = false

    fun setAdsRewardedId(adsRewardedId: String, adsRewardedHighId : String) {
        this.adsRewardedId = adsRewardedId
        this.adsRewardedHighId = adsRewardedHighId
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
            || _rewardedAd != null || isLoadingRewarded || !AdsConfig.supportRewarded
        ) {
            Logger.w("AdManager", "RewardAdLoader -----> Return rewarded not load")
            listener.onLoadFinish()
            return
        }

        isLoadingRewarded = true
        isForceLoadRewarded = isForceLoad

        Logger.w(TAG, "RewardAdLoader -----> handler load RewardedAds")
        RewardedAd.load(
            context,
            adsRewardedId,
            buildAdRequest(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    Logger.w("AdManager", "RewardAdLoader -----> rewarded load success")
                    _rewardedAd = rewardedAd
                    isLoadingRewarded = false
                    listener.onAdLoaded()
                    listener.onLoadFinish()
                    TrackingUtils.logEvent(EventUtils.REWARD_VIDEO + "load_success")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Logger.w("AdManager", "RewardAdLoader -----> rewarded load fail")
                    _rewardedAd = null
                    isLoadingRewarded = false
                    listener.onLoadAdsError()
                    listener.onLoadFinish()
                    TrackingUtils.logEvent(EventUtils.REWARD_VIDEO + "load_fail")
                }
            })
    }

  fun loadRewardedAdHigh(listener: AdsLoaderListener, isForceLoad: Boolean) {
        if (!AppSession.canRequestAd || AppSession.isVipUser
            || _rewardedAd != null || isLoadingRewarded || !AdsConfig.supportRewarded
        ) {
            Logger.w("AdManager", "RewardAdLoader -----> Return rewarded HIGH not load")
            listener.onLoadFinish()
            return
        }

        isLoadingRewarded = true
        isForceLoadRewarded = isForceLoad

        Logger.w(TAG, "RewardAdLoader -----> handler load RewardedAds HIGH")
        RewardedAd.load(
            context,
            adsRewardedHighId.ifEmpty { DEFAULT_HIGH_AD_UNIT },
            buildAdRequest(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    Logger.w("AdManager", "RewardAdLoader -----> rewarded HIGH load success")
                    _rewardedAd = rewardedAd
                    isLoadingRewarded = false
                    listener.onAdLoaded()
                    listener.onLoadFinish()
                    TrackingUtils.logEvent(EventUtils.REWARD_VIDEO_HIGH + "load_success")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Logger.w("AdManager", "RewardAdLoader -----> rewarded HIGH load fail")
                    _rewardedAd = null
                    isLoadingRewarded = false
                    listener.onLoadAdsError()
                    listener.onLoadFinish()
                    TrackingUtils.logEvent(EventUtils.REWARD_VIDEO_HIGH + "load_fail")
                }
            })
    }

    override fun showAd(
        activity: Activity,
        rwdTime: Int?,
        listener: AdsShowerListener
    ) {
        setActivity(activity)
        if (_rewardedAd == null || isLoadingRewarded || !AdsConfig.supportRewarded) {
            listener.onShowAdsError()
            handleAutoLoadAds(0, isDelay = false)
            recordRewardAd()
            return
        }
        rwdTimeShow = rwdTime ?: 0
        _rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Logger.w(TAG, "-----onAdDismissedFullScreenContent")
                release()
                listener.onAdDismissedFullScreenContent()
                handleAutoLoadAds(0, isDelay = false)
                recordRewardAd()
            }

            override fun onAdShowedFullScreenContent() {
                Logger.w(TAG, "-----onAdShowedFullScreenContent")
                listener.onAdShowedFullScreenContent()
            }

            override fun onAdClicked() {
                listener.onAdClicked()
            }

            override fun onAdImpression() {
                listener.onAdImpression()
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                Logger.w(TAG, "-----onAdFailedToShowFullScreenContent :${p0.message}")
                listener.onAdFailedToShowFullScreenContent(p0)
                release()
                handleAutoLoadAds(0, isDelay = false)
                recordRewardAd()
            }
        }
        _rewardedAd?.show(activity, onUserEarnedListener(listener))
        _rewardedAd?.onPaidEventListener = OnPaidEventListener { adValue ->
            adsPreferencesHelper.saveTroasCache(
                adValue.valueMicros / 1000000.0,
                adValue.currencyCode
            )
        }
    }

    fun loadAdWithKey(listener: AdsLoaderListener, isForceLoad: Boolean, adsKey: String) {
        if (!AppSession.canRequestAd || AppSession.isVipUser
            || _rewardedAdWithKey != null || isLoadingRewarded
        ) {
            Logger.w("AdManager", "-----rewarded not load")
            listener.onLoadFinish()
            return
        }

        isLoadingRewarded = true
        isForceLoadRewarded = isForceLoad

        Logger.w(TAG, "-----handler load RewardedAds")
        RewardedAd.load(
            context,
            adsKey,
            buildAdRequest(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    Logger.w("AdManager", "-----rewarded load success")
                    _rewardedAdWithKey = rewardedAd
                    isLoadingRewarded = false
                    listener.onAdLoaded()
                    listener.onLoadFinish()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Logger.w("AdManager", "-----rewarded load fail")
                    _rewardedAdWithKey = null
                    isLoadingRewarded = false
                    listener.onLoadAdsError()
                    listener.onLoadFinish()
                }
            })
    }

    fun showAdWithKey(
        activity: Activity,
        key: String,
        listener: AdsShowerListener
    ) {
        setActivity(activity)
        if (_rewardedAdWithKey == null || isLoadingRewarded) {
            listener.onShowAdsError()
            recordRewardAd(key = key)
            return
        }
        _rewardedAdWithKey?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Logger.w(TAG, "-----onAdDismissedFullScreenContent")
                release()
                listener.onAdDismissedFullScreenContent()
                _rewardedAdWithKey = null
                recordRewardAd(key = key)
            }

            override fun onAdShowedFullScreenContent() {
                Logger.w(TAG, "-----onAdShowedFullScreenContent")
                listener.onAdShowedFullScreenContent()
            }

            override fun onAdClicked() {
                listener.onAdClicked()
            }

            override fun onAdImpression() {
                listener.onAdImpression()
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                Logger.w(TAG, "-----onAdFailedToShowFullScreenContent :${p0.message}")
                listener.onAdFailedToShowFullScreenContent(p0)
                _rewardedAdWithKey = null
                release()
                recordRewardAd(key = key)
            }
        }
        _rewardedAdWithKey?.show(activity, onUserEarnedListener(listener))
        _rewardedAdWithKey?.onPaidEventListener = OnPaidEventListener { adValue ->
            adsPreferencesHelper.saveTroasCache(
                adValue.valueMicros / 1000000.0,
                adValue.currencyCode
            )
        }
    }

    private fun onUserEarnedListener(listener: AdsShowerListener) = OnUserEarnedRewardListener {
        rwdEarned = true
        listener.onRewardedEarned(it)
    }

    override fun handleAutoLoadAds(count: Int, isDelay: Boolean) {
    }

    fun hasRewardedAds(): Boolean {
        return _rewardedAd != null
    }

    fun isRewardedAdsEarned(): Boolean {
        return rwdEarned ?: false
    }

    override fun release() {
        _rewardedAd = null
    }

    fun recordRewardAd(
        key: String? = adsRewardedId
    ) {
    }

    companion object {
        const val TAG = "RewardedAdsLoader"
        const val DEFAULT_HIGH_AD_UNIT = "ca-app-pub-4945756407745123/8225329203"
    }

}
