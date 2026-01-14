package com.tp.ads.base

import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.rewarded.RewardItem

open class AdsShowerListener {
    open fun onShowAdsError(){}
    open fun onAdDismissedFullScreenContent(){}
    open fun onAdClicked(){}
    open fun onAdFailedToShowFullScreenContent(p0: AdError){}
    open fun onAdImpression(){}
    open fun onAdShowedFullScreenContent(){}
    open fun onRewardedEarned(rewardedItem: RewardItem){}
}