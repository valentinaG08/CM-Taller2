package com.example.taller2.utils.jsonwriters

import android.content.Context
import android.util.Log
import org.json.JSONArray
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter


class JSONUtil {

    companion object {
        fun readJsonFromFile(filename: String, directory: File) : JSONArray {
            val jsonString : String = readFromFile(filename, directory)
            return JSONArray(jsonString)
        }

        fun writeJSON(filename: String, json: JSONArray, directory: File) {
            writeToFile(json.toString(), filename, directory)
        }

        private fun readFromFile(filename: String, directory: File): String {
            var ret = ""
            var inputStream: InputStream? = null
            try {
                inputStream = File(directory, filename).inputStream()
                if (inputStream != null) {
                    val inputStreamReader = InputStreamReader(inputStream)
                    val bufferedReader = BufferedReader(inputStreamReader)
                    var receiveString: String? = ""
                    val stringBuilder = StringBuilder()
                    while (bufferedReader.readLine().also { receiveString = it } != null) {
                        stringBuilder.append(receiveString)
                    }
                    ret = stringBuilder.toString()
                }
            } catch (e: FileNotFoundException) {
                Log.e("login activity", "File not found: $e")
            } catch (e: IOException) {
                Log.e("login activity", "Can not read file: $e")
            } finally {
                try {
                    inputStream!!.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return ret
        }

        private fun writeToFile(data: String, filename: String, directory: File) {
            try {
                val outputStreamWriter =
                    OutputStreamWriter(File(directory, filename).outputStream())
                outputStreamWriter.write(data)
                outputStreamWriter.close()
            } catch (e: IOException) {
                Log.e("Exception", "File write failed: $e")
            }
        }
    }
}