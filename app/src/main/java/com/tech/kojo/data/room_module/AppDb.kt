package com.tech.kojo.data.room_module

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


@Database(entities = [DownloadVideoData::class], version = 2)
abstract class AppDb : RoomDatabase() {
    abstract fun connectAbstractClassConnection(): RoomDataBaseQueryPage
}
