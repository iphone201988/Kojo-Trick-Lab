package com.tech.kojo.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.PopupWindow
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
import com.tech.kojo.data.model.HomeTrickVault
import com.tech.kojo.data.model.HomeType
import com.tech.kojo.data.model.NotificationData
import com.tech.kojo.data.model.PastSessionData
import com.tech.kojo.data.model.UserIdProfile
import com.tech.kojo.databinding.ItemLayoutInnerNotificationBinding
import com.tech.kojo.databinding.RvMyTrickInnerItemBinding
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.collections.map

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
                .placeholder(R.drawable.progress_animation_small).error(R.drawable.holder_dummy)
                .into(image)
        } else {
            image.setImageResource(R.drawable.holder_dummy)
        }
    }


    @BindingAdapter("setImageFromUserData")
    @JvmStatic
    fun setImageFromUserData(image: ShapeableImageView, model: UserIdProfile?) {
        if (model != null) {
            val finalUrl = when {
                model?.profilePicture.isNullOrBlank() -> null
                model.profilePicture.startsWith("http", ignoreCase = true) -> model.profilePicture
                else -> Constants.BASE_URL_IMAGE + model?.profilePicture
            }

            if (finalUrl != null) {
                Glide.with(image.context).load(finalUrl)
                    .placeholder(R.drawable.progress_animation_small).error(R.drawable.holder_dummy)
                    .into(image)
            } else {
                image.setImageResource(R.drawable.holder_dummy)
            }
        } else {
            image.setImageResource(R.drawable.holder_dummy)
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
                .error(R.drawable.holder_dummy).into(image)
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
            val cleanUrl = (Constants.BASE_URL_IMAGE + url)
                .replace(" ", "")
            Glide.with(image.context).asBitmap().load(cleanUrl)
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


    fun hasCameraPermission(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", ".jpg", storageDir
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

    @BindingAdapter("hideIfCompleted")
    @JvmStatic
    fun hideIfCompleted(view: ConstraintLayout, status: String?) {
        view.visibility = if (status == "completed") {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    @BindingAdapter("showPersonalBestValue")
    @JvmStatic
    fun showPersonalBestValue(view: AppCompatTextView, value: Int?) {
        if (value!=null){
            view.text=value.toString()
        }
        else{
            view.text ="0"
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

    @BindingAdapter("titleCaseFormattedWithSpace")
    @JvmStatic
    fun titleCaseFormattedWithSpace(textView: AppCompatTextView, text: String?) {

        if (text.isNullOrBlank()) {
            textView.text = "-"
            return
        }

        val formatted = text
            .replace("_", " ")
            .replace(Regex("([a-z])([A-Z])"), "$1 $2")
            .lowercase()
            .split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }

        textView.text = formatted
    }

    @BindingAdapter("setTextCapitalizedPersonalBest")
    @JvmStatic
    fun setTextCapitalizedPersonalBest(view: AppCompatTextView, text: String?) {

        if (text.isNullOrBlank()) {
            view.text = "-"
            return
        }

        // Add space before uppercase letters (camelCase → words)
        val formatted = text
            .replace(Regex("([a-z])([A-Z])"), "$1 $2")
            .replaceFirstChar { it.uppercase() }

        // Add colon at end
        view.text = "$formatted :"
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

    @BindingAdapter("setNotificationIcon")
    @JvmStatic
    fun setNotificationIcon(imageView: AppCompatImageView, count: Int?) {
        if ((count ?: 0) > 0) {
            imageView.setImageResource(R.drawable.notification)
        } else {
            imageView.setImageResource(R.drawable.ic_no_notifications)
        }
    }


    fun showDropdownModel(
        anchor: View, items: List<HomeTrickVault>, onItemSelected: (HomeTrickVault) -> Unit
    ) {
        val context = anchor.context
        val inflater = LayoutInflater.from(context)

        val popupView = inflater.inflate(R.layout.popup_menu_view, null)
        val listView = popupView.findViewById<ListView>(R.id.listView)

        val popupWindow = PopupWindow(
            popupView,
            context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._250sdp),
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        val adapter =
            ArrayAdapter(context, android.R.layout.simple_list_item_1, items.map { it.name?.toTitleCase() })
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            onItemSelected(items[position])
            popupWindow.dismiss()
        }

        popupWindow.elevation = 12f
        popupWindow.isOutsideTouchable = true
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        popupWindow.showAsDropDown(anchor, 0, 0, Gravity.END)
    }

    fun String.toTitleCase(): String {
        return lowercase().split(" ")
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
    }


    /** Compress and Rotate Image **/
    fun compressImage(imageUri: Uri, context: Context): Uri? {
        return try {
            val bitmap = decodeSampledBitmapFromUri(
                imageUri, context
            ) // Decode with proper sampling
            val rotatedBitmap =
                bitmap?.let { rotateImageIfRequired(context, it, imageUri) } // Fix orientation

            val outputStream = ByteArrayOutputStream()
            rotatedBitmap?.compress(
                Bitmap.CompressFormat.JPEG, 50, outputStream
            ) // Compress to 50% quality
            val byteArray = outputStream.toByteArray()
            val file = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            file.writeBytes(byteArray)

            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    // Decode Bitmap
    private fun decodeSampledBitmapFromUri(
        imageUri: Uri,
        context: Context,
    ): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(imageUri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
            options.inSampleSize = calculateInSampleSize(options)
            options.inJustDecodeBounds = false
            context.contentResolver.openInputStream(imageUri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Calculate its sample size
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
    ): Int {

        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > 800 || width > 800) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= 800 && halfWidth / inSampleSize >= 800) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    /** Fix Image Orientation: Rotate Landscape to Portrait Only **/
    private fun rotateImageIfRequired(context: Context, bitmap: Bitmap, imageUri: Uri): Bitmap {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val exif = ExifInterface(inputStream!!)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
            )
            inputStream.close()

            val rotationAngle = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }

            if (rotationAngle != 0) {
                val matrix = Matrix()
                matrix.postRotate(rotationAngle.toFloat())
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else {
                bitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            bitmap
        }
    }


    @BindingAdapter("setStatusText")
    @JvmStatic
    fun setStatusText(textView: AppCompatTextView, type: String?) {
        if (type != null) {
            when (type) {
                "attempted" -> {
                    textView.text= "In Progress"
                    textView.setTextColor(ContextCompat.getColor(textView.context, R.color.colorPrimary))
                }

                "completed" -> {
                   textView.text ="Completed"
                    textView.setTextColor(ContextCompat.getColor(textView.context, R.color.green_color))
                }

                "pending" -> {
                    textView.text="Pending"
                    textView.setTextColor(ContextCompat.getColor(textView.context, R.color.blue))
                }
            }
        }
    }

    @BindingAdapter("setStatusIcon")
    @JvmStatic
    fun setStatusIcon(imageView: AppCompatImageView, type: String?) {
        if (type != null) {
            when (type) {
                "attempted" -> {
                    imageView.setImageResource(R.drawable.progress_icon)
                }

                "completed" -> {
                    imageView.setImageResource(R.drawable.completed_icon)
                }

                "pending" -> {
                    imageView.setImageResource(R.drawable.hugeicons_play)
                }
            }
        }
    }

    @BindingAdapter("setBackgroundTint")
    @JvmStatic
    fun setBackgroundTint(imageView: LinearLayout, type: String?) {
        if (type != null) {
            when (type) {
                "attempted" -> {
                    imageView.backgroundTintList = ContextCompat.getColorStateList(imageView.context, R.color.colorPrimary)
                }

                "completed" -> {
                    imageView.backgroundTintList = ContextCompat.getColorStateList(imageView.context, R.color.green_color)
                }

                "pending" -> {
                    imageView.backgroundTintList = ContextCompat.getColorStateList(imageView.context, R.color.blue)
                }
            }
        }
    }

    @BindingAdapter("setSpannableUnderlineBlue")
    @JvmStatic
    fun setSpannableUnderlineBlue(view: AppCompatTextView, text: String?) {
        if (text.isNullOrEmpty()) {
            view.text = "-"
            return
        }

        val spannable = SpannableString(text)

        // Underline
        spannable.setSpan(
            UnderlineSpan(),
            0,
            text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Blue color (URL style)
        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(view.context, android.R.color.holo_blue_dark)),
            0,
            text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        view.text = spannable
    }

}
