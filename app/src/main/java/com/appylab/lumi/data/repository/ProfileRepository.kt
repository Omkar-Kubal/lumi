package com.appylab.lumi.data.repository

import com.appylab.lumi.data.db.AppStateDao
import com.appylab.lumi.data.db.AppStateEntity
import com.appylab.lumi.data.db.FaceAnalysisDao
import com.appylab.lumi.data.db.UserProfileDao
import com.appylab.lumi.data.db.UserProfileEntity
import com.appylab.lumi.data.db.toModel
import com.appylab.lumi.data.model.FaceAnalysis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class ProfileRepository(
    private val userProfileDao: UserProfileDao,
    private val faceAnalysisDao: FaceAnalysisDao,
    private val appStateDao: AppStateDao
) {
    fun observeProfile(): Flow<UserProfileEntity?> = userProfileDao.observe()

    fun observeRecentScans(): Flow<List<FaceAnalysis>> =
        faceAnalysisDao.getRecent().map { list -> list.map { it.toModel() } }

    fun observeAppState(): Flow<AppStateEntity> =
        appStateDao.observe().map { it ?: AppStateEntity() }

    suspend fun updateNotifScanReminders(value: Boolean) = updateAppState { it.copy(notifScanReminders = value) }
    suspend fun updateNotifPromotions(value: Boolean) = updateAppState { it.copy(notifPromotions = value) }
    suspend fun updateNotifUpdates(value: Boolean) = updateAppState { it.copy(notifUpdates = value) }

    suspend fun signOut() {
        val current = userProfileDao.observe().first() ?: UserProfileEntity()
        userProfileDao.upsert(
            current.copy(
                hasCompletedOnboarding = false,
                displayName = "",
                photoUrl = "",
                email = ""
            )
        )
    }

    private suspend fun updateAppState(transform: (AppStateEntity) -> AppStateEntity) {
        val current = appStateDao.getAppState() ?: AppStateEntity()
        appStateDao.upsert(transform(current))
    }
}
