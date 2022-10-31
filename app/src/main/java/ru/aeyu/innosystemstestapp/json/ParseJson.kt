package ru.aeyu.innosystemstestapp.json

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.aeyu.innosystemstestapp.model.VideoFileItem
import ru.aeyu.innosystemstestapp.model.VideoFiles

class ParseJson {

    fun fetchDataFromJson(listJsonData: String): VideoFiles {
        val gson = Gson()
        //val playListType = object : TypeToken<List<VideoFileItem>>() {}.type
        return gson.fromJson(listJsonData, VideoFiles::class.java)
    }
}