package com.example.newbase_2025.base.utils

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.View
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.example.newbase_2025.R
import com.google.android.material.imageview.ShapeableImageView

object BindingUtils {

    @BindingAdapter("setImageFromUrl")
    @JvmStatic
    fun setImageFromUrl(image: ShapeableImageView, url: String?) {
        if (url != null) {
            Glide.with(image.context).load(url).placeholder(R.drawable.user)
                .error(R.drawable.user).into(image)
        }
    }

    @BindingAdapter("setImageFromDrawbale")
    @JvmStatic
    fun setImageFromDrawbale(image: ShapeableImageView, url: Int?) {
        if (url != null) {
            Glide.with(image.context).load(url).placeholder(R.drawable.dummy_image)
                .error(R.drawable.user).into(image)
        }
    }


    fun styleSystemBars(activity: Activity, color: Int) {
        activity.window.navigationBarColor = color
    }

    fun statusBarStyleWhite(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            activity.window.statusBarColor = Color.TRANSPARENT
        }
    }

    fun statusBarStyleBlack(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // Ensures black text/icons
            activity.window.statusBarColor = Color.TRANSPARENT // Transparent status bar
        }
    }
}
