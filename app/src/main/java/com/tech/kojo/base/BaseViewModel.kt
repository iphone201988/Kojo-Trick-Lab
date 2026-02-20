package com.tech.kojo.base

import android.view.View
import androidx.lifecycle.ViewModel
import com.tech.kojo.utils.event.SingleActionEvent
import okhttp3.ResponseBody
import org.json.JSONObject

open class BaseViewModel : ViewModel() {
    val onClick: SingleActionEvent<View?> = SingleActionEvent()
    val onUnAuth: SingleActionEvent<Int?> = SingleActionEvent()
    fun handleErrorResponse(errorBody: ResponseBody?, code: Int? = null): String {
        val text: String? = errorBody?.string()
        var message = ""
        if (!text.isNullOrEmpty()) {
            message = try {
                val obj = JSONObject(text)
                obj.optString("message", text)
            } catch (e: Exception) {
                text
            }
        }
        // Trigger logout if 401 OR JWT missing
        if (code == 401 || message.contains("jwt must be provided", ignoreCase = true) || message.contains(
                "Invalid algorithm",
                ignoreCase = true
            )
        ) {
            onUnAuth.postValue(401)
        }
        return message.ifEmpty { errorBody?.toString() ?: "Unknown error" }
    }
    open fun onClick(view: View?) {
        onClick.value = view
    }
}