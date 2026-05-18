package com.appylab.lumi.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FaceAnalysisDao {
    @Query("SELECT * FROM face_analysis WHERE userId = :userId ORDER BY timestamp DESC LIMIT 1")
    fun getLatest(userId: Int = 1): Flow<FaceAnalysisEntity?>

    @Query("SELECT * FROM face_analysis WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): FaceAnalysisEntity?

    @Query("SELECT * FROM face_analysis WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecent(userId: Int = 1, limit: Int = 5): Flow<List<FaceAnalysisEntity>>

    @Insert
    suspend fun insert(entity: FaceAnalysisEntity): Long

    /** All scans for the user ordered oldest → newest for progress chart */
    @Query("SELECT * FROM face_analysis WHERE userId = :userId ORDER BY timestamp ASC")
    suspend fun getScoreHistory(userId: Int = 1): List<FaceAnalysisEntity>

    @Query("DELETE FROM face_analysis")
    suspend fun deleteAll()
}
