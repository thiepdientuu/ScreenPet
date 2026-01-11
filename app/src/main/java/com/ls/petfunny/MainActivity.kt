package com.ls.petfunny

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.ls.petfunny.ui.ShimejiService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val btnStart = findViewById<Button>(R.id.btnStartInfo)
        val btnStop = findViewById<Button>(R.id.btnStopInfo)

        btnStart.setOnClickListener {
            if (checkOverlayPermission()) {
                startShimejiService()
            } else {
                requestOverlayPermission()
            }
        }

        btnStop.setOnClickListener {
            stopShimejiService()
        }
    }

    // Kiểm tra quyền vẽ đè
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
