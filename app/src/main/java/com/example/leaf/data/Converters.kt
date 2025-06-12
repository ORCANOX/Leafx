package com.example.leaf.data

import android.graphics.Bitmap
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromDetectionsList(value: List<Detection>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toDetectionsList(value: String): List<Detection> {
        val listType = object : TypeToken<List<Detection>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromBitmap(bitmap: Bitmap?): ByteArray? {
        if (bitmap == null) return null
        val stream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    @TypeConverter
    fun toBitmap(byteArray: ByteArray?): Bitmap? {
        if (byteArray == null) return null
        return android.graphics.BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }
} 