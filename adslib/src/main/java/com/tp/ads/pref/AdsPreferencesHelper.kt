package com.tp.ads.pref

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.tp.ads.utils.Logger

class AdsPreferencesHelper constructor(var context: Context) : Preferences(context) {

    //tRoast
    var troasCache by floatPref("TroasCache", 0f)

    fun saveTroasCache(currentImpressionRevenue: Double, currency: String?, ) {
        //Use App Local storage to store cache of tROAS
        val previousTroasCache = troasCache
        val currentTroasCache = (previousTroasCache + currentImpressionRevenue).toFloat()
        Logger.w(
            TAG_NAME,
            ">>>>>>>> previousTroasCache: $previousTroasCache currentTroasCache:$currentTroasCache"
        )

        troasCache = if (currentTroasCache >= 0.01) {
            recordAdRevenueEvent(currentTroasCache, currency)
            0f
        } else {
            currentTroasCache
        }
    }

    private fun recordAdRevenueEvent(tROASCache: Float, currency: String?) {
        val bundle = Bundle()
        // (Required) tROAS event must include Double Value
        bundle.putDouble(FirebaseAnalytics.Param.VALUE, tROASCache.toDouble())
        // Put in the correct currency
        bundle.putString(FirebaseAnalytics.Param.CURRENCY, currency)
        FirebaseAnalytics.getInstance(context).logEvent(DAILY_ADS_REVENUE_EVENT, bundle)
    }


    companion object {
        private const val TAG_NAME = "AppPreferencesHelper"

        // tROAS event
        const val DAILY_ADS_REVENUE_EVENT = "daily_ads_revenue"
    }
}