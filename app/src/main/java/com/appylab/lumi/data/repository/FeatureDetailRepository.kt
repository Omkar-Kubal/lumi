package com.appylab.lumi.data.repository

import com.appylab.lumi.data.db.FaceAnalysisDao
import com.appylab.lumi.data.db.FeatureDetailDao
import com.appylab.lumi.data.db.FeatureDetailEntity
import com.appylab.lumi.data.db.toModel
import com.appylab.lumi.data.model.FaceAnalysis
import kotlinx.coroutines.flow.Flow

class FeatureDetailRepository(
    private val featureDetailDao: FeatureDetailDao,
    private val faceAnalysisDao: FaceAnalysisDao
) {
    fun observeFeatureDetail(faceAnalysisId: Long): Flow<FeatureDetailEntity?> =
        featureDetailDao.observeByFaceAnalysisId(faceAnalysisId)

    suspend fun getFaceAnalysis(faceAnalysisId: Long): FaceAnalysis? =
        faceAnalysisDao.getById(faceAnalysisId)?.toModel()

    suspend fun upsert(entity: FeatureDetailEntity) = featureDetailDao.upsert(entity)
}
