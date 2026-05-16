package com.appylab.lumi.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FaceAnalysisDao {
    @Query("SELECT * FROM face_analysis WHERE userId = :userId ORDER BY timestamp DESC LIMIT 1")
    fun getLatest(userId: Int = 1): Flow<FaceAnalysisEntity?>

    @Insert
    suspend fun insert(entity: FaceAnalysisEntity): Long
}
