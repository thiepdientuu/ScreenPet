package com.tp.ads.natives

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoController
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.tp.ads.R
import com.tp.ads.base.AdsConfig
import com.tp.ads.base.AdsLoaderListener
import com.tp.ads.base.BaseLoader
import com.tp.ads.utils.AppSession
import com.tp.ads.utils.EventUtils
import com.tp.ads.utils.Logger
import com.tp.ads.utils.TrackingUtils

class NativeListLoader(
    val context: Context,
    val retryLoad: Int = DEFAULT_RETRY_LOAD_NATIVE,
    val delayRetry: Long = DEFAULT_DELAY_RETRY_NATIVE,
    private val maxDisplayNative: Int = DEFAULT_DISPLAY_NATIVE,
    private val lifeTimeNative: Long = DEFAULT_LIFETIME_NATIVE
) :
    BaseLoader(context) {

    private var adsModel: AdsModel? = null
    private var retryCount = 0
    private var isDoingLoadNativeList = false
    private var adUnit: String = ""
    var oldTimeLoadNative = System.currentTimeMillis()
    var newTimeLoadNative = System.currentTimeMillis()
    private var orderLoadNative = 0

    companion object {
        const val DEFAULT_RETRY_LOAD_NATIVE = 2
        const val DEFAULT_DELAY_RETRY_NATIVE = 5000L
        const val DEFAULT_DISPLAY_NATIVE = 4
        const val DEFAULT_LIFETIME_NATIVE = 4000L
        const val TAG = "NativeListLoader"
        var nativeAdCache : NativeAd ?= null
    }

    var didUsingCache = false

    fun loadNativeListAds(
        adUnit: String,
        listener: AdsLoaderListener,
        fromRetry: Boolean = false,
    ) {
        if (AppSession.isVipUser || !AppSession.canRequestAd || !AdsConfig.supportNative) return
        if (adsModel != null && !fromRetry) {
            if (adsModel!!.countDisplay < maxDisplayNative) {
                Logger.w(
                    TAG,
                    "==== return loadNativeListAds numberDisplay: ${adsModel!!.countDisplay} checkTimeLoadNative: ${adsModel!!.checkTimeLoadNative()} "
                )
                return
            }
        }
        if (isDoingLoadNativeList) return
        isDoingLoadNativeList = true
        if (nativeAdCache != null && !didUsingCache) {
            didUsingCache = true
            isDoingLoadNativeList = false
            adsModel = AdsModel(nativeAdCache!!, 0, System.currentTimeMillis(), lifeTimeNative)
            listener.onAdLoaded()
            return
        }
        this.adUnit = adUnit
        Logger.w(TAG, "==== Start load native list ads")
        val videoOptions = VideoOptions.Builder().setStartMuted(true).build()
        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions).build()
        val adLoader = AdLoader.Builder(context, adUnit)
            .forNativeAd { nativeAd ->
                Logger.w(TAG, "load native list successfully:" + nativeAd.headline.toString())
                isDoingLoadNativeList = false
                nativeAd.setOnPaidEventListener {
                    adsPreferencesHelper.saveTroasCache(
                        it.valueMicros / 1000000.0,
                        it.currencyCode
                    )
                }
                adsModel = AdsModel(nativeAd, 0, System.currentTimeMillis(), lifeTimeNative)
                listener.onAdLoaded()
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    if (retryCount < retryLoad) {
                        retryCount++
                        Handler(Looper.getMainLooper()).postDelayed({
                            isDoingLoadNativeList = false
                            loadNativeListAds(
                                adUnit,
                                listener,
                                fromRetry = true
                            ) // retry loading the ad
                            Logger.w(TAG, "retry load native list ads")
                        }, delayRetry)

                    } else {
                        listener.onLoadAdsError()
                        isDoingLoadNativeList = false
                        retryCount = 0
                        Logger.w(TAG, " load native list ads fail")
                        TrackingUtils.logEvent(EventUtils.NATIVE + "load_fail")
                    }
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                    Logger.w(TAG, "NativeFlow ---> native list onAdImpression")

                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    TrackingUtils.logEvent(EventUtils.NATIVE + "load_success")
                    retryCount = 0
                }
            })
            .withNativeAdOptions(adOptions)
            .build()

        // Load the ad
        adLoader.loadAd(buildAdRequest())
        orderLoadNative++
        oldTimeLoadNative = newTimeLoadNative
        newTimeLoadNative = System.currentTimeMillis()
    }

    fun showNativeList(container: ViewGroup) {
        Logger.w(TAG, "showNativeList ")
        if (adsModel != null) {
            container.visibility = View.VISIBLE
            val adView = LayoutInflater.from(container.context).inflate(
                R.layout.native_list, null
            ) as NativeAdView
            populateNativeAdView(adsModel!!.ads, adView)
            container.removeAllViews()
            container.addView(adView)
            adsModel?.updateDisplay()
        } else {
            container.visibility = View.GONE
        }
    }

    private fun populateNativeAdView(
        nativeAd: NativeAd?,
        adView: NativeAdView
    ) {
        val mediaView: MediaView? = adView.findViewById(R.id.adMedia)
        adView.mediaView = mediaView
        if (nativeAd == null) {
            return
        }
        // Set other ad assets.
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.adBody)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        (adView.headlineView as? TextView?)?.text = nativeAd.headline
        (adView.bodyView as? TextView)?.text = nativeAd.body
        if (nativeAd.callToAction == null) {
            adView.callToActionView?.visibility = View.GONE
        } else {
            adView.callToActionView?.visibility = View.VISIBLE
            (adView.callToActionView as? TextView?)?.text = nativeAd.callToAction
        }
        if (nativeAd.icon == null) {
            adView.iconView?.visibility = View.GONE
        } else {
            (adView.iconView as? ImageView?)?.setImageDrawable(nativeAd.icon!!.drawable)
            adView.iconView?.visibility = View.VISIBLE
        }
        if (nativeAd.starRating == null) {
            adView.starRatingView?.visibility = View.GONE
        } else {
            (adView.starRatingView as? RatingBar?)?.rating = nativeAd.starRating!!.toFloat()
            adView.starRatingView?.visibility = View.VISIBLE
        }
        if (nativeAd.advertiser == null) {
            adView.advertiserView?.visibility = View.GONE
        } else {
            (adView.advertiserView as? TextView?)?.text = nativeAd.advertiser
            adView.advertiserView?.visibility = View.VISIBLE
        }
        adView.setNativeAd(nativeAd)

        val vc = nativeAd.mediaContent?.videoController

        if (vc?.hasVideoContent() == true) {
            mediaView?.minimumHeight = 120
            vc.videoLifecycleCallbacks = object : VideoController.VideoLifecycleCallbacks() {
            }
        } else {
            mediaView?.minimumHeight = 100
        }
        mediaView?.visibility = View.VISIBLE
    }

    override fun release() {
        super.release()
        adsModel?.ads?.destroy()
    }
}

data class AdsModel(
    var ads: NativeAd,
    var countDisplay: Int = 0,
    var timeLoadNative: Long = 0L,
    var lifeTimeNative: Long = NativeListLoader.DEFAULT_LIFETIME_NATIVE
) {
    fun updateDisplay() {
        countDisplay++
        Logger.w(NativeListLoader.TAG, "---> Count display native: $countDisplay")
    }

    fun checkTimeLoadNative(): Boolean {
        return System.currentTimeMillis() - timeLoadNative > lifeTimeNative
    }
}
