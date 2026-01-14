package com.tp.ads.banner

import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.tp.ads.base.AdsConfig
import com.tp.ads.base.BaseLoader
import com.tp.ads.utils.AppSession
import com.tp.ads.utils.EventUtils
import com.tp.ads.utils.Logger
import com.tp.ads.utils.TrackingUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BannerLoader(val context: Context, private val autoRefresh : Boolean = false, private val delayTimeRefresh : Long = 30000L) :
    BaseLoader(context) {

    private var mAdView: AdView? = null
    private var refreshJob: Job? = null
    private var adUnit : String = ""
    private var container : ViewGroup? = null
    private var showCollapsible : Boolean = false
    private var isRefreshCollapsible : Boolean = false
    private var widthBanner = 0f
    private var didRetry = false

    fun loadBannerAd(
        adUnit: String,
        adUnitHigh: String = "",
        container: ViewGroup,
        showCollapsible: Boolean,
        isRefreshCollapsible : Boolean = false,
        widthBanner : Float = 1f,
        callback: (() -> Unit)? = null,
        supportHighFloor : Boolean = false
    ) {
        try {
            this.adUnit = adUnit
            this.container = container
            this.showCollapsible = showCollapsible
            this.isRefreshCollapsible = isRefreshCollapsible
            this.widthBanner = widthBanner
            if (AppSession.isVipUser || !AppSession.canRequestAd || !AdsConfig.supportBanner) return
            mAdView = AdView(container.context)
            Logger.w(TAG,"Load banner supportHighFloor = $supportHighFloor adUnit = ${if(supportHighFloor) adUnitHigh else adUnit}")
            mAdView!!.adUnitId = if(supportHighFloor) adUnitHigh else adUnit
            mAdView!!.adListener = object : com.google.android.gms.ads.AdListener() {

                override fun onAdImpression() {
                    super.onAdImpression()
                    Logger.w(TAG,"onAdImpression callback = $callback")
                    TrackingUtils.logEvent(EventUtils.BANNER + "impression")
                    callback?.invoke()
                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    Logger.w(TAG,"onAdLoaded")
                    didRetry = false
                    TrackingUtils.logEvent(EventUtils.BANNER + "load_success")
                    container.visibility = View.VISIBLE
                    container.removeAllViews()
                    container.addView(mAdView)
                }

                override fun onAdFailedToLoad(p0: com.google.android.gms.ads.LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    if (supportHighFloor) {
                        loadBannerAd(
                            adUnit = adUnit,
                            adUnitHigh = adUnitHigh,
                            container = container,
                            showCollapsible = showCollapsible,
                            isRefreshCollapsible = isRefreshCollapsible,
                            widthBanner = widthBanner,
                            callback = callback,
                            supportHighFloor = false
                        )
                    }
                    Logger.w(TAG,"onAdFailedToLoad")
                }

            }
            mAdView!!.setAdSize(
                AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                    container.context,
                    ((Resources.getSystem().displayMetrics.widthPixels / Resources.getSystem().displayMetrics.density) * this.widthBanner).toInt()
                )
            )
            mAdView!!.setOnPaidEventListener {
                adsPreferencesHelper.saveTroasCache(
                    it.valueMicros / 1000000.0,
                    it.currencyCode
                )
            }
            mAdView!!.loadAd(
                buildAdRequestBanner(
                    isCollapsibleBanner = showCollapsible,
                    isBanner = true,
                    isInter = false,
                    isRefreshCollapsible = this.isRefreshCollapsible
                )
            )
            // Schedule the next refresh if autoRefresh is enabled
            if (autoRefresh) {
                refreshJob?.cancel() // Cancel the previous job if it's still running
                refreshJob = CoroutineScope(Dispatchers.IO).launch {
                    while (true) {
                        delay(delayTimeRefresh)
                        Logger.w(TAG,"Refreshing banner ad")
                        launch(Dispatchers.Main) {
                            loadBannerAd(
                                adUnit = adUnit,
                                adUnitHigh = adUnitHigh,
                                container = container,
                                showCollapsible = showCollapsible,
                                isRefreshCollapsible = isRefreshCollapsible,
                                widthBanner = widthBanner,
                                callback = callback,
                                supportHighFloor = false
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Logger.e(TAG,e)
        }
    }

    override fun reLoadAd() {
        super.reLoadAd()

    }

    override fun release() {
        super.release()
        mAdView?.destroy()
        refreshJob?.cancel() // Cancel the refresh job when the banner is released
    }

    companion object{
        const val TAG = "BannerLoader"
    }

}