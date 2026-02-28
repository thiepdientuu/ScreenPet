package com.ls.petfunny.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import com.ls.petfunny.R
import org.checkerframework.checker.units.qual.g

/**
 * Extension function cho Context để mở trang Play Store.
 * Vị trí: CommonUtils.kt hoặc UI Extensions
 */
fun Context.openPlayStore(appPackageName: String? = null) {
    // Nếu appPackageName null, mặc định lấy package name của app hiện tại
    val targetPackage = appPackageName ?: this.packageName

    // URI dùng để mở trực tiếp ứng dụng Play Store
    val marketUri = Uri.parse("market://details?id=$targetPackage")
    val marketIntent = Intent(Intent.ACTION_VIEW, marketUri).apply {
        // Thêm Flag để khi quay lại (back) sẽ về đúng app của mình, không bị kẹt ở Play Store
        addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
    }

    try {
        // Thử mở bằng ứng dụng Play Store
        startActivity(marketIntent)
    } catch (e: ActivityNotFoundException) {
        // FALLBACK: Nếu không có Play Store, mở bằng trình duyệt web
        val webUri = Uri.parse("https://play.google.com/store/apps/details?id=$targetPackage")
        try {
            startActivity(Intent(Intent.ACTION_VIEW, webUri))
        } catch (webEx: Exception) {
            // Trường hợp cực hiếm: Máy không có cả trình duyệt
            Toast.makeText(this, getString(R.string.error_unkow), Toast.LENGTH_SHORT).show()
        }
    }
}

fun Context.sendFeedbackEmail() {
    val recipient = "nguyenthingoclinh28121999@gmail.com"
    val subject = "Feedback App Pet On Screen - shimeji"

    // Tự động lấy thông tin máy để hỗ trợ debug tốt hơn
    val deviceInfo = """
        ---------------------------
        Device Infomation:
        - Model: ${Build.MODEL}
        - Android version: ${Build.VERSION.RELEASE}
        - App version: ${this.packageManager.getPackageInfo(this.packageName, 0).versionName}
        ---------------------------
        Content feedback here ...
        
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SENDTO).apply {
        // Chỉ lọc các ứng dụng mail
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, deviceInfo)
    }

    try {
        // Kiểm tra xem có app mail nào không trước khi mở
        startActivity(Intent.createChooser(intent,getString(R.string.choose_app_send)))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, getString(R.string.no_app_found), Toast.LENGTH_SHORT).show()
    }
}