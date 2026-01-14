package com.tp.ads.natives

import android.content.Context
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
import com.tp.ads.base.BaseLoader
import com.tp.ads.utils.AppSession
import com.tp.ads.utils.EventUtils
import com.tp.ads.utils.Logger
import com.tp.ads.utils.TrackingUtils

class NativeLoader(val context: Context,) :
    BaseLoader(context) {

    private var nativeAd: NativeAd? = null
    private var adUnit: String = ""
    private var typeNativeAds: TypeNativeAds = TypeNativeAds.NATIVE_NO_MEDIA
    private var container: ViewGroup? = null
    private var isShowedNative = false

    fun loadNativeAds(
        adUnit: String,
        typeNativeAds: TypeNativeAds,
        container: ViewGroup,
        listener: NativeAdsLoaderListener,
        nativeAdView: NativeAdView ?= null
    ) {
        if (AppSession.isVipUser || !AppSession.canRequestAd || !AdsConfig.supportNative) return
        Logger.w(TAG,"====> Start loadNativeAds")
        this.adUnit = adUnit
        this.typeNativeAds = typeNativeAds
        this.container = container
        val videoOptions = VideoOptions.Builder().setStartMuted(true).build()
        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions).build()
        val adLoader = AdLoader.Builder(context, adUnit)
            .forNativeAd { ad ->
                nativeAd = ad
                container.visibility = View.VISIBLE
                val adView = nativeAdView ?: LayoutInflater.from(container.context).inflate(
                    when (typeNativeAds) {
                        TypeNativeAds.NATIVE_NO_MEDIA -> R.layout.native_no_media
                        TypeNativeAds.NATIVE_FULL -> R.layout.native_full
                        TypeNativeAds.NATIVE_CUSTOM -> R.layout.native_full
                    }, null
                ) as NativeAdView
                populateNative(ad, adView)
                container.removeAllViews()
                container.addView(adView)
                listener.onAdLoaded()
            }
            .withAdListener(object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    Logger.w(TAG,"====> onAdLoaded")
                    TrackingUtils.logEvent(EventUtils.NATIVE + "load_success")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    listener.onLoadAdsError()
                    Logger.w(TAG,"====> onAdFailedToLoad")
                    TrackingUtils.logEvent(EventUtils.NATIVE + "load_fail")
                }

                override fun onAdClicked() {
                    listener.onAdClicked()
                }

                override fun onAdOpened() {
                    listener.onAdOpened()
                }

                override fun onAdClosed() {
                    listener.onAdClosed()
                }

                override fun onAdImpression() {
                    listener.onAdImpression()
                    Logger.w(TAG,"====> onAdImpression")
                    isShowedNative = true
                }
            })
            .withNativeAdOptions(adOptions)
            .build()

        // Load the ad
        adLoader.loadAd(buildAdRequest())
    }

    private fun populateNative(
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
        adView.bodyView = adView.findViewById(R.id.ad_des)
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
    }

    override fun release() {
        super.release()
        nativeAd?.destroy()
    }

    companion object{
        const val TAG = "NativeLoader"
    }

}

enum class TypeNativeAds {
    NATIVE_NO_MEDIA,
    NATIVE_FULL,
    NATIVE_CUSTOM
}

interface NativeAdsLoaderListener {
    fun onAdLoaded()
    fun onAdClicked()
    fun onAdOpened()
    fun onAdClosed()
    fun onAdImpression()
    fun onLoadAdsError()
    fun onPaidEvent()
}