package com.example.newbase_2025.ui.dashboard.home.final_progress

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.data.api.ApiHelper
import com.example.newbase_2025.utils.Resource
import com.example.newbase_2025.utils.event.SingleRequestEvent
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FinalProgressFragmentVM @Inject constructor(private val apiHelper: ApiHelper) :
    BaseViewModel() {
    val observeCommon = SingleRequestEvent<JsonObject>()

    // user progress api
    fun userProgressApi(url: String,request:HashMap<String, Any>) {
        viewModelScope.launch(Dispatchers.IO) {
            observeCommon.postValue(Resource.loading(null))
            runCatching {
                val response = apiHelper.apiPostForRawBody(url,request)
                if (response.isSuccessful) {
                    observeCommon.postValue(Resource.success("userProgressApi", response.body()))
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