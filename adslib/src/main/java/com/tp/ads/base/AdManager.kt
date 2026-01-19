package com.tp.ads.base

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.rewarded.RewardItem
import com.tp.ads.banner.BannerLoader
import com.tp.ads.cmp.GoogleMobileAdsConsentManager
import com.tp.ads.intersititial.IntersAdsLoader
import com.tp.ads.natives.NativeAdsLoaderListener
import com.tp.ads.natives.NativeListLoader
import com.tp.ads.natives.NativeLoader
import com.tp.ads.natives.TypeNativeAds
import com.tp.ads.openads.OpenAdsLoader
import com.tp.ads.pref.AdsPreferencesHelper
import com.tp.ads.rewarded.RewardedAdsLoader
import com.tp.ads.rewardedinters.RewardedInterAdsLoader
import com.tp.ads.utils.AdCommonUtils
import com.tp.ads.utils.AdsUtils
import com.tp.ads.utils.AppSession
import com.tp.ads.utils.EventUtils
import com.tp.ads.utils.Logger
import com.tp.ads.utils.TrackingUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AdManager @Inject constructor(
    @ApplicationContext val context: Context,
) {

    companion object {
        const val TAG = "AdManager"
        const val DEFAULT_INTER_SPLASH_KEY_HIGH = "ca-app-pub-3940256099942544/1033173712"
    }

    private var canRequestAd = true
    private var isVipUser = false
    val interAdsLoader = IntersAdsLoader(context)
    val rewardedAdsLoader = RewardedAdsLoader(context)
    val rewardedIntersAdsLoader = RewardedInterAdsLoader(context)
    val openAdsLoader = OpenAdsLoader(context)
    var nativeAgeV1 = MutableLiveData<NativeAd>()
    var nativeAgeV2 = MutableLiveData<NativeAd>()
    var nativeLanguage = MutableLiveData<NativeAd>()
    var nativeHome = MutableLiveData<NativeAd>()
    var nativeDownload = MutableLiveData<NativeAd>()
    var nativeDownloading = MutableLiveData<NativeAd>()
    var nativeLanguageV2 = MutableLiveData<NativeAd>()
    var nativeSuccessRingtone = MutableLiveData<NativeAd>()
    var nativeDownloadRingtone = MutableLiveData<NativeAd>()
    var nativeIntroFullScreen = MutableLiveData<NativeAd>()
    var nativeIntro1 = MutableLiveData<NativeAd>()
    var nativeIntro2 = MutableLiveData<NativeAd>()
    var interSplash: InterstitialAd? = null
    var openAdSplash: AppOpenAd? = null
    var adUnitInterSplash = ""
    var adUnitInterSplashHigh = ""
    var adUnitNativeHome = ""
    var adUnitNativeHomeHigh = ""
    var adUnitOpenSplash = ""
    var openAdSplashLoadFail = false
    var interSplashLoadFail = false
    var nativeAgeLoadFail = false
    var supportLoadCacheInter = true
    val adsPreferencesHelper = AdsPreferencesHelper(context)
    val googleMobileAdsConsentManager = GoogleMobileAdsConsentManager(context)
    var didShowNativeIntro2 = false
    var didShowNativeIntro1 = false

    fun checkConsent(
        activity: Activity,
        gatheringCompleteListener: GoogleMobileAdsConsentManager.OnConsentGatheringCompleteListener,
        beforeInitMobileAds: (() -> Unit)? = null,
        initMobileAdSuccess: (() -> Unit)? = null
    ) {
        TrackingUtils.init(activity)
        if (googleMobileAdsConsentManager.canRequestAds) {
            initializeAds(beforeInitMobileAds, initMobileAdSuccess)
            return
        }
        googleMobileAdsConsentManager.gatherConsent(
            activity
        ) { error ->
            canRequestAd = googleMobileAdsConsentManager.canRequestAds
            AppSession.canRequestAd = googleMobileAdsConsentManager.canRequestAds
            gatheringCompleteListener.consentGatheringComplete(error)
            if (googleMobileAdsConsentManager.canRequestAds) {
                Logger.w("AdManager", "##### Init MobileAds SDK after finish gatherConsent")
                initializeAds(beforeInitMobileAds, initMobileAdSuccess)
            }
        }
    }

    private fun initializeAds(
        beforeInitMobileAds: (() -> Unit)?,
        initMobileAdSuccess: (() -> Unit)? = null
    ) {
        beforeInitMobileAds?.invoke()
        MobileAds.initialize(context) {
            loadInterSplashHigh()
            initMobileAdSuccess?.invoke()
            Logger.w(TAG, "AdManager-----initializeAds success")
        }
    }

    fun loadOpenAdSplash() {
        if (!AppSession.canRequestAd || AppSession.isVipUser
            || openAdSplash != null
        ) {
            Logger.w("AdManager", "-----open ad splash not load")
            return
        }
        Logger.w(IntersAdsLoader.TAG, "-----handler load open ad splash")
        val adRequest = buildAdRequest()
        AppOpenAd.load(
            context,
            adUnitOpenSplash,
            adRequest,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Logger.w("AdManager", "-----openAds splash load success")
                    openAdSplash = ad
                    openAdSplashLoadFail = false
                    TrackingUtils.logEvent(EventUtils.OPEN_AD + "splash_load_success")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Logger.w("AdManager", "-----openAds splash load fail :${loadAdError.message}")
                    openAdSplash = null
                    openAdSplashLoadFail = true
                    TrackingUtils.logEvent(EventUtils.OPEN_AD + "splash_load_fail")
                }
            })
    }

    fun showOpenAdSplash(activity: Activity, onFinishShow: () -> Unit) {
        if (!AppSession.canRequestAd || AppSession.isVipUser || openAdSplash == null) {
            onFinishShow.invoke()
            return
        }
        openAdSplash?.setOnPaidEventListener {
            adsPreferencesHelper.saveTroasCache(
                it.valueMicros / 1000000.0,
                it.currencyCode
            )
        }
        openAdSplash?.show(activity)
        openAdSplash?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                openAdSplash = null
                onFinishShow.invoke()
            }

            override fun onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent()
                TrackingUtils.logEvent(EventUtils.OPEN_AD + "splash_show_success")
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                super.onAdFailedToShowFullScreenContent(p0)
                onFinishShow.invoke()
            }
        }
    }

    private fun loadInterSplash() {
        if (!AppSession.canRequestAd || AppSession.isVipUser
            || interSplash != null
        ) {
            Logger.w("AdManager", "-----inter splash not load")
            return
        }
        Logger.w(IntersAdsLoader.TAG, "AdManager -----handler load InterAds splash")
        val adRequest = buildAdRequest()
        InterstitialAd.load(context, AdCommonUtils.INTER_SPLASH_NORMAL_KEY, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                interSplash = null
                interSplashLoadFail = true
                TrackingUtils.logEvent(EventUtils.INTERSTITIAL + "splash_load_fail")
                Logger.w("AdManager", "AdManager-----inter splash load fail :${adError.message}")
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                interSplash = interstitialAd
                interSplashLoadFail = false
                TrackingUtils.logEvent(EventUtils.INTERSTITIAL + "splash_load_success")
                Logger.w("AdManager", "AdManager-----inter splash load success")
            }
        })
    }

    private fun loadInterSplashHigh() {
        if (!AppSession.canRequestAd || AppSession.isVipUser
            || interSplash != null
        ) {
            Logger.w("AdManager", "-----inter splash HIGH not load")
            return
        }
        Logger.w(IntersAdsLoader.TAG, "AdManager -----handler load InterAds splash HIGH")
        val adRequest = buildAdRequest()
        InterstitialAd.load(
            context,
            AdCommonUtils.INTER_SPLASH_HIGH_KEY,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interSplash = null
                    loadInterSplash()
                    TrackingUtils.logEvent(EventUtils.INTERSTITIAL_HIGH + "splash_load_fail")
                    Logger.w("AdManager", "-----inter splash HIGH load fail :${adError.message}")
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    interSplash = interstitialAd
                    TrackingUtils.logEvent(EventUtils.INTERSTITIAL_HIGH + "splash_load_success")
                    Logger.w("AdManager", "-----inter splash HIGH load success")
                }
            })
    }

    fun showInterSplash(activity: Activity, onFinishShow: () -> Unit) {
        if (!AppSession.canRequestAd || AppSession.isVipUser || interSplash == null) {
            onFinishShow.invoke()
            return
        }
        interSplash?.setOnPaidEventListener {
            adsPreferencesHelper.saveTroasCache(
                it.valueMicros / 1000000.0,
                it.currencyCode
            )
        }
        interSplash?.show(activity)
        interSplash?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interSplash = null
                updateLastTimeShowInter()
                onFinishShow.invoke()
            }

            override fun onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent()
                TrackingUtils.logEvent(EventUtils.INTERSTITIAL + "splash_show_success")
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                super.onAdFailedToShowFullScreenContent(p0)
                onFinishShow.invoke()
            }
        }
    }

    fun setActivity(activity: Activity) {
        openAdsLoader.setActivity(activity)
        interAdsLoader.setActivity(activity)
        rewardedIntersAdsLoader.setActivity(activity)
        rewardedIntersAdsLoader.setActivity(activity)
    }

    fun setOnOffAds(inter: Boolean, rewarded: Boolean, banner: Boolean, openAd: Boolean) {

    }

    fun setUnitAdsLibs(
        adsUnitInter: String,
        adsUnitInterHigh: String,
        adsUnitInterSplash: String,
        adsUnitReward: String,
        adsUnitRewardInter: String,
        adsUnitOpenAd: String,
        adsUnitOpenAdHigh: String,
        adsUnitOpenAdSplash: String,
        adsUnitRewardHigh: String,
        adUnitInterSplashHigh: String,
        adUnitNativeHomeHigh: String,
        adUnitNativeHome: String
    ) {
        Logger.w("AdManager", "------------ > setUnitAdsLibs")

        interAdsLoader.setAdsInterId(adsUnitInter, adsUnitInterHigh)
        this.adUnitInterSplash = adsUnitInterSplash
        this.adUnitInterSplashHigh = adUnitInterSplashHigh
        this.adUnitOpenSplash = adsUnitOpenAdSplash
        this.adUnitNativeHomeHigh = adUnitNativeHomeHigh
        this.adUnitNativeHome = adUnitNativeHome
        rewardedAdsLoader.setAdsRewardedId(adsUnitReward, adsUnitRewardHigh)
        rewardedIntersAdsLoader.setAdsRewardedInterId(adsUnitRewardInter)
        openAdsLoader.setAdsOpenId(adsOpenId = adsUnitOpenAd, adsOpenSplashId = adsUnitOpenAdSplash, adOpenHigh = adsUnitOpenAdHigh)
    }

    fun setVipUser(isVip: Boolean) {
        isVipUser = isVip
        AppSession.isVipUser = isVip
    }

    fun setInterAdsTimeDelay(timeDelay: Int) {
        interAdsLoader.setIntervalShowInter(timeDelay)
    }

    fun releaseAllAds() {
        interAdsLoader.release()
        rewardedAdsLoader.release()
        rewardedIntersAdsLoader.release()
        openAdsLoader.release()
    }

    //----------------------------------------------------------------------------------------------
    fun loadInterAds() {
        interAdsLoader.loadInterHighAd(object : AdsLoaderListener() {
            override fun onLoadFinish() {
            }
        }, false)
    }

    fun loadCacheOpenAds() {
        openAdsLoader.loadAdHigh(object : AdsLoaderListener() {
            override fun onLoadFinish() {
            }
        })
    }

    private fun showRewardedInterWhenRewardedError(
        activity: Activity,
        listener: AdsShowerListener
    ) {
        showInterRewardedAds(activity, object : AdsShowerListener() {
            override fun onShowAdsError() {
                Logger.w("InterRewardedAdsLoader-----onShowAdsError")
                listener.onShowAdsError()
            }

            override fun onAdDismissedFullScreenContent() {
                listener.onAdDismissedFullScreenContent()
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                listener.onAdFailedToShowFullScreenContent(p0)
            }

            override fun onRewardedEarned(rewardedItem: RewardItem) {
                listener.onRewardedEarned(rewardedItem)
            }
        })
    }

    //--- Inter Ads---------------------------------------------------------------------------------
    fun hasInterAds() = interAdsLoader.hasInterAds()

    fun isLoadingInterAds() = interAdsLoader.isLoadingInter

    fun forceLoadInterAds(listener: AdsLoaderListener) {
        TrackingUtils.logEvent("inter_realtime_load_start")
        interAdsLoader.loadInterHighAd(listener, isForceLoad = true)
    }

    fun updateLastTimeShowInter() {
        interAdsLoader.updateLastTimeShowInter()
    }

    fun showInterAds(activity: Activity, listener: AdsShowerListener) {
        if (!canRequestAd || isVipUser) {
            listener.onShowAdsError()
            return
        }
        interAdsLoader.showAd(activity, null, object : AdsShowerListener() {
            override fun onShowAdsError() {
                listener.onShowAdsError()
            }

            override fun onAdDismissedFullScreenContent() {
                openAdsLoader.switchOnOff(true)
                listener.onAdDismissedFullScreenContent()
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                listener.onAdFailedToShowFullScreenContent(p0)
            }

            override fun onAdShowedFullScreenContent() {
                openAdsLoader.switchOnOff(false)
                listener.onAdShowedFullScreenContent()
            }

            override fun onAdClicked() {
                listener.onAdClicked()
            }

            override fun onAdImpression() {
                listener.onAdImpression()
            }

        })
    }

    //--- Rewarded Ads------------------------------------------------------------------------------
    fun loadCacheRewardedAds(onFinishLoad: (() -> Unit)? = null) {
        rewardedAdsLoader.loadRewardedAdHigh(object : AdsLoaderListener() {
            override fun onLoadFinish() {
                loadRewardAdsNormal()
                super.onLoadFinish()
                onFinishLoad?.invoke()
            }
        }, false)
    }

    fun loadRewardAdsNormal() {
        rewardedAdsLoader.loadAd(object : AdsLoaderListener() {}, false)
    }

    fun loadRewardedAdsRealtime(listener: AdsLoaderListener) {
        TrackingUtils.logEvent("rewarded_realtime_load_start")
        rewardedAdsLoader.loadAd(listener = listener, isForceLoad = true)
    }

    fun forceLoadRewardedAdsWithKey(key: String, listener: AdsLoaderListener) {
        rewardedAdsLoader.loadAdWithKey(listener, isForceLoad = true, key)
    }

    fun showRewardedAdsByKey(
        activity: Activity, key: String, listener: AdsShowerListener
    ) {
        if (!canRequestAd || isVipUser) {
            listener.onShowAdsError()
            return
        }
        rewardedAdsLoader.showAdWithKey(activity, key, object : AdsShowerListener() {
            override fun onShowAdsError() {
                Logger.w("RewardedAdsLoader-----onShowAdsError: doing show rewarded inter")
            }

            override fun onAdDismissedFullScreenContent() {
                openAdsLoader.switchOnOff(true)
                listener.onAdDismissedFullScreenContent()
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                Logger.w("RewardedAdsLoader-----onShowAdsError: doing show rewarded inter")
            }

            override fun onAdShowedFullScreenContent() {
                openAdsLoader.switchOnOff(false)
                listener.onAdShowedFullScreenContent()
            }

            override fun onAdClicked() {
                listener.onAdClicked()
            }

            override fun onRewardedEarned(rewardedItem: RewardItem) {
                listener.onRewardedEarned(rewardedItem)
            }

            override fun onAdImpression() {
                listener.onAdImpression()
            }
        })
    }

    fun isLoadingRewardedAds() = rewardedAdsLoader.isLoadingRewarded

    fun showRewardedAds(
        activity: Activity, timeShowRwd: Int, listener: AdsShowerListener
    ) {
        if (!canRequestAd || isVipUser) {
            listener.onShowAdsError()
            return
        }
        rewardedAdsLoader.showAd(activity, timeShowRwd, object : AdsShowerListener() {
            override fun onShowAdsError() {
                Logger.w("RewardedAdsLoader-----onShowAdsError: doing show rewarded inter")
                showRewardedInterWhenRewardedError(activity, listener)
            }

            override fun onAdDismissedFullScreenContent() {
                openAdsLoader.switchOnOff(true)
                listener.onAdDismissedFullScreenContent()
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                Logger.w("RewardedAdsLoader-----onShowAdsError: doing show rewarded inter")
                showRewardedInterWhenRewardedError(activity, listener)
            }

            override fun onAdShowedFullScreenContent() {
                openAdsLoader.switchOnOff(false)
                listener.onAdShowedFullScreenContent()
            }

            override fun onAdClicked() {
                listener.onAdClicked()
            }

            override fun onRewardedEarned(rewardedItem: RewardItem) {
                listener.onRewardedEarned(rewardedItem)
            }

            override fun onAdImpression() {
                listener.onAdImpression()
            }
        })
    }

    fun hasRewardedAds() = rewardedAdsLoader.hasRewardedAds()

    //--- Banner Ads------------------------------------------------------------------------
    fun loadBannerAds(
        adUnit: String,
        adsUnitHigh: String,
        container: ViewGroup,
        showCollapsible: Boolean,
        autoRefresh: Boolean,
        timeDelayRefresh: Long = 30000L,
        isRefreshCollapsible: Boolean = false,
        widthBanner: Float = 1f,
        callback: (() -> Unit)? = null,
        supportHighFloor: Boolean = false
    ): BaseLoader {
        val bannerLoader = BannerLoader(
            context,
            autoRefresh,
            timeDelayRefresh,
        )
        bannerLoader.loadBannerAd(
            adUnit = adUnit,
            adUnitHigh = adsUnitHigh,
            container = container,
            showCollapsible = showCollapsible,
            isRefreshCollapsible = isRefreshCollapsible,
            widthBanner = widthBanner,
            callback = callback,
            supportHighFloor = supportHighFloor
        )
        return bannerLoader
    }

    //--- Native Ads------------------------------------------------------------------------
    fun buildAdRequest(
    ): AdManagerAdRequest {
        val request = AdManagerAdRequest.Builder()
        val extras = Bundle()
        if (!AppSession.isActiveServer) {
            extras.putString("max_ad_content_rating", "PG")
        }
        request.addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
        return request.build()
    }

    fun showNativeAd(adView: NativeAdView, container: ViewGroup, nativeAd: NativeAd) {
        try {
            container.visibility = View.VISIBLE
            AdsUtils.populateNative(nativeAd, adView)
            container.removeAllViews()
            container.addView(adView)
        } catch (e: Exception) {
            Logger.e(TAG, e)
        }
    }

    fun loadNativeAgeV1(adsUnit: String) {
        if (AppSession.isVipUser || !AppSession.canRequestAd || nativeAgeV1.value != null) return
        Logger.w(TAG, "NativeFlow ---> Start Cache loadNativeAds Age1 adUnit = $adsUnit")
        val videoOptions = VideoOptions.Builder().setStartMuted(true).build()
        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions).build()
        val adLoader = AdLoader.Builder(context, adsUnit)
            .forNativeAd { ad ->
                ad.setOnPaidEventListener {
                    adsPreferencesHelper.saveTroasCache(
                        it.valueMicros / 1000000.0,
                        it.currencyCode
                    )
                }
                nativeAgeV1.postValue(ad)
            }
            .withAdListener(object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    nativeAgeLoadFail = false
                    TrackingUtils.logEvent(EventUtils.NATIVE + "age1_load_success")
                    Logger.w(TAG, "NativeFlow ---> NativeAds Age1 onAdLoaded ")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    nativeAgeLoadFail = true
                    TrackingUtils.logEvent(EventUtils.NATIVE + "age1_load_fail")
                    Logger.w(TAG, "NativeFlow ---> NativeAds Age1  onAdFailedToLoad: ${adError.message}")
                }

                override fun onAdImpression() {
                    Logger.w(TAG, "NativeFlow ---> NativeAds Age1  onAdImpression")
                    TrackingUtils.logEvent(EventUtils.NATIVE + "age1_impression")
                }
            })
            .withNativeAdOptions(adOptions)
            .build()

        // Load the ad
        adLoader.loadAd(buildAdRequest())
    }

    fun loadNativeAgeV2(adsUnit: String, adUnitHigh: String, onHighFloor: Boolean) {
        if (AppSession.isVipUser || !AppSession.canRequestAd) return
        Logger.w(
            TAG,
            "NativeFlow ---> Start Cache loadNativeAds Age V2 ${if (onHighFloor) "high floor" else "low floor"} adUnit = $adsUnit"
        )
        val videoOptions = VideoOptions.Builder().setStartMuted(true).build()
        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions).build()
        val adLoader = AdLoader.Builder(context, if (onHighFloor) adUnitHigh else adsUnit)
            .forNativeAd { ad ->
                ad.setOnPaidEventListener {
                    adsPreferencesHelper.saveTroasCache(
                        it.valueMicros / 1000000.0,
                        it.currencyCode
                    )
                }
                nativeAgeV2.postValue(ad)
            }
            .withAdListener(object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    TrackingUtils.logEvent(EventUtils.NATIVE + "age_v2_load_success" + if (onHighFloor) "_high" else "_low")
                    Logger.w(TAG, "NativeFlow ---> NativeAds  Age V2 ${if (onHighFloor) "high floor" else "low floor"}  onAdLoaded")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    TrackingUtils.logEvent(EventUtils.NATIVE + "age_v2_load_fail" + if (onHighFloor) "_high" else "_low")
                    if (onHighFloor) {
                        loadNativeAgeV2(
                            adsUnit = adsUnit,
                            adUnitHigh = adUnitHigh,
                            onHighFloor = false
                        )
                    }
                    Logger.w(
                        TAG,
                        "NativeFlow ---> NativeAds  Age V2 ${if (onHighFloor) "high floor" else "low floor"}  onAdFailedToLoad ${adError.message}"
                    )
                }

                override fun onAdImpression() {
                    Logger.w(TAG, "NativeFlow ---> NativeAds  Age V2 ${if (onHighFloor) "high floor" else "low floor"}  onAdImpression")
                    TrackingUtils.logEvent(EventUtils.NATIVE + "age_v2_impression" + if (onHighFloor) "_high" else "_low")
                }
            })
            .withNativeAdOptions(adOptions)
            .build()

        // Load the ad
        adLoader.loadAd(buildAdRequest())
    }

    fun loadNativeLanguage(adsUnit: String) {
        if (AppSession.isVipUser || !AppSession.canRequestAd) return
        Logger.w(TAG, "NativeFlow ---> Start loadNativeAds language adUnit = $adsUnit")
        val videoOptions = VideoOptions.Builder().setStartMuted(true).build()
        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions).build()
        val adLoader = AdLoader.Builder(context, adsUnit)
            .forNativeAd { ad ->
                ad.setOnPaidEventListener {
                    adsPreferencesHelper.saveTroasCache(
                        it.valueMicros / 1000000.0,
                        it.currencyCode
                    )
                }
                nativeLanguage.postValue(ad)
            }
            .withAdListener(object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    TrackingUtils.logEvent(EventUtils.NATIVE + "language_load_success")
                    Logger.w(TAG, "NativeFlow ---> NativeAds  language onAdLoaded")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    TrackingUtils.logEvent(EventUtils.NATIVE + "language_load_fail")
                    Logger.w(TAG, "NativeFlow ---> NativeAds  language   onAdFailedToLoad ${adError.message}")
                }

                override fun onAdImpression() {
                    Logger.w(TAG, "NativeFlow ---> NativeAds  language  onAdImpression")
                    TrackingUtils.logEvent(EventUtils.NATIVE + "language_impression")
                }
            })
            .withNativeAdOptions(adOptions)
            .build()

        // Load the ad
        adLoader.loadAd(buildAdRequest())
    }

    fun loadNativeHomeHigh(adsUnit: String) {
        if (AppSession.isVipUser || !AppSession.canRequestAd) return
        Logger.w(TAG, "NativeFlow ---> Start loadNativeAds home high adUnit = $adsUnit")
        val videoOptions = VideoOptions.Builder().setStartMuted(true).build()
        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions).build()
        val adLoader = AdLoader.Builder(context, adsUnit)
            .forNativeAd { ad ->
                ad.setOnPaidEventListener {
                    adsPreferencesHelper.saveTroasCache(
                        it.valueMicros / 1000000.0,
                        it.currencyCode
                    )
                }
                nativeHome.postValue(ad)
            }
            .withAdListener(object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    TrackingUtils.logEvent(EventUtils.NATIVE + "home_high_load_success")
                    Logger.w(TAG, "NativeFlow ---> NativeAds  home high onAdLoaded")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    TrackingUtils.logEvent(EventUtils.NATIVE + "home_high_load_fail")
                    Logger.w(TAG, "NativeFlow ---> NativeAds  home high   onAdFailedToLoad ${adError.message}")
                    loadNativeHome(adUnitNativeHome)
                }

                override fun onAdImpression() {
                    Logger.w(TAG, "NativeFlow ---> NativeAds  home high  onAdImpression")
                    TrackingUtils.logEvent(EventUtils.NATIVE + "home_high_impression")
                }
            })
            .withNativeAdOptions(adOptions)
            .build()

        // Load the ad
        adLoader.loadAd(buildAdRequest())
    }

    fun loadNativeHome(adsUnit: String) {
        if (AppSession.isVipUser || !AppSession.canRequestAd || nativeHome.value != null) return
        Logger.w(TAG, "NativeFlow ---> Start loadNativeAds language adUnit = $adsUnit")
        val videoOptions = VideoOptions.Builder().setStartMuted(true).build()
        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions).build()
        val adLoader = AdLoader.Builder(context, adsUnit)
            .forNativeAd { ad ->
                ad.setOnPaidEventListener {
                    adsPreferencesHelper.saveTroasCache(
                        it.valueMicros / 1000000.0,
                        it.currencyCode
                    )
                }
                nativeHome.postValue(ad)
            }
            .withAdListener(object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    TrackingUtils.logEvent(EventUtils.NATIVE + "home_load_success")
                    Logger.w(TAG, "NativeFlow ---> NativeAds  home onAdLoaded")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    TrackingUtils.logEvent(EventUtils.NATIVE + "home_load_fail")
                    Logger.w(TAG, "NativeFlow ---> NativeAds  home   onAdFailedToLoad ${adError.message}")
                }

                override fun onAdImpression() {
                    Logger.w(TAG, "NativeFlow ---> NativeAds  home  onAdImpression")
                    TrackingUtils.logEvent(EventUtils.NATIVE + "home_impression")
                }
            })
            .withNativeAdOptions(adOptions)
            .build()

        // Load the ad
        adLoader.loadAd(buildAdRequest())
    }

    fun loadNativeDownload(adsUnit: String) {
        if (AppSession.isVipUser || !AppSession.canRequestAd) return
        Logger.w(TAG, "NativeFlow ---> Start loadNativeAds nativeDownload adUnit = $adsUnit")
        val videoOptions = VideoOptions.Builder().setStartMuted(true).build()
        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions).build()
        val adLoader = AdLoader.Builder(context, adsUnit)
            .forNativeAd { ad ->
                ad.setOnPaidEventListener {
                    adsPreferencesHelper.saveTroasCache(
                        it.valueMicros / 1000000.0,
                        it.currencyCode
                    )
                }
                nativeDownload.postValue(ad)
            }
            .withAdListener(object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    TrackingUtils.logEvent(EventUtils.NATIVE + "download_load_success")
                    Logger.w(TAG, "NativeFlow ---> NativeAds  nativeDownload onAdLoaded")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    TrackingUtils.logEvent(EventUtils.NATIVE + "download_load_fail")
                    Logger.w(TAG, "NativeFlow ---> NativeAds  nativeDownload   onAdFailedToLoad ${adError.message}")
                }

                override fun onAdImpression() {
                    Logger.w(TAG, "NativeFlow ---> NativeAds  nativeDownload  onAdImpression")
                    TrackingUtils.logEvent(EventUtils.NATIVE + "download_impression")
                }
            })
            .withNativeAdOptions(adOptions)
            .build()

        // Load the ad
        adLoader.loadAd(buildAdRequest())
    }

    fun loadNativeDownloading(adsUnit: String) {
        if (AppSession.isVipUser || !AppSession.canRequestAd) return
        if (!didShowNativeIntro2 && nativeIntro2.value != null) {
            nativeDownloading.postValue(nativeIntro2.value)
            Logger.w(TAG, "NativeFlow ---> Using native intro2 for NativeDownloading")
            return
        }
        Logger.w(TAG, "NativeFlow ---> Start loadNativeAds nativeDownload adUnit = $adsUnit")
        val videoOptions = VideoOptions.Builder().setStartMuted(true).build()
        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions).build()
        val adLoader = AdLoader.Builder(context, adsUnit)
            .forNativeAd { ad ->
                ad.setOnPaidEventListener {
                    adsPreferencesHelper.saveTroasCache(
                        it.valueMicros / 1000000.0,
                        it.currencyCode
                    )
                }
                nativeDownloading.postValue(ad)
            }
            .withAdListener(object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    TrackingUtils.logEvent(EventUtils.NATIVE + "downloading_load_success")
                    Logger.w(TAG, "NativeFlow ---> NativeAds  downloading onAdLoaded")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    TrackingUtils.logEvent(EventUtils.NATIVE + "downloading_load_fail")
                    Logger.w(TAG, "NativeFlow ---> NativeAds  downloading   onAdFailedToLoad ${adError.message}")
                }

                override fun onAdImpression() {
                    Logger.w(TAG, "NativeFlow ---> NativeAds  downloading  onAdImpression")
                    TrackingUtils.logEvent(EventUtils.NATIVE + "downloading_impression")
                }
            })
            .withNativeAdOptions(adOptions)
            .build()

        // Load the ad
        adLoader.loadAd(buildAdRequest())
    }

    fun loadNativeLanguageV2(adsUnit: String, adsUnitHigh: String, onHighFloor: Boolean) {
        if (AppSession.isVipUser || !AppSession.canRequestAd) return
        Logger.w(
            TAG,
            "NativeFlow ---> Start loadNativeAds language v2 ${if (onHighFloor) "high floor" else "low floor"}  adUnit = $adsUnit"
        )
        val videoOptions = VideoOptions.Builder().setStartMuted(true).build()
        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions).build()
        val adLoader = AdLoader.Builder(context, if (onHighFloor) adsUnitHigh else adsUnit)
            .forNativeAd { ad ->
                ad.setOnPaidEventListener {
                    adsPreferencesHelper.saveTroasCache(
                        it.valueMicros / 1000000.0,
                        it.currencyCode
                    )
                }
                nativeLanguageV2.postValue(ad)
            }
            .withAdListener(object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    TrackingUtils.logEvent(EventUtils.NATIVE + "language_v2_load_success" + if (onHighFloor) "_high" else "_low")
                    Logger.w(TAG, "NativeFlow ---> NativeAds  language V2 ${if (onHighFloor) "high floor" else "low floor"}  onAdLoaded")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    TrackingUtils.logEvent(EventUtils.NATIVE + "language_v2_load_fail" + if (onHighFloor) "_high" else "_low")
                    if (onHighFloor) {
                        loadNativeLanguageV2(
                            adsUnit = adsUnit,
                            adsUnitHigh = adsUnitHigh,
                            onHighFloor = false
                        )
                    }
                    Logger.w(
                        TAG,
                        "NativeFlow ---> NativeAds  language V2 ${if (onHighFloor) "high floor" else "low floor"}  onAdFailedToLoad ${adError.message}"
                    )
                }

                override fun onAdImpression() {
                    Logger.w(
                        TAG,
                        "NativeFlow ---> NativeAds  language V2 ${if (onHighFloor) "high floor" else "low floor"}  onAdImpression"
                    )
                    TrackingUtils.logEvent(EventUtils.NATIVE + "language_v2_impression" + if (onHighFloor) "_high" else "_low")
                }
            })
            .withNativeAdOptions(adOptions)
            .build()

        // Load the ad
        adLoader.loadAd(buildAdRequest())
    }

    fun loadNativeIntroFullScreen(adsUnit: String, adsUnitHigh: String, onHighFloor: Boolean) {
        if (AppSession.isVipUser || !AppSession.canRequestAd || nativeIntroFullScreen.value != null) return
        Logger.w(TAG, "NativeFlow ---> Start loadNativeIntroFullScreen adUnit = $adsUnit")
        val videoOptions = VideoOptions.Builder().setStartMuted(true).build()
        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions).build()
        val adLoader = AdLoader.Builder(context, if (onHighFloor) adsUnitHigh else adsUnit)
            .forNativeAd { ad ->
                ad.setOnPaidEventListener {
                    adsPreferencesHelper.saveTroasCache(
                        it.valueMicros / 1000000.0,
                        it.currencyCode
                    )
                }
                nativeIntroFullScreen.postValue(ad)
            }
            .withAdListener(object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    TrackingUtils.logEvent(EventUtils.NATIVE + "NativeIntroFullScreen_load_success")
                    Logger.w(TAG, "NativeFlow ---> NativeAds  NativeIntroFullScreen onAdLoaded")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    TrackingUtils.logEvent(EventUtils.NATIVE + "NativeIntroFullScreen_load_fail")
                    if (onHighFloor) {
                        loadNativeIntroFullScreen(
                            adsUnitHigh = adsUnitHigh,
                            adsUnit = adsUnit,
                            onHighFloor = false
                        )
                    }
                    Logger.w(TAG, "NativeFlow ---> NativeAds  NativeIntroFullScreen   onAdFailedToLoad ${adError.message}")
                }

                override fun onAdImpression() {
                    Logger.w(TAG, "NativeFlow ---> NativeAds  NativeIntroFullScreen  onAdImpression")
                    TrackingUtils.logEvent(EventUtils.NATIVE + "NativeIntroFullScreen_impression")
                }
            })
            .withNativeAdOptions(adOptions)
            .build()

        // Load the ad
        adLoader.loadAd(buildAdRequest())
    }

    fun loadNativeIntro1(adsUnit: String) {
        if (AppSession.isVipUser || !AppSession.canRequestAd || nativeIntro1.value != null) return
        Logger.w(TAG, "NativeFlow ---> Start loadNativeAds nativeIntro1 adUnit = $adsUnit")
        val videoOptions = VideoOptions.Builder().setStartMuted(true).build()
        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions).build()
        val adLoader = AdLoader.Builder(context, adsUnit)
            .forNativeAd { ad ->
                ad.setOnPaidEventListener {
                    adsPreferencesHelper.saveTroasCache(
                        it.valueMicros / 1000000.0,
                        it.currencyCode
                    )
                }
                nativeIntro1.postValue(ad)
            }
            .withAdListener(object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    TrackingUtils.logEvent(EventUtils.NATIVE + "nativeIntro1_load_success")
                    Logger.w(TAG, "NativeFlow ---> NativeAds  nativeIntro1 onAdLoaded")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    TrackingUtils.logEvent(EventUtils.NATIVE + "nativeIntro1_load_fail")
                    Logger.w(TAG, "NativeFlow ---> NativeAds  nativeIntro1   onAdFailedToLoad ${adError.message}")
                }

                override fun onAdImpression() {
                    Logger.w(TAG, "NativeFlow ---> NativeAds  nativeIntro1  onAdImpression")
                    didShowNativeIntro1 = true
                    TrackingUtils.logEvent(EventUtils.NATIVE + "nativeIntro1_impression")
                }
            })
            .withNativeAdOptions(adOptions)
            .build()

        // Load the ad
        adLoader.loadAd(buildAdRequest())
    }

    fun loadNativeIntro2(adsUnit: String) {
        if (AppSession.isVipUser || !AppSession.canRequestAd || nativeIntro2.value != null) return
        Logger.w(TAG, "NativeFlow ---> Start loadNativeAds nativeIntro2 adUnit = $adsUnit")
        val videoOptions = VideoOptions.Builder().setStartMuted(true).build()
        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions).build()
        val adLoader = AdLoader.Builder(context, adsUnit)
            .forNativeAd { ad ->
                ad.setOnPaidEventListener {
                    adsPreferencesHelper.saveTroasCache(
                        it.valueMicros / 1000000.0,
                        it.currencyCode
                    )
                }
                nativeIntro2.postValue(ad)
            }
            .withAdListener(object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    TrackingUtils.logEvent(EventUtils.NATIVE + "nativeIntro2_load_success")
                    Logger.w(TAG, "NativeFlow ---> NativeAds  nativeIntro2 onAdLoaded")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    TrackingUtils.logEvent(EventUtils.NATIVE + "nativeIntro2_load_fail")
                    Logger.w(TAG, "NativeFlow ---> NativeAds  nativeIntro2   onAdFailedToLoad ${adError.message}")
                }

                override fun onAdImpression() {
                    Logger.w(TAG, "NativeFlow ---> NativeAds  nativeIntro2  onAdImpression")
                    didShowNativeIntro2 = true
                    TrackingUtils.logEvent(EventUtils.NATIVE + "nativeIntro2_impression")
                }
            })
            .withNativeAdOptions(adOptions)
            .build()

        // Load the ad
        adLoader.loadAd(buildAdRequest())
    }

    fun loadNativeSuccessRing(adsUnit: String) {
        if (AppSession.isVipUser || !AppSession.canRequestAd || nativeSuccessRingtone.value != null) return
        Logger.w(TAG, "@@@@@ Start Cache lNativeSuccess adUnit = $adsUnit")
        val videoOptions = VideoOptions.Builder().setStartMuted(true).build()
        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions).build()
        val adLoader = AdLoader.Builder(context, adsUnit)
            .forNativeAd { ad ->
                ad.setOnPaidEventListener {
                    adsPreferencesHelper.saveTroasCache(
                        it.valueMicros / 1000000.0,
                        it.currencyCode
                    )
                }
                nativeSuccessRingtone.postValue(ad)
            }
            .withAdListener(object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    Logger.w(TAG, "@@@@@ NativeSuccess onAdLoaded")
                    TrackingUtils.logEvent(EventUtils.NATIVE + "congratulation_load_success")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Logger.w(TAG, "@@@@@@ NativeSuccess  onAdFailedToLoad")
                    TrackingUtils.logEvent(EventUtils.NATIVE + "congratulation_load_fail")
                }

                override fun onAdImpression() {
                    Logger.w(TAG, "@@@@@ NativeSuccess  onAdImpression")
                }
            })
            .withNativeAdOptions(adOptions)
            .build()

        // Load the ad
        adLoader.loadAd(buildAdRequest())
    }

    fun releaseNativeSuccess() {

    }

    fun loadNativeDownloadRing(adsUnit: String) {
        if (AppSession.isVipUser || !AppSession.canRequestAd || nativeDownloadRingtone.value != null) return
        Logger.w(TAG, "@@@@@ Start Cache NativeDownload adUnit = $adsUnit")
        val videoOptions = VideoOptions.Builder().setStartMuted(true).build()
        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions).build()
        val adLoader = AdLoader.Builder(context, adsUnit)
            .forNativeAd { ad ->
                nativeDownloadRingtone.postValue(ad)
            }
            .withAdListener(object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    Logger.w(TAG, "@@@@@ NativeDownload onAdLoaded")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Logger.w(TAG, "@@@@@@ NativeDownload  onAdFailedToLoad")
                }

                override fun onAdImpression() {
                    Logger.w(TAG, "@@@@@ NativeDownload  onAdImpression")
                }
            })
            .withNativeAdOptions(adOptions)
            .build()

        // Load the ad
        adLoader.loadAd(buildAdRequest())
    }

    fun releaseNativeDownload() {

    }

    fun loadNativeAds(
        adUnit: String,
        typeNativeAds: TypeNativeAds,
        container: ViewGroup,
        listener: NativeAdsLoaderListener,
        nativeAdView: NativeAdView? = null
    ): BaseLoader {
        val nativeLoader = NativeLoader(context)
        nativeLoader.loadNativeAds(
            adUnit = adUnit,
            typeNativeAds = typeNativeAds,
            container = container,
            listener = listener,
            nativeAdView = nativeAdView
        )
        return nativeLoader
    }

    fun getNativeListLoader(
        retryLoad: Int = NativeListLoader.DEFAULT_RETRY_LOAD_NATIVE,
        delayRetry: Long = NativeListLoader.DEFAULT_DELAY_RETRY_NATIVE,
        maxDisplayNative: Int = NativeListLoader.DEFAULT_DISPLAY_NATIVE,
        lifeTimeNative: Long = NativeListLoader.DEFAULT_LIFETIME_NATIVE
    ): NativeListLoader {
        return NativeListLoader(
            context = context,
            retryLoad = retryLoad,
            delayRetry = delayRetry,
            maxDisplayNative = maxDisplayNative,
            lifeTimeNative = lifeTimeNative
        )
    }

    //--- RewardedInter Ads-------------------------------------------------------------------------

    fun forceLoadRewardedInterAds(listener: AdsLoaderListener) {
        rewardedIntersAdsLoader.loadAd(listener, isForceLoad = true)
    }

    fun showInterRewardedAds(activity: Activity, listener: AdsShowerListener) {
        if (!canRequestAd || isVipUser) {
            listener.onShowAdsError()
            return
        }
        rewardedIntersAdsLoader.showAd(activity, null, object : AdsShowerListener() {
            override fun onShowAdsError() {
                Logger.w("RewardedInterAdsLoader-----onShowAdsError")
                listener.onShowAdsError()
            }

            override fun onAdDismissedFullScreenContent() {
                openAdsLoader.switchOnOff(true)
                listener.onAdDismissedFullScreenContent()
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                Logger.w("RewardedInterAdsLoader-----onAdFailedToShowFullScreenContent")
                listener.onAdFailedToShowFullScreenContent(p0)
            }

            override fun onAdShowedFullScreenContent() {
                openAdsLoader.switchOnOff(false)
                listener.onAdShowedFullScreenContent()
            }

            override fun onAdClicked() {
                listener.onAdClicked()
            }

            override fun onRewardedEarned(rewardedItem: RewardItem) {
                listener.onRewardedEarned(rewardedItem)
            }

            override fun onAdImpression() {
                listener.onAdImpression()
            }
        })
    }

    //--- Open Ads----------------------------------------------------------------------------------
    fun hasOpenAds() = openAdsLoader.isOpenAdAvailable

    fun hasSplashOpenAds() = openAdsLoader.isSplashOpenAdAvailable

    fun loadOpenAdsListener(listener: AdsLoaderListener) {
        openAdsLoader.loadOpenAdsListener = listener
    }

    fun forceLoadOpenAdsSplash(listener: AdsLoaderListener) {
        openAdsLoader.loadOpenAdsSplash(listener)
    }

    fun showOpenAdsSplash(activity: Activity, listener: AdsShowerListener, onShowSuccess: (Boolean) -> Unit) {
        openAdsLoader.showOpenAdsSplash(activity, listener, onShowSuccess)
    }

    fun setOnOffOpenAds(isOn: Boolean) {
        openAdsLoader.switchOnOff(isOn)
    }

    //----------------------------------------------------------------------------------------------

}