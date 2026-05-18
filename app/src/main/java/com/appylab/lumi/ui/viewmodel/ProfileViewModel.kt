package com.appylab.lumi.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.appylab.lumi.data.db.LumiDatabase
import com.appylab.lumi.data.model.FaceAnalysis
import com.appylab.lumi.data.model.SubscriptionTier
import com.appylab.lumi.data.repository.ProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val age: Int = 0,
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
    val showComingSoon: Boolean = false,
    val isEditingPersonalDetails: Boolean = false,
    val ageDraft: String = "",
    val skinTypeDraft: String = "",
    val skinToneDraft: String = "",
    val undertoneDraft: String = "",
    val locationDraft: String = "",
    val saveError: Boolean = false
)

private data class ProfileDialogState(
    val signOut: Boolean = false,
    val delete: Boolean = false,
    val comingSoon: Boolean = false
)

private data class ProfileEditState(
    val isEditing: Boolean = false,
    val ageDraft: String = "",
    val skinTypeDraft: String = "",
    val skinToneDraft: String = "",
    val undertoneDraft: String = "",
    val locationDraft: String = "",
    val saveError: Boolean = false
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val db = LumiDatabase.getInstance(application)
    private val repository = ProfileRepository(
        userProfileDao = db.userProfileDao(),
        faceAnalysisDao = db.faceAnalysisDao(),
        appStateDao = db.appStateDao()
    )

    private val _dialogs = MutableStateFlow(ProfileDialogState())
    private val _editState = MutableStateFlow(ProfileEditState())

    val uiState: StateFlow<ProfileUiState> = combine(
        combine(
            repository.observeProfile(),
            repository.observeRecentScans(),
            repository.observeAppState()
        ) { profile, scans, state -> Triple(profile, scans, state) },
        combine(_dialogs, _editState) { d, e -> Pair(d, e) }
    ) { (profile, scans, state), (dialogs, edit) ->
        ProfileUiState(
            displayName = profile?.displayName.orEmpty(),
            email = profile?.email.orEmpty(),
            photoUrl = profile?.photoUrl.orEmpty(),
            age = profile?.age ?: 0,
            skinType = profile?.skinTypePref.orEmpty(),
            skinTone = profile?.skinTonePref.orEmpty(),
            undertone = profile?.undertonePref.orEmpty(),
            location = profile?.location.orEmpty(),
            subscriptionTier = if (state.subscriptionTier == "PRO") SubscriptionTier.PRO else SubscriptionTier.FREE,
            recentScans = scans,
            notifScanReminders = state.notifScanReminders,
            notifPromotions = state.notifPromotions,
            notifUpdates = state.notifUpdates,
            showSignOutDialog = dialogs.signOut,
            showDeleteDialog = dialogs.delete,
            showComingSoon = dialogs.comingSoon,
            isEditingPersonalDetails = edit.isEditing,
            ageDraft = edit.ageDraft,
            skinTypeDraft = edit.skinTypeDraft,
            skinToneDraft = edit.skinToneDraft,
            undertoneDraft = edit.undertoneDraft,
            locationDraft = edit.locationDraft,
            saveError = edit.saveError
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ProfileUiState()
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = repository.observeProfile().first()
            if (profile != null && profile.skinTonePref.isEmpty() && profile.undertonePref.isEmpty()) {
                val latestScan = repository.observeRecentScans().first().firstOrNull()
                if (latestScan != null) {
                    repository.prependSkinFromScan(latestScan.skinTone, latestScan.undertone)
                }
            }
        }
    }

    // Notification toggles
    fun setNotifScanReminders(v: Boolean) { viewModelScope.launch { repository.updateNotifScanReminders(v) } }
    fun setNotifPromotions(v: Boolean) { viewModelScope.launch { repository.updateNotifPromotions(v) } }
    fun setNotifUpdates(v: Boolean) { viewModelScope.launch { repository.updateNotifUpdates(v) } }

    // Dialog state
    fun requestSignOut() { _dialogs.update { it.copy(signOut = true) } }
    fun dismissSignOut() { _dialogs.update { it.copy(signOut = false) } }
    fun requestDelete() { _dialogs.update { it.copy(delete = true) } }
    fun dismissDelete() { _dialogs.update { it.copy(delete = false) } }
    fun showComingSoon() { _dialogs.update { it.copy(comingSoon = true) } }
    fun dismissComingSoon() { _dialogs.update { it.copy(comingSoon = false) } }

    // Auth actions
    fun confirmSignOut(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.signOut()
            _dialogs.update { ProfileDialogState() }
            onDone()
        }
    }

    fun confirmDelete(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.deleteAccount()
            _dialogs.update { ProfileDialogState() }
            onDone()
        }
    }

    // Personal details edit
    fun startEditPersonalDetails() {
        val s = uiState.value
        _editState.update {
            ProfileEditState(
                isEditing = true,
                ageDraft = if (s.age > 0) s.age.toString() else "",
                skinTypeDraft = s.skinType,
                skinToneDraft = s.skinTone,
                undertoneDraft = s.undertone,
                locationDraft = s.location
            )
        }
    }

    fun cancelEditPersonalDetails() { _editState.update { ProfileEditState() } }

    fun updateAgeDraft(v: String) { _editState.update { it.copy(ageDraft = v, saveError = false) } }
    fun updateSkinTypeDraft(v: String) { _editState.update { it.copy(skinTypeDraft = v, saveError = false) } }
    fun updateSkinToneDraft(v: String) { _editState.update { it.copy(skinToneDraft = v, saveError = false) } }
    fun updateUndertoneDraft(v: String) { _editState.update { it.copy(undertoneDraft = v, saveError = false) } }
    fun updateLocationDraft(v: String) { _editState.update { it.copy(locationDraft = v, saveError = false) } }

    fun savePersonalDetails() {
        val edit = _editState.value
        val age = if (edit.ageDraft.isEmpty()) 0 else (edit.ageDraft.toIntOrNull() ?: -1)
        if (age == -1 || (age != 0 && (age < 10 || age > 99))) {
            _editState.update { it.copy(saveError = true) }
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updatePersonalDetails(
                    age = age,
                    skinType = edit.skinTypeDraft,
                    skinTone = edit.skinToneDraft,
                    undertone = edit.undertoneDraft,
                    location = edit.locationDraft
                )
                _editState.update { ProfileEditState() }
            } catch (_: Exception) {
                _editState.update { it.copy(saveError = true) }
            }
        }
    }
}
