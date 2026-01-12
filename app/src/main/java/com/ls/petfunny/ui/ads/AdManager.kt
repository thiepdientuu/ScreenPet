package com.ls.petfunny.ui.ads

import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import java.util.UUID

object AdManager {

    private fun buildAdRequestBanner(isCollapsibleBanner: Boolean = false,): AdManagerAdRequest {
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

    fun loadBannerAd(
        adUnitHigh: String,
        adUnitNormal: String,
        container: ViewGroup,
        showCollapsible: Boolean
    ) {
        val mAdView = AdView(container.context)
        val keyLoad = adUnitHigh.ifEmpty { adUnitNormal }
        mAdView.adUnitId = keyLoad
        mAdView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                container.visibility = View.VISIBLE
                container.removeAllViews()
                container.addView(mAdView)
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                if (adUnitHigh.isNotEmpty()) {
                    loadBannerAd(
                        adUnitHigh = "",
                        adUnitNormal = adUnitNormal,
                        container = container,
                        showCollapsible = showCollapsible
                    )
                }
            }
        }
        mAdView.setAdSize(
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                container.context,
                ((Resources.getSystem().displayMetrics.widthPixels / Resources.getSystem().displayMetrics.density)).toInt()
            )
        )
        mAdView.loadAd(
            buildAdRequestBanner(
                isCollapsibleBanner = showCollapsible
            )
        )
    }
}