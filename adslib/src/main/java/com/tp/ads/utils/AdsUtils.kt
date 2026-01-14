package com.tp.ads.utils

import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.google.android.gms.ads.VideoController
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.tp.ads.R

object AdsUtils {
     fun populateNative(
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
}