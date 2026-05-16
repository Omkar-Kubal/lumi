package com.appylab.lumi.data.repository

import android.content.Context
import com.appylab.lumi.data.db.AppStateDao
import com.appylab.lumi.data.db.AppStateEntity
import com.appylab.lumi.data.db.FaceAnalysisDao
import com.appylab.lumi.data.db.SavedTipDao
import com.appylab.lumi.data.db.SavedTipEntity
import com.appylab.lumi.data.db.UserProfileDao
import com.appylab.lumi.data.model.BeautyTip
import com.appylab.lumi.data.model.FaceAnalysis
import com.appylab.lumi.data.model.SubscriptionTier
import com.appylab.lumi.data.model.TrendingLook
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray

class HomeRepository(
    private val context: Context,
    private val userProfileDao: UserProfileDao,
    private val faceAnalysisDao: FaceAnalysisDao,
    private val savedTipDao: SavedTipDao,
    private val appStateDao: AppStateDao
) {
    fun observeUserProfile() = userProfileDao.observe()

    fun observeLatestScan(): Flow<FaceAnalysis?> =
        faceAnalysisDao.getLatest().map { entity ->
            entity?.let {
                FaceAnalysis(
                    id = it.id,
                    userId = it.userId,
                    glowUpScore = it.glowUpScore,
                    faceShape = it.faceShape,
                    skinTone = it.skinTone,
                    undertone = it.undertone,
                    eyeShape = it.eyeShape,
                    imageUrl = it.imageUrl,
                    timestamp = it.timestamp
                )
            }
        }

    fun observeAppState(): Flow<AppStateEntity> =
        appStateDao.observe().map { it ?: AppStateEntity() }

    fun observeSubscriptionTier(): Flow<SubscriptionTier> =
        observeAppState().map { state ->
            if (state.subscriptionTier == "PRO") SubscriptionTier.PRO else SubscriptionTier.FREE
        }

    fun observeUnreadNotificationCount(): Flow<Int> =
        observeAppState().map { it.unreadNotificationCount }

    fun observeResultsUnviewed(): Flow<Boolean> =
        observeAppState().map { it.resultsUnviewed }

    fun observeSavedTipIds(): Flow<Set<Int>> =
        savedTipDao.observeAll().map { it.toSet() }

    suspend fun clearUnreadCount() = updateAppState { it.copy(unreadNotificationCount = 0) }

    suspend fun saveTip(tipId: Int) = savedTipDao.save(SavedTipEntity(tipId))

    suspend fun removeSavedTip(tipId: Int) = savedTipDao.remove(SavedTipEntity(tipId))

    private suspend fun updateAppState(transform: (AppStateEntity) -> AppStateEntity) {
        val current = appStateDao.observe().first() ?: AppStateEntity()
        appStateDao.upsert(transform(current))
    }

    fun loadBeautyTips(): List<BeautyTip> = runCatching {
        val json = context.assets.open("beauty_tips.json").bufferedReader().readText()
        val array = JSONArray(json)
        List(array.length()) { i ->
            val obj = array.getJSONObject(i)
            BeautyTip(
                id = obj.getInt("id"),
                text = obj.getString("text"),
                category = obj.getString("category")
            )
        }
    }.getOrDefault(emptyList())

    fun loadTrendingLooks(): List<TrendingLook> = runCatching {
        val json = context.assets.open("trending_looks.json").bufferedReader().readText()
        val array = JSONArray(json)
        List(array.length()) { i ->
            val obj = array.getJSONObject(i)
            TrendingLook(
                id = obj.getInt("id"),
                tag = obj.getString("tag"),
                title = obj.getString("title"),
                subtitle = obj.getString("subtitle"),
                imageUrl = obj.getString("imageUrl")
            )
        }
    }.getOrDefault(emptyList())
}
