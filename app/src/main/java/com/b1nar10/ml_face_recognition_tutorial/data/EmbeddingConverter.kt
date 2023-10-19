package com.b1nar10.ml_face_recognition_tutorial.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object EmbeddingConverter {
    private val gson = Gson()

    fun fromFloatArrayToJson(value: FloatArray): String {
        return gson.toJson(value)
    }

    fun fromJsonToFloatArray(value: String): FloatArray {
        val type = object : TypeToken<FloatArray>() {}.type
        return gson.fromJson(value, type)
    }
}