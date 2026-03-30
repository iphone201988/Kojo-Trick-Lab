package com.tech.kojo.ui.dashboard.profile_options.download_video

import com.tech.kojo.data.room_module.DownloadVideoData
import com.tech.kojo.data.room_module.RoomDataBaseQueryPage
import javax.inject.Inject

class VideoRepository @Inject constructor(
    private val dao: RoomDataBaseQueryPage
) {

    suspend fun insertVideo(video: DownloadVideoData) {
        dao.insertVideo(video)
    }

    suspend fun getAllVideos(): List<DownloadVideoData> {
        return dao.getAllVideos()
    }

    suspend fun deleteVideoById(id: String?) {
        dao.deleteById(id)
    }

}