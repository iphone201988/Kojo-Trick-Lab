package com.tech.kojo.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.material.imageview.ShapeableImageView
import com.google.gson.Gson
import com.tech.kojo.BR
import com.tech.kojo.R
import com.tech.kojo.base.SimpleRecyclerViewAdapter
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.GetComboData
import com.tech.kojo.data.model.HomeType
import com.tech.kojo.data.model.NotificationData
import com.tech.kojo.data.model.PastSessionData
import com.tech.kojo.databinding.ItemLayoutInnerNotificationBinding
import com.tech.kojo.databinding.RvMyTrickInnerItemBinding
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object BindingUtils {

    @BindingAdapter("setImageFromUrl")
    @JvmStatic
    fun setImageFromUrl(image: ShapeableImageView, url: String?) {
        if (url != null) {
            Glide.with(image.context).load(Constants.BASE_URL_IMAGE + url)
                .placeholder(R.drawable.progress_animation_small).error(R.drawable.holder_dummy)
                .into(image)
        } else {
            image.setImageResource(R.drawable.holder_dummy)
        }
    }

    @BindingAdapter("setImageFromUrlHttp")
    @JvmStatic
    fun setImageFromUrlHttp(image: ShapeableImageView, url: String?) {

        val finalUrl = when {
            url.isNullOrBlank() -> null
            url.startsWith("http", ignoreCase = true) -> url
            else -> Constants.BASE_URL_IMAGE + url
        }

        if (finalUrl != null) {
            Glide.with(image.context).load(finalUrl)
                .placeholder(R.drawable.progress_animation_small).error(R.drawable.blank_pofile)
                .into(image)
        } else {
            image.setImageResource(R.drawable.blank_pofile)
        }
    }


    @BindingAdapter("setBgSkin")
    @JvmStatic
    fun setBgSkin(image: AppCompatImageView, url: String?) {
        if (url != null) {
            when (url) {
                "Skin 1" -> image.setImageResource(R.drawable.skin_first)
                "Skin 2" -> image.setImageResource(R.drawable.skin_second)
                "Skin 3" -> image.setImageResource(R.drawable.skin_third)
                "Skin 4" -> image.setImageResource(R.drawable.skin_four)
                "Skin 5" -> image.setImageResource(R.drawable.skin_five)
                "Skin 6" -> image.setImageResource(R.drawable.skin_six)
            }
        } else {
            image.setImageResource(R.drawable.skin_first)
        }
    }


    @BindingAdapter("setUrlPost")
    @JvmStatic
    fun setUrlPost(image: ShapeableImageView, url: String?) {
        if (url != null) {
            Glide.with(image.context).load(Constants.BASE_URL_IMAGE + url)
                .placeholder(R.drawable.progress_animation_small)
                .error(R.drawable.progress_animation_small).into(image)
        } else {
            image.setImageResource(R.drawable.holder_dummy)
        }
    }


    @BindingAdapter(value = ["setUrlPost2", "titleView"], requireAll = false)
    @JvmStatic
    fun setUrlPost2(
        image: ShapeableImageView, url: String?, titleView: AppCompatTextView?
    ) {

        // Hide title initially (while loading)
        titleView?.visibility = View.GONE

        if (url.isNullOrEmpty()) {
            image.setImageResource(R.drawable.holder_dummy)
            return
        }

        Glide.with(image.context).load(Constants.BASE_URL_IMAGE + url)
            .placeholder(R.drawable.progress_animation_small)
            .error(R.drawable.progress_animation_small)
            .listener(object : RequestListener<Drawable> {

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    titleView?.visibility = View.VISIBLE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {

                    // Show title when image loaded
                    titleView?.visibility = View.VISIBLE

                    return false
                }
            }).into(image)
    }


    @BindingAdapter("videoThumbNail")
    @JvmStatic
    fun videoThumbNail(image: ShapeableImageView, url: String?) {
        if (url != null) {
            Glide.with(image.context).asBitmap().load(Constants.BASE_URL_IMAGE + url)
                .apply(RequestOptions().frame(1_000_000)).into(image)
        } else {

        }
    }

    /**
     * navigate with slide animation
     */
    fun navigateWithSlide(navController: NavController, directions: NavDirections) {
        val navOptions = NavOptions.Builder().setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left).setPopEnterAnim(R.anim.slide_in_left)
            .setPopExitAnim(R.anim.slide_out_right).build()

        navController.navigate(directions, navOptions)
    }


    @BindingAdapter("setImageFromDrawbale")
    @JvmStatic
    fun setImageFromDrawbale(image: ShapeableImageView, url: Int?) {
        if (url != null) {
            Glide.with(image.context).load(url).placeholder(R.drawable.progress_animation_small)
                .error(R.drawable.progress_animation_small).into(image)
        } else {
            image.setImageResource(R.drawable.holder_dummy)
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
    fun innerMyTrickAdapter(view: RecyclerView, tasks: List<HomeType>?) {
        val taskAdapter = SimpleRecyclerViewAdapter<HomeType, RvMyTrickInnerItemBinding>(
            R.layout.rv_my_trick_inner_item, BR.bean
        ) { v, m, pos ->
            when (v.id) {

            }
        }
        view.adapter = taskAdapter
        taskAdapter.list = tasks

    }


    @BindingAdapter("visibleIfCompleted")
    @JvmStatic
    fun visibleIfCompleted(view: ConstraintLayout, status: String?) {
        view.visibility = if (status == "completed") {
            View.VISIBLE
        } else {
            View.GONE
        }
    }


    @BindingAdapter("changeImageCheck")
    @JvmStatic
    fun changeImageCheck(view: AppCompatImageView, status: String?) {
        if (status.equals("completed")) {
            view.setImageResource(R.drawable.bg_complete)
        } else {
            view.setImageResource(R.drawable.unselected_category_checkbox)
        }
    }


    @BindingAdapter("visibleIfProgress")
    @JvmStatic
    fun visibleIfProgress(view: ConstraintLayout, status: String?) {
        view.visibility = if (status == "in-progress") {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    @BindingAdapter("visibleIfAttempted")
    @JvmStatic
    fun visibleIfAttempted(view: ConstraintLayout, status: String?) {
        view.visibility = if (status == "attempted") {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    @BindingAdapter("visibleIfNotStarted")
    @JvmStatic
    fun visibleIfNotStarted(view: ConstraintLayout, status: String?) {
        view.visibility = if (status == "not-started") {
            View.VISIBLE
        } else {
            View.GONE
        }
    }


    @BindingAdapter("setImageType")
    @JvmStatic
    fun setImageType(image: AppCompatImageView, type: String?) {
        if (type != null) {
            when (type) {
                "attempted" -> {
                    image.setImageResource(R.drawable.bg_progress)
                }

                "completed" -> {
                    image.setImageResource(R.drawable.bg_complete)
                }

                "pending" -> {
                    image.setImageResource(R.drawable.bg_play)
                }
            }
        }
    }


    @BindingAdapter("setLayoutBg")
    @JvmStatic
    fun setLayoutBg(image: ConstraintLayout, type: String?) {
        if (type != null) {
            when (type) {
                "red" -> {
                    image.setBackgroundResource(R.drawable.red_card_bg)
                }

                "green" -> {
                    image.setBackgroundResource(R.drawable.green_card_bg)
                }

                "orange" -> {
                    image.setBackgroundResource(R.drawable.orange_card_bg)
                }

                "blue" -> {
                    image.setBackgroundResource(R.drawable.purple_card_bg)
                }
            }
        }
    }


    @BindingAdapter("childNotificationAdapter")
    @JvmStatic
    fun childNotificationAdapter(view: RecyclerView, notification: List<NotificationData>?) {

        // Create and set a LayoutManager for the inner RecyclerView
        val layoutManager = LinearLayoutManager(view.context)
        view.layoutManager = layoutManager
        view.context
        val notificationAdapter =
            SimpleRecyclerViewAdapter<NotificationData, ItemLayoutInnerNotificationBinding>(
                R.layout.item_layout_inner_notification, BR.bean
            ) { v, m, pos ->
                when (v.id) {

                }
            }
        view.adapter = notificationAdapter
        notificationAdapter.list = notification
        notificationAdapter.notifyDataSetChanged()

    }

    inline fun <reified T> parseJson(json: String): T? {
        return try {
            val gson = Gson()
            gson.fromJson(json, T::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("gfdgdgdgfd", "parseJson: $e")
            null
        }
    }


    fun parseMonthColorJson(jsonString: String, month: Int, year: Int): HashMap<String, String> {
        val result = HashMap<String, String>()

        val jsonObject = JSONObject(jsonString)

        // extract "data" object
        val dataObject = jsonObject.optJSONObject("data") ?: return result

        val keys = dataObject.keys()

        while (keys.hasNext()) {
            keys.next()
            // example: "2025-11-22"

//            val parts = dateKey.split("-")
//            if (parts.size == 3) {
//                val y = parts[0].toIntOrNull()
//                val m = parts[1].toIntOrNull()
//
//                // match passed month & year
//                if (y == year && m == month) {
//
//                    // get value: ["blue"]
//                    val colorArray = dataObject.optJSONArray(dateKey)
//                    val color = colorArray?.optString(0).orEmpty()
//
//                    result[dateKey] = color
//                }
            // }
        }

        return result
    }


    @BindingAdapter("setTimeAgo")
    @JvmStatic
    fun setTimeAgo(textView: AppCompatTextView, time: String?) {
        if (!time.isNullOrEmpty()) {
            textView.text = getTimeAgo(time)
        }
    }

    @BindingAdapter("setDateFilter", "allItems", "itemPosition")
    @JvmStatic
    fun setDateFilter(
        textView: AppCompatTextView,
        bean: PastSessionData?,
        allItems: List<PastSessionData>?,
        position: Int
    ) {
        if (bean == null || allItems.isNullOrEmpty()) {
            textView.visibility = View.GONE
            return
        }

        val currentLabel = getDateLabel(bean.date)

        if (position == 0) {
            textView.text = currentLabel
            textView.visibility = View.VISIBLE
            return
        }

        val prevItem = allItems[position - 1]
        val prevDateLabel = getDateLabel(prevItem.date)

        if (currentLabel == prevDateLabel) {
            textView.visibility = View.GONE
        } else {
            textView.visibility = View.VISIBLE
            textView.text = currentLabel
        }
    }


    fun getTimeAgo(dateString: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")

            val past = sdf.parse(dateString)?.time ?: return ""
            val now = System.currentTimeMillis()

            var diff = (now - past)

            if (diff < 0) diff = 0   // handle future dates safely

            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24

            return when {
                seconds < 60 -> "Just now"

                minutes < 60 -> "${minutes} minute${plural(minutes)} ago"

                hours < 24 -> "${hours} hour${plural(hours)} ago"

                days < 7 -> "${days} day${plural(days)} ago"

                days < 30 -> {
                    val weeks = days / 7
                    "${weeks} week${plural(weeks)} ago"
                }

                days < 365 -> {
                    val months = days / 30
                    "${months} month${plural(months)} ago"
                }

                else -> {
                    val years = days / 365
                    "${years} year${plural(years)} ago"
                }
            }

        } catch (e: Exception) {
            ""
        }
    }

    private fun plural(value: Long): String = if (value > 1) "s" else ""


    @RequiresApi(Build.VERSION_CODES.O)
    fun getFormattedToday(): String {
        val today = LocalDate.now()

        val formatter = DateTimeFormatter.ofPattern("dd, MMM yyyy")
        return today.format(formatter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatDate(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("dd, MMM yyyy")
        return date.format(formatter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatDateForApi(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return date.format(formatter)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun formatDate(dateString: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val outputFormatter = DateTimeFormatter.ofPattern("dd, MMM yyyy")

        val date = LocalDate.parse(dateString, inputFormatter)
        return date.format(outputFormatter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun convertToApiFormat(dateString: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("dd, MMM yyyy")
        val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val date = LocalDate.parse(dateString, inputFormatter)
        return date.format(outputFormatter)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentDate(): String {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @BindingAdapter("setTimePast")
    @JvmStatic
    fun setTimePast(textView: AppCompatTextView, time: String?) {
        if (!time.isNullOrEmpty()) {
            var data = convertIsoToReadable(time)
            textView.text = data
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun convertIsoToReadable(dateStr: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val outputFormatter = DateTimeFormatter.ofPattern("dd, MMM yyyy")

        val date = LocalDate.parse(dateStr, inputFormatter)
        return date.format(outputFormatter)
    }

    fun getDateLabel(utcDate: String?): String {
        val date = parseUtcToLocalDate(utcDate) ?: return ""

        val calendar = Calendar.getInstance()
        calendar.time = date

        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

        return when {
            isSameDay(calendar, today) -> "Today"
            isSameDay(calendar, yesterday) -> "Yesterday"
            else -> {
                val sdf = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
                sdf.format(date)
            }
        }
    }

    private fun parseUtcToLocalDate(utcDate: String?): Date? {
        if (utcDate.isNullOrEmpty()) return null
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdf.parse(utcDate)

            // Convert to local timezone
            date
        } catch (e: Exception) {
            null
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(
            Calendar.DAY_OF_YEAR
        )
    }

    @BindingAdapter("setBackgroundBg")
    @JvmStatic
    fun setBackgroundBg(image: AppCompatImageView, type: String?) {
        if (type != null) {
            when (type) {
                "red" -> {
                    image.setBackgroundColor(
                        ContextCompat.getColor(
                            image.context, R.color.red_color
                        )
                    )
                }

                "green" -> {
                    image.setBackgroundColor(
                        ContextCompat.getColor(
                            image.context, R.color.green_color
                        )
                    )
                }

                "orange" -> {
                    image.setBackgroundColor(ContextCompat.getColor(image.context, R.color.orange))
                }

                "blue" -> {
                    image.setBackgroundColor(ContextCompat.getColor(image.context, R.color.purple))
                }
            }
        }
    }

    @BindingAdapter("setTextCapitalized")
    @JvmStatic
    fun setTextCapitalized(view: AppCompatTextView, text: String?) {
        text?.let {
            view.text = it.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase() else char.toString()
            }
        }
    }

    @BindingAdapter("formatDateForCombo")
    @JvmStatic
    fun formatDateForCombo(textView: AppCompatTextView, isoDate: String?) {

        if (isoDate.isNullOrEmpty()) {
            textView.text = ""
            return
        }

        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")

            val date = inputFormat.parse(isoDate)

            if (date != null) {
                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                textView.text = "Last Updated: ${outputFormat.format(date)}"
            } else {
                textView.text = ""
            }

        } catch (e: Exception) {
            textView.text = ""
        }
    }

    @BindingAdapter("setExpandableText")
    @JvmStatic
    fun setExpandableText(
        descText: AppCompatTextView, item: GetComboData
    ) {
        val showMoreText =
            (descText.parent as View).findViewById<AppCompatTextView>(R.id.tvShowMore)

        if (item.isExpanded) {
            // Expanded
            descText.maxLines = Int.MAX_VALUE
            descText.ellipsize = null
            showMoreText.text = "Show Less"
            showMoreText.visibility = View.VISIBLE
        } else {
            // Collapsed (measure safely)
            descText.maxLines = Int.MAX_VALUE
            descText.ellipsize = null

            descText.post {
                val lineCount = descText.layout?.lineCount ?: 0

                if (lineCount > 2) {
                    descText.maxLines = 2
                    descText.ellipsize = TextUtils.TruncateAt.END
                    showMoreText.visibility = View.VISIBLE
                    showMoreText.text = "Show More"
                } else {
                    showMoreText.visibility = View.GONE
                }
            }
        }
    }


}
