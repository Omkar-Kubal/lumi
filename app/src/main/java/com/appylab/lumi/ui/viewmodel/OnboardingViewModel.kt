package com.appylab.lumi.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.appylab.lumi.data.db.LumiDatabase
import com.appylab.lumi.data.db.OnboardingProgressEntity
import com.appylab.lumi.data.db.UserProfileEntity
import com.appylab.lumi.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val db         = LumiDatabase.getInstance(application)
    private val repository = UserRepository(db.userProfileDao())
    private val progressDao = db.onboardingProgressDao()

    private val profile: StateFlow<UserProfileEntity?> =
        repository.observeProfile()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // ── Step resume tracking ──────────────────────────────────────────────────

    /** Which onboarding step to resume from on cold-start (1–8). */
    val resumeStep: StateFlow<Int> = progressDao.observe()
        .map { it?.currentStep ?: 1 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 1)

    /** Call on every forward navigation between onboarding screens. */
    fun advanceStep(step: Int) {
        viewModelScope.launch {
            progressDao.upsert(
                OnboardingProgressEntity(
                    currentStep = step,
                    updatedAt   = System.currentTimeMillis()
                )
            )
        }
    }

    /** Clears the progress record once onboarding is fully done. */
    private fun clearProgress() {
        viewModelScope.launch { progressDao.clear() }
    }

    // ── Per-screen state ──────────────────────────────────────────────────────

    private val _cameraPermissionGranted      = MutableStateFlow(false)
    private val _notificationPermissionGranted = MutableStateFlow(false)
    private val _beautyGoals                  = MutableStateFlow<Set<String>>(emptySet())
    private val _skinConcerns                 = MutableStateFlow<Set<String>>(emptySet())
    private val _ageRange                     = MutableStateFlow("")
    private val _authType                     = MutableStateFlow("")
    private val _skinType                     = MutableStateFlow("")
    private val _skinTone                     = MutableStateFlow("")

    val beautyGoals:                 StateFlow<Set<String>> = _beautyGoals
    val skinConcerns:                StateFlow<Set<String>> = _skinConcerns
    val ageRange:                    StateFlow<String>      = _ageRange
    val authType:                    StateFlow<String>      = _authType
    val cameraPermissionGranted:     StateFlow<Boolean>     = _cameraPermissionGranted
    val notificationPermissionGranted: StateFlow<Boolean>   = _notificationPermissionGranted
    val skinType:                    StateFlow<String>      = _skinType
    val skinTone:                    StateFlow<String>      = _skinTone

    /** Display name from the saved profile — used on Screen 8 welcome message. */
    val displayName: StateFlow<String> = profile
        .map { it?.displayName.orEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    init {
        viewModelScope.launch {
            profile.collect { savedProfile ->
                savedProfile ?: return@collect
                _beautyGoals.value              = savedProfile.beautyGoals.toValueSet()
                _skinConcerns.value             = savedProfile.skinConcerns.toValueSet()
                _ageRange.value                 = savedProfile.ageRange
                _authType.value                 = savedProfile.authType
                _cameraPermissionGranted.value  = savedProfile.cameraPermissionGranted
                _notificationPermissionGranted.value = savedProfile.notificationPermissionGranted
                _skinType.value                 = savedProfile.skinTypePref.orEmpty()
                _skinTone.value                 = savedProfile.skinTonePref.orEmpty()
            }
        }
    }

    // ── Screen 5 — goals & concerns ──────────────────────────────────────────

    fun savePersonalization(goals: Set<String>, concerns: Set<String>) {
        _beautyGoals.value  = goals
        _skinConcerns.value = concerns
        viewModelScope.launch {
            val current = currentProfile()
            repository.upsert(
                current.copy(
                    beautyGoals  = goals.joinToString(","),
                    skinConcerns = concerns.joinToString(",")
                    // ageRange intentionally NOT touched here — Screen 6 owns it
                )
            )
        }
    }

    // ── Screen 6 — skin details ───────────────────────────────────────────────

    fun saveSkinDetails(age: String, skinType: String, skinTone: String) {
        if (age.isNotEmpty()) _ageRange.value = age
        _skinType.value = skinType
        _skinTone.value = skinTone
        viewModelScope.launch {
            val current = currentProfile()
            repository.upsert(
                current.copy(
                    ageRange     = if (age.isNotEmpty()) age else current.ageRange,
                    skinTypePref = skinType,
                    skinTonePref = skinTone
                )
            )
        }
    }

    // ── Screen 7 — auth + permissions ────────────────────────────────────────

    fun setAuthType(authType: String) {
        _authType.value = authType
    }

    fun setCameraPermissionGranted(granted: Boolean) {
        _cameraPermissionGranted.value = granted
    }

    fun setNotificationPermissionGranted(granted: Boolean) {
        _notificationPermissionGranted.value = granted
    }

    fun saveAccessSettings(authType: String, cameraGranted: Boolean, notifGranted: Boolean) {
        _authType.value                      = authType
        _cameraPermissionGranted.value       = cameraGranted
        _notificationPermissionGranted.value = notifGranted
        viewModelScope.launch {
            val current = currentProfile()
            repository.upsert(
                current.copy(
                    authType                      = authType,
                    cameraPermissionGranted       = cameraGranted,
                    notificationPermissionGranted = notifGranted
                )
            )
        }
    }

    // ── Screen 8 — finalize ───────────────────────────────────────────────────

    fun finalizeOnboarding() {
        viewModelScope.launch {
            repository.upsert(
                currentProfile().copy(
                    hasCompletedOnboarding = true,
                    lastLoginAt            = System.currentTimeMillis()
                )
            )
            clearProgress()
        }
    }

    // ── Skip (from Screen 1) ──────────────────────────────────────────────────

    fun skipOnboarding() {
        _authType.value = "skip"
        viewModelScope.launch {
            repository.upsert(
                currentProfile().copy(
                    hasCompletedOnboarding = true,
                    authType               = "skip",
                    lastLoginAt            = System.currentTimeMillis()
                )
            )
            clearProgress()
        }
    }

    // ── Legacy compat ─────────────────────────────────────────────────────────

    fun completeOnboarding(authType: String, cameraGranted: Boolean, notifGranted: Boolean) {
        saveAccessSettings(authType, cameraGranted, notifGranted)
        finalizeOnboarding()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private suspend fun currentProfile(): UserProfileEntity =
        profile.value ?: repository.observeProfile().first() ?: UserProfileEntity()

    private fun String?.toValueSet(): Set<String> =
        this.orEmpty()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
}
