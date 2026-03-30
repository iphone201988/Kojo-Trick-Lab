package com.tech.kojo.ui.dashboard.home.progress

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.data.api.ApiHelper
import com.tech.kojo.utils.Resource
import com.tech.kojo.utils.event.SingleRequestEvent
import com.google.gson.JsonObject
import com.tech.kojo.data.room_module.DownloadVideoData
import com.tech.kojo.ui.dashboard.profile_options.download_video.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeProgressDetailsVM @Inject constructor(private val apiHelper: ApiHelper,private val repository: VideoRepository): BaseViewModel(){
    val observeCommon = SingleRequestEvent<JsonObject>()
    val observeVideo = SingleRequestEvent<List<DownloadVideoData>>()
    // get trick data by id api
    fun getTrickDataByIdApi(data: HashMap<String, Any>,url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            observeCommon.postValue(Resource.loading(null))
            runCatching {
                val response = apiHelper.apiGetWithQuery(data,url)
                if (response.isSuccessful) {
                    observeCommon.postValue(Resource.success("getTrickDataByIdApi", response.body()))
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

    fun insertVideo(video: DownloadVideoData) {
        viewModelScope.launch {
            repository.insertVideo(video)
        }
    }

    fun getAllVideos() {
        viewModelScope.launch {
            val list = repository.getAllVideos()
            observeVideo.postValue(Resource.success("getDownloadVideo", list))
        }
    }
}