package ru.aeyu.innosystemstestapp.repositories

import ru.aeyu.innosystemstestapp.model.VideoFileItem

interface SaveFileInfoRepo : BaseRepo {
    fun addFileInfo(video: VideoFileItem): Long
}