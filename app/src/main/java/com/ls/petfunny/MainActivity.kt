package com.ls.petfunny

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.ads.AdError
import com.ls.petfunny.databinding.ActivityMainBinding
import com.ls.petfunny.ui.ShimejiService
import com.tp.ads.base.AdManager
import com.tp.ads.base.AdsShowerListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var adManager: AdManager

    lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adManager.loadInterAds()
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)

        binding.btnStartInfo.setOnClickListener {
            if (checkOverlayPermission()) {
                startShimejiService()
            } else {
                requestOverlayPermission()
            }
        }

        binding.btnStopInfo.setOnClickListener {
            stopShimejiService()
        }
    }


    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    // Yêu cầu quyền vẽ đè
    private fun requestOverlayPermission() {
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

    private fun startShimejiService() {
        val intent = Intent(this, ShimejiService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopShimejiService() {
        val intent = Intent(this, ShimejiService::class.java)
        stopService(intent)
    }
}
