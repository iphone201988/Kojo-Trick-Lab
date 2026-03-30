package com.tech.kojo.ui.video

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.data.api.ApiHelper
import com.tech.kojo.utils.Resource
import com.tech.kojo.utils.event.SingleRequestEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoFragmentVM @Inject constructor(val apiHelper: ApiHelper): BaseViewModel(){
    val observeCommon = SingleRequestEvent<JsonObject>()
    // update video view status
    // post Like api
    fun updateVideoViewStatus(url: String, request:HashMap<String, Any>) {
        viewModelScope.launch(Dispatchers.IO) {
//            observeCommon.postValue(Resource.loading(null))
            runCatching {
                val response = apiHelper.apiPostForRawBody(url,request)
                if (response.isSuccessful) {
                    observeCommon.postValue(Resource.success("updateVideoViewStatus", response.body()))
                } else {
                    val errorMsg = handleErrorResponse(response.errorBody(), response.code())
                    observeCommon.postValue(Resource.error(errorMsg, null))
                }
            }.onFailure { e ->
                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
                observeCommon.postValue(Resource.error("${e.message}", null))
            }
        }
    }
}