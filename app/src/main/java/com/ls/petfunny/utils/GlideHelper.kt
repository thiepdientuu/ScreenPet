package com.ls.petfunny.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.FutureTarget
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
import com.bumptech.glide.request.transition.Transition
import com.ls.petfunny.MainApp
import com.ls.petfunny.R
import java.io.File

object GlideHelper {

    // Load ảnh full kích thước với placeholder và error fallback
    fun loadImageFull(
        context: Context,
        url: String,
        imageView: ImageView,
        placeholderResId: Int = android.R.color.darker_gray,
        errorResId: Int = android.R.drawable.ic_dialog_alert
    ) {
        Glide.with(context)
            .load(url)
            .placeholder(placeholderResId)
            .error(errorResId)
            .into(imageView)
    }

    // Load ảnh thumbnail (ví dụ 10% kích thước gốc) – dùng cho preview
    fun loadImageThumb(
        context: Context,
        url: String,
        imageView: ImageView,
        thumbnailScale: Float = 0.1f,
        cornerRadius: Int = 16
    ) {
        val radiusInPx = cornerRadius * context.resources.displayMetrics.density

        Glide.with(context)
            .load(url)
            .thumbnail(thumbnailScale)
            .transform(CenterCrop(), RoundedCorners(radiusInPx.toInt()))
            .into(imageView)
    }

    // Load ảnh với centerCrop (phổ biến trong Grid/List)
    fun loadImageCenterCrop(
        context: Context,
        url: String,
        imageView: ImageView,
        placeholderResId: Int = android.R.color.darker_gray
    ) {
        Glide.with(context)
            .load(url)
            .centerCrop()
            .placeholder(placeholderResId)
            .into(imageView)
    }

    // Load ảnh với fitCenter (giữ nguyên tỉ lệ, vừa khung)
    fun loadImageFitCenter(
        context: Context,
        url: String,
        imageView: ImageView,
        placeholderResId: Int = android.R.color.darker_gray
    ) {
        Glide.with(context)
            .load(url)
            .fitCenter()
            .placeholder(placeholderResId)
            .into(imageView)
    }

    // Load ảnh từ resource nội bộ
    fun loadFromResource(
        context: Context,
        resId: Int,
        imageView: ImageView
    ) {
        Glide.with(context)
            .load(resId)
            .into(imageView)
    }

    // Load ảnh với rounded corners
    fun loadImageWithRoundedCorners(
        context: Context,
        url: String,
        imageView: ImageView,
        cornerRadius: Int = 16 // in dp
    ) {
        val radiusInPx = cornerRadius * context.resources.displayMetrics.density

        Glide.with(context)
            .load(url)
            .transform(CenterCrop(), RoundedCorners(radiusInPx.toInt()))
            .into(imageView)
    }

    fun ImageView.loadUrl(url: String,view : View? = null,cornerRadius: Int = 0) {
        val radiusInPx = cornerRadius * context.resources.displayMetrics.density
        Glide.with(view ?: this)
            .load(url)
            .centerCrop()
            .override(AppConfig.widthScreen / 8, AppConfig.widthScreen / 8)
            .placeholder(R.drawable.ic_launcher_foreground)
            .transform(CenterCrop(), RoundedCorners(radiusInPx.toInt()))
            .dontAnimate()
            .into(this)
    }

    fun download(url: String, options: RequestOptions? = null): FutureTarget<File> {
        var requestOptions = options ?: RequestOptions()
            .override(SIZE_ORIGINAL)
        requestOptions = requestOptions.priority(Priority.HIGH)
        return Glide.with(MainApp.instances)
            .downloadOnly()
            .load(url)
            .apply(requestOptions)
            .submit()
    }

    fun clear(view: ImageView) {
        try {
            Glide.with(view).clear(view)
            view.setImageDrawable(null)
        } catch (e: IllegalArgumentException) {
        }
    }


    val TRANSITION = Transition { current: Drawable?, adapter: Transition.ViewAdapter ->
        if (adapter.view is ImageView) {
            val image = adapter.view as ImageView
            if (image.drawable == null) {
                image.alpha = 0f
                image.animate().alpha(1f)
            }
        }
        false
    }

}