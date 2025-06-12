package com.example.leaf.data

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.*

@Entity(tableName = "detection_history")
@TypeConverters(Converters::class)
data class DetectionHistory(
    @PrimaryKey
    val id: Long = System.currentTimeMillis(),
    val timestamp: Date = Date(),
    val imageUri: String,
    val leafCount: Int,
    val detections: List<Detection>,
    val resultImage: Bitmap? = null
)

data class Detection(
    val className: String,
    val confidence: Float
) 