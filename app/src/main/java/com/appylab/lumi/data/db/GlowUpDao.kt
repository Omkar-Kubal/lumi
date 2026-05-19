package com.appylab.lumi.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GlowUpDao {

    @Query("SELECT * FROM glow_up WHERE faceAnalysisId = :faceAnalysisId LIMIT 1")
    fun observeByFaceAnalysisId(faceAnalysisId: Long): Flow<GlowUpEntity?>

    @Query("SELECT * FROM glow_up WHERE faceAnalysisId = :faceAnalysisId LIMIT 1")
    suspend fun getByFaceAnalysisId(faceAnalysisId: Long): GlowUpEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: GlowUpEntity)

    @Query(
        "UPDATE glow_up SET glowUpImageUrl = :url, glowUpImageStatus = :status " +
        "WHERE faceAnalysisId = :faceAnalysisId"
    )
    suspend fun updateImageStatus(faceAnalysisId: Long, url: String?, status: String)

    @Query("SELECT faceAnalysisId FROM glow_up")
    fun observeAllFaceAnalysisIds(): Flow<List<Long>>

    @Query("DELETE FROM glow_up WHERE faceAnalysisId = :faceAnalysisId")
    suspend fun deleteByFaceAnalysisId(faceAnalysisId: Long)
}
