package com.example.newbase_2025.ui.dashboard

import androidx.lifecycle.viewModelScope
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.base.utils.Resource
import com.example.newbase_2025.base.utils.event.SingleRequestEvent
import com.example.newbase_2025.data.api.ApiHelper
import com.example.newbase_2025.data.api.Constants
import com.example.newbase_2025.data.model.DummyApiResponseModel
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashBoardActivityVM @Inject constructor(
    private val apiHelper: ApiHelper,
) : BaseViewModel() {
    val observeCommon = SingleRequestEvent<JsonObject>()
    fun socialLogin(request: HashMap<String, Any>, url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                apiHelper.apiForRawBody(request, url).let {
                    if (it.isSuccessful) {
                        observeCommon.postValue(Resource.success("SOCIAL", it.body()))
                    } else
                        if (it.code() == 401)
                            observeCommon.postValue(Resource.error("Unauthorized", null))
                        else
                            observeCommon.postValue(Resource.error(handleErrorResponse(it.errorBody()), null))
                }
            } catch (e: Exception) {
                observeCommon.postValue(
                    Resource.error(
                        e.message, null
                    )
                )
            }

        }
    }
}

