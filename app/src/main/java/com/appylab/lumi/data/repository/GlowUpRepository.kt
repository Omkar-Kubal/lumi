package com.appylab.lumi.data.repository

import com.appylab.lumi.data.db.AppStateDao
import com.appylab.lumi.data.db.FaceAnalysisDao
import com.appylab.lumi.data.db.GlowUpDao
import com.appylab.lumi.data.db.GlowUpEntity
import com.appylab.lumi.data.model.GlowUpImageStatus
import com.appylab.lumi.data.model.ScanScorePoint
import com.appylab.lumi.data.model.SubscriptionTier
import kotlinx.coroutines.flow.Flow

class GlowUpRepository(
    private val glowUpDao: GlowUpDao,
    private val faceAnalysisDao: FaceAnalysisDao,
    private val appStateDao: AppStateDao
) {

    fun observeGlowUp(faceAnalysisId: Long): Flow<GlowUpEntity?> =
        glowUpDao.observeByFaceAnalysisId(faceAnalysisId)

    suspend fun getSubscriptionTier(): SubscriptionTier {
        val tier = appStateDao.getAppState()?.subscriptionTier ?: "FREE"
        return runCatching { SubscriptionTier.valueOf(tier) }.getOrDefault(SubscriptionTier.FREE)
    }

    /** Returns all scan scores oldest → newest for the progress chart. */
    suspend fun getScoreHistory(userId: Int = 1): List<ScanScorePoint> =
        faceAnalysisDao.getScoreHistory(userId).map { entity ->
            ScanScorePoint(
                date = entity.timestamp,
                score = entity.glowUpScore,
                faceAnalysisId = entity.id
            )
        }

    suspend fun upsertGlowUp(entity: GlowUpEntity) = glowUpDao.upsert(entity)

    suspend fun updateImageStatus(faceAnalysisId: Long, url: String?, status: GlowUpImageStatus) =
        glowUpDao.updateImageStatus(faceAnalysisId, url, status.name)
}
