package ru.aeyu.innosystemstestapp.json

import java.io.*
import java.lang.NullPointerException

class ReadJsonFromFileUseCase {

    //private val strBuilder = StringBuilder("")

    fun read(inputStream: InputStream): String {
        return try {
             inputStream.bufferedReader()
                .use {reader ->
                    reader.readText()
                }
//            val fis = FileInputStream(File(fullJsonFileName))
//            val reader = BufferedReader(InputStreamReader(fis))
//            while (true) {
//                val line: String = reader.readLine() ?: break
//                strBuilder.append(line).append("\n")
//            }
//            reader.close()
//            fis.close()
//            strBuilder
        } catch (npe: NullPointerException) {
            npe.printStackTrace()
            ""
        } catch (se: SecurityException) {
            se.printStackTrace()
            ""
        } catch (fnfe: FileNotFoundException) {
            fnfe.printStackTrace()
            ""
        } catch(ioe: IOException) {
            ioe.printStackTrace()
            ""
        } catch (ex: Exception) {
            ex.printStackTrace()
            ""
        }
    }
}