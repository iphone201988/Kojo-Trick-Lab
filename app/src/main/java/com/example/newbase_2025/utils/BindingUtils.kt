package com.example.newbase_2025.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.BindingAdapter
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newbase_2025.BR
import com.example.newbase_2025.R
import com.example.newbase_2025.base.SimpleRecyclerViewAdapter
import com.example.newbase_2025.data.model.SubTitle
import com.example.newbase_2025.databinding.RvMyTrickInnerItemBinding
import com.google.android.material.imageview.ShapeableImageView
import kotlin.reflect.typeOf

object BindingUtils {

    @BindingAdapter("setImageFromUrl")
    @JvmStatic
    fun setImageFromUrl(image: ShapeableImageView, url: String?) {
        if (url != null) {
            Glide.with(image.context).load(url).placeholder(R.drawable.user)
                .error(R.drawable.user).into(image)
        }
    }

    /**
     * navigate with slide animation
     */
    fun navigateWithSlide(navController: NavController, directions: NavDirections) {
        val navOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left)
            .setPopEnterAnim(R.anim.slide_in_left)
            .setPopExitAnim(R.anim.slide_out_right)
            .build()

        navController.navigate(directions, navOptions)
    }


    @BindingAdapter("setImageFromDrawbale")
    @JvmStatic
    fun setImageFromDrawbale(image: ShapeableImageView, url: Int?) {
        if (url != null) {
            Glide.with(image.context).load(url).placeholder(R.drawable.dummy_image)
                .error(R.drawable.user).into(image)
        }
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

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.CAMERA
        )
    } else {
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )
    }


    fun hasPermissions(context: Context?, permissions: Array<String>?): Boolean {
        if (context != null && permissions != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        context, permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }




    @BindingAdapter("innerMyTrickAdapter")
    @JvmStatic
    fun innerMyTrickAdapter(view: RecyclerView, tasks: List<SubTitle>?) {
        val taskAdapter = SimpleRecyclerViewAdapter<SubTitle, RvMyTrickInnerItemBinding>(
            R.layout.rv_my_trick_inner_item, BR.bean
        ) { v, m, pos ->
            when (v.id) {

            }
        }
        view.adapter = taskAdapter
        taskAdapter.list = tasks

    }

    @BindingAdapter("setImageType")
    @JvmStatic
    fun setImageType(image: AppCompatImageView, type: Int?) {
        if (type != null) {
           when(type){
               1->{
                   image.setImageResource(R.drawable.bg_progress)
               }
               2->{
                   image.setImageResource(R.drawable.bg_complete)
               }
               3->{
                   image.setImageResource(R.drawable.bg_play)
               }
           }
        }
    }

}
