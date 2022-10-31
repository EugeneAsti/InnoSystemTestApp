package ru.aeyu.innosystemstestapp.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

class Entities {
    @Entity(tableName = "reports")
    data class Reports(
        @ColumnInfo(name = "id_video")      val videoId: Int,
        @ColumnInfo(name = "video_name")    val videoName: String,
        @ColumnInfo(name = "startTime")     val startTime: Date,
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        var rId: Int = 0
    )
}
