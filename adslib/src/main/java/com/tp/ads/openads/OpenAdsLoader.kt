package com.tp.ads.openads

import android.app.Activity
import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.appopen.AppOpenAd
import com.tp.ads.base.AdsConfig
import com.tp.ads.base.AdsLoaderListener
import com.tp.ads.base.AdsShowerListener
import com.tp.ads.base.BaseLoader
import com.tp.ads.utils.AppSession
import com.tp.ads.utils.EventUtils
import com.tp.ads.utils.Logger
import com.tp.ads.utils.TrackingUtils
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenAdsLoader
@Inject constructor(
    val context: Context,
) : BaseLoader(context), LifecycleEventObserver {

    private var adsOpenId: String = ""
    private var adsOpenHighId: String = ""
    private var adsOpenSplashId: String = ""
    private var _adsOpenApp: AppOpenAd? = null
    private var _adsOpenAppSplash: AppOpenAd? = null

    private var activityWeakRef: WeakReference<Activity>? = null
    private var isReadyShowAd = true
    private var lastAdFetchTime = 0L

    private var isLoadingOpenAds = false
    private var retryLoadAds = true
    var openShowListener = MutableLiveData<Boolean>()

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    val isOpenAdAvailable: Boolean
        get() = _adsOpenApp != null && (System.currentTimeMillis() - lastAdFetchTime <= MAX_AD_EXPIRY_DURATION)

    val isSplashOpenAdAvailable: Boolean
        get() = _adsOpenAppSplash != null && (System.currentTimeMillis() - lastAdFetchTime <= MAX_AD_EXPIRY_DURATION)

    var loadOpenAdsListener = AdsLoaderListener()

    fun setAdsOpenId(adsOpenId: String, adsOpenSplashId: String,adOpenHigh : String) {
        this.adsOpenId = adsOpenId
        this.adsOpenSplashId = adsOpenSplashId
        this.adsOpenHighId = adOpenHigh
    }

    fun setActivity(activity: Activity) {
        activityWeakRef = WeakReference(activity)
    }

    fun switchOnOff(turnOn: Boolean) {
        isReadyShowAd = turnOn
    }

    override fun buildAdRequest(
        isCollapsibleBanner: Boolean, isBanner: Boolean, isInter: Boolean
    ): AdManagerAdRequest {
        return super.buildAdRequest(isCollapsibleBanner, isBanner, isInter)
    }

    override fun loadAd(listener: AdsLoaderListener, isForceLoad: Boolean) {
        Logger.w(TAG, "OpenAdLoader-----> handle load OpenAds ")
        isLoadingOpenAds = true
        AppOpenAd.load(
            context,
            adsOpenId,
            buildAdRequest(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Logger.w(TAG,"OpenAdLoader-----> openAds load success")
                    lastAdFetchTime = System.currentTimeMillis()
                    _adsOpenApp = ad
                    retryLoadAds = true

                    listener.onLoadFinish()
                    listener.onAdLoaded()
                    loadOpenAdsListener.onAdLoaded()
                    loadOpenAdsListener.onLoadFinish()
                    isLoadingOpenAds = false
                    TrackingUtils.logEvent(EventUtils.OPEN_AD + "load_success")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Logger.w(TAG,"OpenAdLoader----->openAds load fail :${loadAdError.message}")
                    listener.onLoadFinish()
                    listener.onLoadAdsError()
                    loadOpenAdsListener.onLoadAdsError()
                    loadOpenAdsListener.onLoadFinish()
                    isLoadingOpenAds = false
                    TrackingUtils.logEvent(EventUtils.OPEN_AD + "load_fail")
                }
            })
    }

    fun loadAdHigh(listener: AdsLoaderListener) {
        if (!AppSession.canRequestAd || AppSession.isVipUser
            || isOpenAdAvailable || isLoadingOpenAds || !AdsConfig.supportOpenAd
        ) {
            Logger.w("AdManager", "OpenAdLoader----->Return openAds HIGH load")
            listener.onLoadFinish()
            return
        }

        Logger.w(TAG, "OpenAdLoader-----> handle load OpenAds HIGH ")
        isLoadingOpenAds = true
        AppOpenAd.load(
            context,
            adsOpenHighId,
            buildAdRequest(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Logger.w(TAG,"OpenAdLoader-----> openAds HIGH load success")
                    lastAdFetchTime = System.currentTimeMillis()
                    _adsOpenApp = ad
                    retryLoadAds = true

                    listener.onLoadFinish()
                    listener.onAdLoaded()
                    loadOpenAdsListener.onAdLoaded()
                    loadOpenAdsListener.onLoadFinish()
                    isLoadingOpenAds = false
                    TrackingUtils.logEvent(EventUtils.OPEN_AD + "load_success")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Logger.w(TAG,"OpenAdLoader----->openAds HIGH load fail :${loadAdError.message}")
                    loadAd(object  : AdsLoaderListener(){})
                    listener.onLoadFinish()
                    listener.onLoadAdsError()
                    loadOpenAdsListener.onLoadAdsError()
                    loadOpenAdsListener.onLoadFinish()
                    isLoadingOpenAds = false
                    TrackingUtils.logEvent(EventUtils.OPEN_AD + "load_fail")
                }
            })
    }

    override fun showAd(
        activity: Activity, rwdTime: Int?, listener:
        AdsShowerListener
    ) {
        if (!AppSession.canRequestAd || AppSession.isVipUser || !AdsConfig.supportOpenAd) {
            listener.onAdDismissedFullScreenContent()
            return
        }
        if (!isReadyShowAd) {
            listener.onAdDismissedFullScreenContent()
            Logger.w(TAG, "----- switch OnOff OpenAds is false")
            return
        }
        if (!isOpenAdAvailable) {
            listener.onAdDismissedFullScreenContent()
            loadAdHigh(object : AdsLoaderListener() {

            })
            return
        }

        _adsOpenApp?.show(activity)
        _adsOpenApp?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Logger.w(TAG, "-----onAdDismissedFullScreenContent")
                _adsOpenApp = null
                openShowListener.postValue(false)
                listener.onAdDismissedFullScreenContent()
                loadAdHigh(object : AdsLoaderListener() {

                })
            }

            override fun onAdClicked() {
                listener.onAdClicked()
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                Logger.w(TAG, "-----onAdFailedToShowFullScreenContent :${p0.message}")
                listener.onAdFailedToShowFullScreenContent(p0)
                loadAdHigh(object : AdsLoaderListener() {

                })
            }

            override fun onAdImpression() {
                listener.onAdImpression()
            }

            override fun onAdShowedFullScreenContent() {
                Logger.w(TAG, "-----onAdShowedFullScreenContent")
                openShowListener.postValue(true)
                listener.onAdShowedFullScreenContent()
            }
        }
        _adsOpenApp!!.onPaidEventListener = OnPaidEventListener { adValue: AdValue ->
            adsPreferencesHelper.saveTroasCache(
                adValue.valueMicros / 1000000.0,
                adValue.currencyCode
            )
        }
    }

    override fun handleAutoLoadAds(count: Int, isDelay: Boolean) {

    }

    override fun release() {
        _adsOpenApp = null
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_START -> {
                activityWeakRef?.let {
                    it.get()?.let { activity ->
                        Logger.w("$TAG ----> Show open ads background")
                        showAd(activity, null, object : AdsShowerListener() {})
                    }
                }
            }

            Lifecycle.Event.ON_RESUME -> {
//                loadAd(object : AdsLoaderListener() {})
            }

            else -> Unit
        }
    }

    //----- Logic Open Ads Splash ------------------------------------------------------------------

    fun loadOpenAdsSplash(listener: AdsLoaderListener) {
        if (!AppSession.canRequestAd || AppSession.isVipUser) {
            Logger.w("AdManager", "-----openAds not load")
            listener.onLoadFinish()
            return
        }

        Logger.w(TAG, "-----handle load OpenAds Splash")
        AppOpenAd.load(
            context,
            adsOpenSplashId,
            buildAdRequest(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Logger.w("AdManager", "$TAG -----> openAds load success")
                    _adsOpenAppSplash = ad


                    listener.onLoadFinish()
                    listener.onAdLoaded()
                    loadOpenAdsListener.onLoadFinish()
                    loadOpenAdsListener.onAdLoaded()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Logger.w("AdManager", "$TAG -----> openAds load fail :${loadAdError.message}")


                    listener.onLoadFinish()
                    listener.onLoadAdsError()
                    loadOpenAdsListener.onLoadAdsError()
                    loadOpenAdsListener.onLoadFinish()
                }
            })
    }

    fun showOpenAdsSplash(
        activity: Activity, listener: AdsShowerListener, onShowSuccess: (Boolean) -> Unit
    ) {
        if (!AppSession.canRequestAd || AppSession.isVipUser || _adsOpenAppSplash == null) {
            listener.onAdDismissedFullScreenContent()
            onShowSuccess.invoke(false)
            return
        }
        if (!isReadyShowAd) {
            listener.onAdDismissedFullScreenContent()
            onShowSuccess.invoke(false)
            Logger.w(TAG, "----- switch OnOff OpenAds is false")
            return
        }

        _adsOpenAppSplash?.show(activity)
        _adsOpenAppSplash?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Logger.w(TAG, "-----onAdDismissedFullScreenContent")
                _adsOpenAppSplash = null
                listener.onAdDismissedFullScreenContent()
                onShowSuccess.invoke(true)
            }

            override fun onAdClicked() {
                listener.onAdClicked()
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                Logger.w(TAG, "-----onAdFailedToShowFullScreenContent :${p0.message}")
                listener.onAdFailedToShowFullScreenContent(p0)
                onShowSuccess.invoke(false)
            }

            override fun onAdImpression() {
                Logger.w(TAG, "-----onAdShowedFullScreenContent11123")
                listener.onAdImpression()
            }

            override fun onAdShowedFullScreenContent() {
                Logger.w(TAG, "-----onAdShowedFullScreenContent13123454")
                listener.onAdShowedFullScreenContent()
            }
        }
        _adsOpenAppSplash?.onPaidEventListener = OnPaidEventListener { adValue: AdValue ->
            adsPreferencesHelper.saveTroasCache(
                adValue.valueMicros / 1000000.0,
                adValue.currencyCode
            )
        }
    }

    companion object {
        const val TAG = "OpenAdsLoader"
        const val MAX_AD_EXPIRY_DURATION = 3600000 * 4.toLong()
    }

}