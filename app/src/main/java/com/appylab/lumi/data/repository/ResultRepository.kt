package com.appylab.lumi.data.repository

import com.appylab.lumi.data.db.AppStateDao
import com.appylab.lumi.data.db.AppStateEntity
import com.appylab.lumi.data.db.FaceAnalysisDao
import com.appylab.lumi.data.db.toModel
import com.appylab.lumi.data.model.FaceAnalysis
import com.appylab.lumi.data.model.SubscriptionTier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ResultRepository(
    private val faceAnalysisDao: FaceAnalysisDao,
    private val appStateDao: AppStateDao
) {
    fun observeLatestAnalysis(): Flow<FaceAnalysis?> =
        faceAnalysisDao.getLatest().map { it?.toModel() }

    suspend fun getAnalysisById(id: Long): FaceAnalysis? =
        faceAnalysisDao.getById(id)?.toModel()

    fun observeSubscriptionTier(): Flow<SubscriptionTier> =
        appStateDao.observe().map { state ->
            val s = state ?: AppStateEntity()
            if (s.subscriptionTier == "PRO") SubscriptionTier.PRO else SubscriptionTier.FREE
        }
}
