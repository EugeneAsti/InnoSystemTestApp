package ru.aeyu.innosystemstestapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Entities.Reports::class], version = 1, exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class MainDataBase : RoomDatabase() {

    abstract fun daoSaveFileInfo(): DAOSavePlayingVideoInfo

    companion object {
        const val DB_NAME = "minitv.db"

        private lateinit var INSTANCE: MainDataBase

        fun getInstance(context: Context): MainDataBase {

            if (!::INSTANCE.isInitialized) {
                synchronized(this) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        MainDataBase::class.java,
                        DB_NAME
                    ).fallbackToDestructiveMigration()
                            // отключает режим WAL
                        .setJournalMode(JournalMode.TRUNCATE)
                        .build()

                }
            }
            return INSTANCE
        }
    }
}
