package com.tech.kojo.data.room_module

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface RoomDataBaseQueryPage {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: DownloadVideoData)

    @Query("SELECT * FROM VideoDownload")
    suspend fun getAllVideos(): List<DownloadVideoData>

    @Query("DELETE FROM VideoDownload WHERE id = :id")
    suspend fun deleteById(id: String?)
}