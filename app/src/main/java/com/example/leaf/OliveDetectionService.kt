package com.example.leaf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import android.util.Base64

class OliveDetectionService(
    private val context: Context,
    private val serverUrl: String
) {
    private val client = OkHttpClient()

    suspend fun detectOlives(imageUri: Uri): DetectionResult = withContext(Dispatchers.IO) {
        Log.d("OliveDetectionService", "Using server URL: $serverUrl")

        if (!serverUrl.startsWith("http://") && !serverUrl.startsWith("https://")) {
            Log.e("OliveDetectionService", "Invalid server URL: $serverUrl")
            throw IllegalArgumentException("Invalid server URL: $serverUrl")
        }

        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            val imageBytes = stream.toByteArray()
            
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "image",
                    "image.jpg",
                    imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                )
                .build()

            val request = Request.Builder()
                .url("$serverUrl/detect")
                .post(requestBody)
                .header("Accept", "application/json")
                .build()

            Log.d("OliveDetectionService", "Sending request to: ${request.url}")

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()
                Log.d("OliveDetectionService", "Response code: ${response.code}")
                
                if (!response.isSuccessful) {
                    throw IOException("Server returned ${response.code}: $responseBody")
                }

                if (responseBody == null) {
                    return@withContext DetectionResult(success = false, error = "Empty response from server")
                }

                try {
                    val jsonResponse = JSONObject(responseBody)
                    
                    if (!jsonResponse.has("detection_info") || !jsonResponse.has("image")) {
                        Log.e("OliveDetectionService", "Invalid response format: $responseBody")
                        return@withContext DetectionResult(
                            success = false,
                            error = "Invalid response format from server"
                        )
                    }

                    val detectionInfo = jsonResponse.getJSONObject("detection_info")
                    val leaves = detectionInfo.getJSONArray("leaves")
                    val leafCount = detectionInfo.getInt("leaf_count")
                    
                    val detections = mutableListOf<LeafDetection>()
                    for (i in 0 until leaves.length()) {
                        val leaf = leaves.getJSONObject(i)
                        detections.add(
                            LeafDetection(
                                className = leaf.getString("class_name"),
                                confidence = leaf.getDouble("confidence") / 100.0 
                            )
                        )
                    }

                  
                    val base64Image = jsonResponse.getString("image")
                    val imageBytes = Base64.decode(base64Image, Base64.DEFAULT)
                    val resultImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    
                    DetectionResult(
                        success = true,
                        detections = detections,
                        resultImage = resultImage,
                        leafCount = leafCount
                    )
                } catch (e: Exception) {
                    Log.e("OliveDetection", "Error parsing response", e)
                    DetectionResult(success = false, error = "Error parsing server response: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("OliveDetection", "Error detecting olives", e)
            DetectionResult(success = false, error = e.message ?: "Unknown error")
        }
    }
}

data class LeafDetection(
    val className: String,
    val confidence: Double
)

data class DetectionResult(
    val success: Boolean,
    val detections: List<LeafDetection> = emptyList(),
    val resultImage: Bitmap? = null,
    val leafCount: Int = 0,
    val error: String? = null
) 