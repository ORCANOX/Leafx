package com.example.leaf.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.leaf.DetectionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DetectionViewModel : ViewModel() {
    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    private val _detectionResult = MutableStateFlow<DetectionResult?>(null)
    val detectionResult: StateFlow<DetectionResult?> = _detectionResult.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    fun setSelectedImage(uri: Uri?) {
        _selectedImageUri.value = uri
    }

    fun setDetectionResult(result: DetectionResult?) {
        _detectionResult.value = result
    }

    fun setProcessing(processing: Boolean) {
        _isProcessing.value = processing
    }

    fun clearDetection() {
        _selectedImageUri.value = null
        _detectionResult.value = null
        _isProcessing.value = false
    }
} 