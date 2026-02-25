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
import com.tp.ads.base.AdManager
import com.tp.ads.base.AdsShowerListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var adManager: AdManager

    private val viewModel: MainViewModel by viewModels()

    lateinit var binding : ActivityMainBinding


    private val shimejiAdapter by lazy {
        ShimejiAdapter { shimejiGif ->
            viewModel.downloadShimejiV2(shimejiGif)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adManager.loadInterAds()
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)
        setupViewPager()
        setupBottomNavigation()
        setOnClickListener()
        setUpListPet()
        setUpObserver()
        viewModel.loadPack()
    }

    private fun setupViewPager() {
        // Khởi tạo Adapter - Luôn truyền 'this' (FragmentActivity)
        val adapter = ViewPagerAdapter(this)
        binding.viewPager.adapter = adapter
        binding.viewPager.isUserInputEnabled = false

        // Tối ưu: Giữ sẵn 1 page bên cạnh để scroll mượt hơn
        binding.viewPager.offscreenPageLimit = 2

        // Lắng nghe sự kiện vuốt để cập nhật BottomNav tương ứng
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

            }
        })
    }

    private fun setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> binding.viewPager.currentItem = 0
                R.id.nav_pet -> binding.viewPager.currentItem = 1
                R.id.nav_setting -> binding.viewPager.currentItem = 2
            }
            true
        }
    }

    private fun setOnClickListener() {
//        binding.btnStartInfo.setOnClickListener {
//            if (checkOverlayPermission()) {
//                startShimejiService()
//            } else {
//                requestOverlayPermission()
//            }
//        }
//
//        binding.btnStopInfo.setOnClickListener {
//            stopShimejiService()
//        }
    }

    private fun setUpObserver() {

    }

    private fun setUpListPet() {
//        binding.rvPet.apply {
//            // Hiển thị 4 cột như yêu cầu của bạn
//            layoutManager = GridLayoutManager(this@MainActivity, 4)
//            adapter = shimejiAdapter
//            setHasFixedSize(true)
//        }
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

    fun gotoPetStore(){
        binding.bottomNav.selectedItemId = R.id.nav_pet
    }

    // Yêu cầu quyền vẽ đè
    fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermissionLauncher.launch(intent)
        }
    }

    // Lắng nghe kết quả sau khi người dùng cấp quyền
    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (checkOverlayPermission()) {
            startShimejiService()
            adManager.showInterAds(this, object  : AdsShowerListener() {
                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    startShimejiService()
                }

                override fun onShowAdsError() {
                    super.onShowAdsError()
                    startShimejiService()
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    super.onAdFailedToShowFullScreenContent(p0)
                    startShimejiService()
                }
            })
        } else {
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
}
