package com.example.taller2.utils.data

import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.util.Date

class MyLocation(var date: Date, var lat: Double, var lng: Double) {

    fun toJSON(): JSONObject {
        val obj = JSONObject()
        try {
            obj.put("latitud", lat)
            obj.put("longitud", lng)
            obj.put("fecha", date)
        } catch (e: JSONException) {
            Log.i("INEDITO", "HAY UN ERRRO")
        }

        return obj
    }

}