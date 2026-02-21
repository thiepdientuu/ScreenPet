package com.ls.petfunny

import android.R.attr.visibility
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.ls.petfunny.databinding.ActivityMainBinding
import com.ls.petfunny.ui.ShimejiService
import com.ls.petfunny.ui.adapter.ShimejiAdapter
import com.ls.petfunny.utils.AppLogger
import com.tp.ads.base.AdManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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
        setOnClickListener()
        setUpListPet()
        setUpObserver()
        viewModel.loadPack()
    }

    private fun setOnClickListener() {
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

    private fun setUpObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 1. Lắng nghe trạng thái Loading
                launch {
                    viewModel.isLoading.collect { isLoading ->
                        if (isLoading) {
                            // Hiện Loading (Có thể dùng ProgressBar hoặc Dialog tùy UI của bạn)
                            binding.loadingView.visibility = View.VISIBLE
                        } else {
                            // Ẩn Loading
                            binding.loadingView.visibility = View.GONE
                        }
                    }
                }

                // 2. Lắng nghe sự kiện Toast (Chỉ phát 1 lần)
                launch {
                    viewModel.toastEvent.collect { message ->
                        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        lifecycleScope.launch {
            // Chỉ thu thập dữ liệu khi Activity ở trạng thái STARTED hoặc RESUMED
            // Tự động dừng khi Activity vào Background (STOPPED)
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.topPackCharacters.collect { characters ->
                    if (characters.isNotEmpty()) {
                        shimejiAdapter.submitList(characters)
                    } else {
                        // Xử lý khi danh sách trống nếu cần
                        AppLogger.d("Danh sách nhân vật trống")
                    }
                }
            }
        }

        // Tư duy Senior: Thu thập Flow một cách an toàn
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mascotUiState.collect { list ->
                    AppLogger.d("HIHI ---> Flow nhận dữ liệu mới: ${list.size} items")
                }
            }
        }
    }

    private fun setUpListPet() {
        binding.rvPet.apply {
            // Hiển thị 4 cột như yêu cầu của bạn
            layoutManager = GridLayoutManager(this@MainActivity, 4)
            adapter = shimejiAdapter
            setHasFixedSize(true)
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
            startShimejiService()
//            adManager.showInterAds(this, object  : AdsShowerListener() {
//                override fun onAdDismissedFullScreenContent() {
//                    super.onAdDismissedFullScreenContent()
//                    startShimejiService()
//                }
//
//                override fun onShowAdsError() {
//                    super.onShowAdsError()
//                    startShimejiService()
//                }
//
//                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
//                    super.onAdFailedToShowFullScreenContent(p0)
//                    startShimejiService()
//                }
//            })
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
