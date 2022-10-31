package ru.aeyu.innosystemstestapp.utils

import android.provider.MediaStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.io.*

class ExportFileToLocalMemory {

    /**
     * Экспорт файла в разрешённую папку для анализа.
     * @param fullOutPutFileName - Имя файла с расширением. Типа dbname.db
     * @return Обернутый во Flow результат String
     */

    fun export(fullOutPutFileName: String, fromFile: File): Flow<String> = flow {
        val inputStream: InputStream = try {
            FileInputStream(fromFile)
        } catch (ex: IOException) {
            ex.printStackTrace()
            this.emit("Create fileInputStreamError: ${ex.localizedMessage}")
            null
        } ?: return@flow

        val toFile = File(fullOutPutFileName)
        try {
            val outputStream = FileOutputStream(toFile)
            val buffer = ByteArray(1024)
            while (true) {
                val read = inputStream.read(buffer)
                if (read < 0)
                    break
                outputStream.write(buffer, 0, read)
            }
            outputStream.flush()
            inputStream.close()
            outputStream.close()
            this.emit("File exported successfully")
        } catch (ex: IOException) {
            ex.printStackTrace()
            this.emit("Save file Error: ${ex.localizedMessage}")
        }
    }

}