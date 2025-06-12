package com.example.leaf.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.leaf.data.AppDatabase
import com.example.leaf.data.DetectionHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val dao = database.detectionHistoryDao()
    
    val history: Flow<List<DetectionHistory>> = dao.getAllDetections()

    fun addDetection(detection: DetectionHistory) {
        viewModelScope.launch {
            dao.insertDetection(detection)
        }
    }

    fun deleteDetection(id: Long) {
        viewModelScope.launch {
            history.collect { detections ->
                detections.find { it.id == id }?.let { detection ->
                    dao.deleteDetection(detection)
                }
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            dao.clearHistory()
        }
    }
} 