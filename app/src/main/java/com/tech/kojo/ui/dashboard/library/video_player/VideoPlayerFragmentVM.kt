package com.tech.kojo.ui.dashboard.library.video_player

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
class VideoPlayerFragmentVM @Inject constructor(private val apiHelper: ApiHelper,private val repository: VideoRepository) :
    BaseViewModel() {
    val observeCommon = SingleRequestEvent<JsonObject>()

    val observeVideo = SingleRequestEvent<List<DownloadVideoData>>()

    // get Video By Id api
    fun getVideoById(url: String, request: HashMap<String, Any>) {
        viewModelScope.launch(Dispatchers.IO) {
            observeCommon.postValue(Resource.loading(null))
            runCatching {
                val response = apiHelper.apiGetWithQuery(request, url)
                if (response.isSuccessful) {
                    observeCommon.postValue(Resource.success("getVideoById", response.body()))
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

    // get user comment api
    fun getUserComment(url: String, request: HashMap<String, Any>) {
        viewModelScope.launch(Dispatchers.IO) {
            observeCommon.postValue(Resource.loading(null))
            runCatching {
                val response = apiHelper.apiGetWithQuery(request, url)
                if (response.isSuccessful) {
                    observeCommon.postValue(Resource.success("getUserComment", response.body()))
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

    // get user related api
    fun getUserRelated(url: String, request: HashMap<String, Any>) {
        viewModelScope.launch(Dispatchers.IO) {
            observeCommon.postValue(Resource.loading(null))
            runCatching {
                val response = apiHelper.apiGetWithQuery(request, url)
                if (response.isSuccessful) {
                    observeCommon.postValue(Resource.success("getUserRelated", response.body()))
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

    // post comment api
    fun postComments(url: String, request: HashMap<String, Any>) {
        viewModelScope.launch(Dispatchers.IO) {
            observeCommon.postValue(Resource.loading(null))
            runCatching {
                val response = apiHelper.apiPostForRawBody(url,request)
                if (response.isSuccessful) {
                    observeCommon.postValue(Resource.success("postComments", response.body()))
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


    // delete video comment api
    fun deletePostVideoComments(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            observeCommon.postValue(Resource.loading(null))
            runCatching {
                val response = apiHelper.apiPostForToken(url)
                if (response.isSuccessful) {
                    observeCommon.postValue(Resource.success("deletePostVideoComments", response.body()))
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