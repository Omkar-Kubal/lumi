package com.appylab.lumi.data.repository

import com.appylab.lumi.data.db.ColorAnalysisDao
import com.appylab.lumi.data.db.ColorAnalysisEntity
import com.appylab.lumi.data.db.FaceAnalysisDao
import com.appylab.lumi.data.db.toModel
import com.appylab.lumi.data.model.FaceAnalysis
import kotlinx.coroutines.flow.Flow

class ColorAnalysisRepository(
    private val colorAnalysisDao: ColorAnalysisDao,
    private val faceAnalysisDao: FaceAnalysisDao
) {
    fun observeColorAnalysis(faceAnalysisId: Long): Flow<ColorAnalysisEntity?> =
        colorAnalysisDao.observeByFaceAnalysisId(faceAnalysisId)

    suspend fun getFaceAnalysis(faceAnalysisId: Long): FaceAnalysis? =
        faceAnalysisDao.getById(faceAnalysisId)?.toModel()

    suspend fun upsert(entity: ColorAnalysisEntity) = colorAnalysisDao.upsert(entity)

    suspend fun toggleSaved(faceAnalysisId: Long, saved: Boolean) =
        colorAnalysisDao.updateSaved(faceAnalysisId, saved, if (saved) System.currentTimeMillis() else 0L)
}
