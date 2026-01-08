package com.tech.kojo.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.view.View
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object AppUtils {

    fun textToRequestBody(text: String?): RequestBody {
        return text!!.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    fun Uri?.getFilePath(context: Context): String {
        return this?.let { uri -> RealPath.getRealPath(context, uri) ?: "" } ?: ""
    }

    @Throws(IOException::class)
    fun createImageFile1(context: Context): File? {
        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())
        val imageFileName = "IMG_" + timeStamp + "_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".png",  /* suffix */
            storageDir /* directory */
        )
        return image
    }

    fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", ".jpg", storageDir
        )
    }






    @Throws(IOException::class)
    fun createImageFile2(context: Context): File? {
        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())
        val imageFileName = "IMG_" + timeStamp + "_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".png",  /* suffix */
            storageDir /* directory */
        )
        return image
    }
    fun preventMultipleClick(view: View) {
        view.isEnabled = false
        view.postDelayed({
            try {
                view.isEnabled = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, 1000)
    }
}