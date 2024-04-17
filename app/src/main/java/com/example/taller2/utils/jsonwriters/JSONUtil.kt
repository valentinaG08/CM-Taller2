package com.example.taller2.utils.jsonwriters

import android.content.Context
import android.util.Log
import org.json.JSONArray
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileWriter
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Writer


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
            val file = File(directory, filename)
            if (!file.exists()) {
                return "[]"
            }
            return file.readText()
        }

        private fun writeToFile(data: String, filename: String, directory: File) {
            var output : Writer?
            try {
                val file = File(directory, filename)
                output = BufferedWriter(FileWriter(file))
                output.write(data)
                output.close()
            } catch (e: IOException) {
                Log.e("Exception", "File write failed: $e")
            }
        }
    }
}