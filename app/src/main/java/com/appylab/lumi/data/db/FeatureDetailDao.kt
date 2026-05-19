package com.appylab.lumi.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FeatureDetailDao {

    @Query("SELECT * FROM feature_detail WHERE faceAnalysisId = :id LIMIT 1")
    fun observeByFaceAnalysisId(id: Long): Flow<FeatureDetailEntity?>

    @Query("SELECT * FROM feature_detail WHERE faceAnalysisId = :id LIMIT 1")
    suspend fun getByFaceAnalysisId(id: Long): FeatureDetailEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: FeatureDetailEntity)

    @Query("DELETE FROM feature_detail WHERE faceAnalysisId = :id")
    suspend fun deleteByFaceAnalysisId(id: Long)
}
