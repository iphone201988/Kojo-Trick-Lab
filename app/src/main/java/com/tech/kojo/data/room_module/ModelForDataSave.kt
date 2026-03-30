package com.tech.kojo.data.room_module

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "VideoDownload")
data class DownloadVideoData(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    val _id: String?,
    val createdAt: String?,
    val thumbnailUrl: String?,
    val title: String?,
    val videoDownload: Boolean,
    val localPath: String? = null,
)