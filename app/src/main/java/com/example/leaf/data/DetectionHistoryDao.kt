package com.example.leaf.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DetectionHistoryDao {
    @Query("SELECT * FROM detection_history ORDER BY timestamp DESC")
    fun getAllDetections(): Flow<List<DetectionHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetection(detection: DetectionHistory)

    @Delete
    suspend fun deleteDetection(detection: DetectionHistory)

    @Query("DELETE FROM detection_history")
    suspend fun clearHistory()
} 