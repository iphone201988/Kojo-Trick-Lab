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
        if (code != null && code == 401) {
            onUnAuth.postValue(code)
        }
        if (!text.isNullOrEmpty()) {
            return try {
                val obj = JSONObject(text)
                obj.getString("message")
            } catch (e: Exception) {
                return text
            }
        }
        return errorBody.toString()
    }

    open fun onClick(view: View?) {
        onClick.value = view
    }
}