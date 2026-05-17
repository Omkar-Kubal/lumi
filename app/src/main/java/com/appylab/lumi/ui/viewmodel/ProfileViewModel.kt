package com.appylab.lumi.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.appylab.lumi.data.db.LumiDatabase
import com.appylab.lumi.data.model.FaceAnalysis
import com.appylab.lumi.data.model.SubscriptionTier
import com.appylab.lumi.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val ageRange: String = "",
    val skinType: String = "",
    val skinTone: String = "",
    val undertone: String = "",
    val location: String = "",
    val subscriptionTier: SubscriptionTier = SubscriptionTier.FREE,
    val recentScans: List<FaceAnalysis> = emptyList(),
    val notifScanReminders: Boolean = true,
    val notifPromotions: Boolean = false,
    val notifUpdates: Boolean = true,
    val showSignOutDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showComingSoon: Boolean = false
)

private data class ProfileDialogState(
    val signOut: Boolean = false,
    val delete: Boolean = false,
    val comingSoon: Boolean = false
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val db = LumiDatabase.getInstance(application)
    private val repository = ProfileRepository(
        userProfileDao = db.userProfileDao(),
        faceAnalysisDao = db.faceAnalysisDao(),
        appStateDao = db.appStateDao()
    )

    private val _dialogs = MutableStateFlow(ProfileDialogState())

    val uiState: StateFlow<ProfileUiState> = combine(
        combine(
            repository.observeProfile(),
            repository.observeRecentScans(),
            repository.observeAppState()
        ) { profile, scans, state ->
            Triple(profile, scans, state)
        },
        _dialogs
    ) { (profile, scans, state), dialogs ->
        val latestScan = scans.firstOrNull()
        ProfileUiState(
            displayName = profile?.displayName.orEmpty(),
            email = profile?.email.orEmpty(),
            photoUrl = profile?.photoUrl.orEmpty(),
            ageRange = profile?.ageRange.orEmpty(),
            skinType = profile?.skinConcerns.orEmpty(),
            skinTone = latestScan?.skinTone.orEmpty(),
            undertone = latestScan?.undertone.orEmpty(),
            location = profile?.location.orEmpty(),
            subscriptionTier = if (state.subscriptionTier == "PRO") SubscriptionTier.PRO else SubscriptionTier.FREE,
            recentScans = scans,
            notifScanReminders = state.notifScanReminders,
            notifPromotions = state.notifPromotions,
            notifUpdates = state.notifUpdates,
            showSignOutDialog = dialogs.signOut,
            showDeleteDialog = dialogs.delete,
            showComingSoon = dialogs.comingSoon
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileUiState()
    )

    fun setNotifScanReminders(value: Boolean) {
        viewModelScope.launch { repository.updateNotifScanReminders(value) }
    }

    fun setNotifPromotions(value: Boolean) {
        viewModelScope.launch { repository.updateNotifPromotions(value) }
    }

    fun setNotifUpdates(value: Boolean) {
        viewModelScope.launch { repository.updateNotifUpdates(value) }
    }

    fun requestSignOut() { _dialogs.update { it.copy(signOut = true) } }
    fun dismissSignOut() { _dialogs.update { it.copy(signOut = false) } }

    fun requestDelete() { _dialogs.update { it.copy(delete = true) } }
    fun dismissDelete() { _dialogs.update { it.copy(delete = false) } }

    fun showComingSoon() { _dialogs.update { it.copy(comingSoon = true) } }
    fun dismissComingSoon() { _dialogs.update { it.copy(comingSoon = false) } }

    fun confirmSignOut(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.signOut()
            _dialogs.update { ProfileDialogState() }
            onDone()
        }
    }
}
