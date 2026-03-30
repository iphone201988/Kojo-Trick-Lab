package com.tech.kojo.ui.dashboard.profile_options.download_video

import androidx.lifecycle.viewModelScope
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.data.room_module.DownloadVideoData
import com.tech.kojo.utils.Resource
import com.tech.kojo.utils.event.SingleRequestEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadVideoVM @Inject constructor(private val repository: VideoRepository): BaseViewModel() {
    val observeVideo = SingleRequestEvent<List<DownloadVideoData>>()

    fun getAllVideos() {
        viewModelScope.launch {
            val list = repository.getAllVideos()
            observeVideo.postValue(Resource.success("getDownloadVideo", list))
        }
    }

    fun deleteVideo(id: String?) {
        viewModelScope.launch {
            repository.deleteVideoById(id)
        }
    }
}