package com.appylab.lumi.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.appylab.lumi.data.db.LumiDatabase
import com.appylab.lumi.data.db.UserProfileEntity
import com.appylab.lumi.data.model.BeautyTip
import com.appylab.lumi.data.model.FaceAnalysis
import com.appylab.lumi.data.model.SubscriptionTier
import com.appylab.lumi.data.model.TrendingLook
import com.appylab.lumi.data.repository.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

data class HomeUiState(
    val isLoading: Boolean = true,
    val displayName: String = "",
    val photoUrl: String? = null,
    val greetingTime: String = "",
    val greetingSubtitle: String = "",
    val unreadNotificationCount: Int = 0,
    val lastScan: FaceAnalysis? = null,
    val subscriptionTier: SubscriptionTier = SubscriptionTier.FREE,
    val dailyTip: BeautyTip? = null,
    val currentTipWindowIndex: Int = 0,
    val tipsWindow: List<BeautyTip> = emptyList(),
    val savedTipIds: Set<Int> = emptySet(),
    val trendingLooks: List<TrendingLook> = emptyList(),
    val showUpsellBanner: Boolean = false,
    val resultsUnviewed: Boolean = false
)

private data class HomePrimaryData(
    val profile: UserProfileEntity?,
    val lastScan: FaceAnalysis?,
    val tier: SubscriptionTier,
    val unreadCount: Int,
    val resultsUnviewed: Boolean
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = LumiDatabase.getInstance(application)
    private val repository = HomeRepository(
        context = application,
        userProfileDao = db.userProfileDao(),
        faceAnalysisDao = db.faceAnalysisDao(),
        savedTipDao = db.savedTipDao(),
        appStateDao = db.appStateDao()
    )

    private val allTips: List<BeautyTip> = repository.loadBeautyTips()
    private val allTrendingLooks: List<TrendingLook> = repository.loadTrendingLooks()

    // Session-only: resets on every cold start (ViewModel recreation)
    private val _bannerDismissed = MutableStateFlow(false)
    private val _tipWindowIndex = MutableStateFlow(0)

    val uiState: StateFlow<HomeUiState> = combine(
        combine(
            repository.observeUserProfile(),
            repository.observeLatestScan(),
            repository.observeSubscriptionTier(),
            repository.observeUnreadNotificationCount(),
            repository.observeResultsUnviewed()
        ) { profile, scan, tier, count, resultsUnviewed ->
            HomePrimaryData(profile, scan, tier, count, resultsUnviewed)
        },
        combine(
            repository.observeSavedTipIds(),
            _bannerDismissed,
            _tipWindowIndex
        ) { savedIds, bannerDismissed, tipIdx ->
            Triple(savedIds, bannerDismissed, tipIdx)
        }
    ) { primary, (savedIds, bannerDismissed, tipIdx) ->
        val hour = LocalTime.now().hour
        val (greetingTime, greetingSubtitle) = when {
            hour in 5..11 -> "Good morning," to "Let's enhance your natural glow"
            hour in 12..16 -> "Good afternoon," to "Ready to perfect your look?"
            else -> "Good evening," to "Time for your evening routine"
        }

        val tipsWindow = computeTipsWindow(primary.profile?.id ?: 1, allTips)

        HomeUiState(
            isLoading = false,
            displayName = primary.profile?.displayName.orEmpty(),
            photoUrl = primary.profile?.photoUrl?.takeIf { it.isNotEmpty() },
            greetingTime = greetingTime,
            greetingSubtitle = greetingSubtitle,
            unreadNotificationCount = primary.unreadCount,
            lastScan = primary.lastScan,
            subscriptionTier = primary.tier,
            dailyTip = tipsWindow.getOrNull(tipIdx),
            currentTipWindowIndex = tipIdx,
            tipsWindow = tipsWindow,
            savedTipIds = savedIds,
            trendingLooks = allTrendingLooks,
            showUpsellBanner = primary.tier == SubscriptionTier.FREE && !bannerDismissed,
            resultsUnviewed = primary.resultsUnviewed
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    /** Hides banner for this session only — reappears on next cold start per PRD §4.7 */
    fun dismissBanner() {
        _bannerDismissed.value = true
    }

    fun onBellTapped() {
        viewModelScope.launch { repository.clearUnreadCount() }
    }

    fun toggleBookmark(tipId: Int) {
        viewModelScope.launch {
            if (tipId in uiState.value.savedTipIds) {
                repository.removeSavedTip(tipId)
            } else {
                repository.saveTip(tipId)
            }
        }
    }

    fun navigateTipWindow(index: Int) {
        _tipWindowIndex.value = index.coerceIn(0, (uiState.value.tipsWindow.size - 1).coerceAtLeast(0))
    }

    /**
     * Deterministic 5-tip window per PRD §4.5:
     * tipIndex = (userId.hashCode() + dayOfYear) % tips.size
     * Same user sees the same tip all day; reproducible across app restarts.
     */
    private fun computeTipsWindow(userId: Int, tips: List<BeautyTip>): List<BeautyTip> {
        if (tips.isEmpty()) return emptyList()
        val dayOfYear = LocalDate.now().dayOfYear
        val raw = (userId.hashCode() + dayOfYear) % tips.size
        val startIndex = if (raw < 0) raw + tips.size else raw
        return (0 until 5).map { offset -> tips[(startIndex + offset) % tips.size] }
    }

    companion object {
        /** Maps a glow-up score to its verdict display string per PRD §4.3.3 */
        fun glowScoreVerdict(score: Int): String = when {
            score >= 90 -> "Glowing! Your look is on point ✦"
            score >= 75 -> "Great! Your glow is coming through ✦"
            score >= 60 -> "Good foundation — let's build on this"
            score >= 40 -> "Potential unlocked — your glow is growing"
            else -> "Let's work on your glow together"
        }
    }
}
