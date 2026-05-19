package com.appylab.lumi.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ColorAnalysisDao {

    @Query("SELECT * FROM color_analysis WHERE faceAnalysisId = :id LIMIT 1")
    fun observeByFaceAnalysisId(id: Long): Flow<ColorAnalysisEntity?>

    @Query("SELECT * FROM color_analysis WHERE faceAnalysisId = :id LIMIT 1")
    suspend fun getByFaceAnalysisId(id: Long): ColorAnalysisEntity?

    @Query("SELECT * FROM color_analysis WHERE isSaved = 1 ORDER BY createdAt DESC")
    fun observeSaved(): Flow<List<ColorAnalysisEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ColorAnalysisEntity)

    @Query("UPDATE color_analysis SET isSaved = :saved, savedAt = :savedAt WHERE faceAnalysisId = :id")
    suspend fun updateSaved(id: Long, saved: Boolean, savedAt: Long)

    @Query("SELECT faceAnalysisId FROM color_analysis")
    fun observeAllFaceAnalysisIds(): Flow<List<Long>>

    @Query("DELETE FROM color_analysis WHERE faceAnalysisId = :id")
    suspend fun deleteByFaceAnalysisId(id: Long)
}
