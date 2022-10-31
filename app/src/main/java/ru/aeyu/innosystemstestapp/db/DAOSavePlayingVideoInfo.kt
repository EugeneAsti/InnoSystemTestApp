package ru.aeyu.innosystemstestapp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface DAOSavePlayingVideoInfo {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertVideoFileInfo(fileInfo: Entities.Reports): Long

}