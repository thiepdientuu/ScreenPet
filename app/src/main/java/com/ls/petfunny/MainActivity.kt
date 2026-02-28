package com.ls.petfunny

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.ads.AdError
import com.ls.petfunny.databinding.ActivityMainBinding
import com.ls.petfunny.ui.ShimejiService
import com.ls.petfunny.ui.adapter.ShimejiAdapter
import com.ls.petfunny.ui.adapter.ViewPagerAdapter
import com.ls.petfunny.utils.AllEvents
import com.ls.petfunny.utils.TrackingHelper
import com.tp.ads.base.AdManager
import com.tp.ads.base.AdsLoaderListener
import com.tp.ads.base.AdsShowerListener
import com.tp.ads.utils.AdCommonUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var adManager: AdManager

    private val viewModel: MainViewModel by viewModels()

    lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)
        adManager.setActivity(this)
        TrackingHelper.logEvent(AllEvents.VIEW_MAIN)
        loadBannerHome()
        setupViewPager()
        setupBottomNavigation()
        setOnClickListener()
        setUpObserver()
        adManager.forceLoadInterAds(object : AdsLoaderListener() {})
        adManager.loadCacheOpenAds()
    }

    private fun loadBannerHome() {
        com.ls.petfunny.ui.ads.AdManager.loadBannerAd(
            adUnitHigh = AdCommonUtils.BANNER_HOME_HIGH_KEY,
            adUnitNormal = AdCommonUtils.BANNER_HOME_KEY,
            container = binding.layoutAds,
            showCollapsible = showCollapsibleBannerHome
        )
    }

    private fun setupViewPager() {
        // Khởi tạo Adapter - Luôn truyền 'this' (FragmentActivity)
        val adapter = ViewPagerAdapter(this)
        binding.viewPager.adapter = adapter
        binding.viewPager.isUserInputEnabled = false

        // Tối ưu: Giữ sẵn 1 page bên cạnh để scroll mượt hơn
        binding.viewPager.offscreenPageLimit = 2
    }

    private fun setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadNativeHome()
                    binding.viewPager.setCurrentItem(0,false)
                }
                R.id.nav_pet -> {
                    adManager.showInterAds(this,object : AdsShowerListener() {
                        override fun onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent()
                            binding.viewPager.setCurrentItem(1,false)
                        }

                        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                            super.onAdFailedToShowFullScreenContent(p0)
                            binding.viewPager.setCurrentItem(1,false)
                        }

                        override fun onShowAdsError() {
                            super.onShowAdsError()
                            binding.viewPager.setCurrentItem(1,false)
                        }
                    })
                }
                R.id.nav_setting -> binding.viewPager.setCurrentItem(2,false)
            }
            true
        }
    }

    private fun setOnClickListener() {
    }

    private fun setUpObserver() {

    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    fun startShimeijService(){
        if (checkOverlayPermission()) {
            startShimejiService()
        } else {
            requestOverlayPermission()
        }
    }

    fun gotoHome(){
        binding.bottomNav.selectedItemId = R.id.nav_home
    }

    fun loadNativeHome(){
        adManager.loadNativeHomeHigh(
            adsUnitHigh = AdCommonUtils.NATIVE_HOME_HIGH_KEY,
            adUnitNormal = AdCommonUtils.NATIVE_HOME_NORMAL_KEY
        )
    }

    // Yêu cầu quyền vẽ đè
    fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        overlayPermissionLauncher.launch(intent)
    }

    // Lắng nghe kết quả sau khi người dùng cấp quyền
    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (checkOverlayPermission()) {
            TrackingHelper.logEvent(AllEvents.PERMISSION + "accept")
            startShimejiService()
        } else {
            TrackingHelper.logEvent(AllEvents.PERMISSION + "deny")
            Toast.makeText(this, getString(R.string.need_permision), Toast.LENGTH_SHORT).show()
        }
    }

   fun startShimejiService() {
        val intent = Intent(this, ShimejiService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    fun stopShimejiService() {
        val intent = Intent(this, ShimejiService::class.java)
        stopService(intent)
    }

    companion object{
        var showCollapsibleBannerHome = true
    }
}
