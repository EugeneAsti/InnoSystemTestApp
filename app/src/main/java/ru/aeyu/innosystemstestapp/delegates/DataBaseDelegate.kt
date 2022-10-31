package ru.aeyu.innosystemstestapp.delegates

import ru.aeyu.innosystemstestapp.db.DAOSavePlayingVideoInfo
import ru.aeyu.innosystemstestapp.db.MainDataBase
import ru.aeyu.innosystemstestapp.repositories.BaseRepo
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

private class DataBaseDelegate : ReadOnlyProperty<BaseRepo, DAOSavePlayingVideoInfo> {

    override fun getValue(thisRef: BaseRepo, property: KProperty<*>): DAOSavePlayingVideoInfo {
        return MainDataBase.getInstance(thisRef.context).daoSaveFileInfo()
    }
}

fun provideDatabaseDelegate(): ReadOnlyProperty<BaseRepo, DAOSavePlayingVideoInfo> = DataBaseDelegate()
