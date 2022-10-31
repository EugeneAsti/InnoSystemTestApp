package ru.aeyu.innosystemstestapp.providers

import android.content.Context
import ru.aeyu.innosystemstestapp.db.DAOSavePlayingVideoInfo
import ru.aeyu.innosystemstestapp.db.MainDataBase
import ru.aeyu.innosystemstestapp.repoimpl.SaveFileInfoImplDB
import ru.aeyu.innosystemstestapp.repositories.SaveFileInfoRepo

class Providers(private val context: Context) {
    /**
     * Данная функция вычисляет с какой реализацией репозитория будем работать.
     * Проверяет доступность удаленной базы и если база недоступна, то
     * возвращается реализация репозитория с локальной БД
     */
    fun getSaveRepo(): SaveFileInfoRepo {
        // TODO Т.к. задания на запись в удаленный источник данных не было, то возвращаем
        //  локальный репозиторий. А так сначала проверям можем ли писать в удалённую БД
        //  и только после того как удаленная БД недоступна возыращаем реализацию репозитория к
        //  локальному источнику данных
        val dao: DAOSavePlayingVideoInfo = MainDataBase.getInstance(context).daoSaveFileInfo()
        return SaveFileInfoImplDB(context, dao)
    }
}

