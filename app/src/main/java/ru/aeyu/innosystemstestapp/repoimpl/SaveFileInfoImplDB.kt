package ru.aeyu.innosystemstestapp.repoimpl

import android.content.Context
import ru.aeyu.innosystemstestapp.db.DAOSavePlayingVideoInfo
import ru.aeyu.innosystemstestapp.db.Entities
import ru.aeyu.innosystemstestapp.model.VideoFileItem
import ru.aeyu.innosystemstestapp.repositories.SaveFileInfoRepo
import java.util.*

class SaveFileInfoImplDB(override val context: Context,
                         private val dao: DAOSavePlayingVideoInfo
    ) : SaveFileInfoRepo {

    override val classTag: String = ""

    override fun addFileInfo(video: VideoFileItem): Long {
        val curDate: Date = Calendar.getInstance(Locale.getDefault()).time
        val report = Entities.Reports(video.VideoId, video.VideoIdentifier, curDate)
        return dao.insertVideoFileInfo(report)
    }

}