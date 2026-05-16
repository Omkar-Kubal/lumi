package com.appylab.lumi.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.appylab.lumi.data.db.LumiDatabase
import com.appylab.lumi.data.db.UserProfileEntity
import com.appylab.lumi.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UserRepository(
        LumiDatabase.getInstance(application).userProfileDao()
    )

    private val profile: StateFlow<UserProfileEntity?> =
        repository.observeProfile()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null
            )

    private val _cameraPermissionGranted = MutableStateFlow(false)
    private val _notificationPermissionGranted = MutableStateFlow(false)
    private val _beautyGoals = MutableStateFlow<Set<String>>(emptySet())
    private val _skinConcerns = MutableStateFlow<Set<String>>(emptySet())
    private val _ageRange = MutableStateFlow("")
    private val _authType = MutableStateFlow("")

    val beautyGoals: StateFlow<Set<String>> = _beautyGoals
    val skinConcerns: StateFlow<Set<String>> = _skinConcerns
    val ageRange: StateFlow<String> = _ageRange
    val authType: StateFlow<String> = _authType
    val cameraPermissionGranted: StateFlow<Boolean> = _cameraPermissionGranted
    val notificationPermissionGranted: StateFlow<Boolean> = _notificationPermissionGranted

    init {
        viewModelScope.launch {
            profile.collect { savedProfile ->
                savedProfile ?: return@collect
                _beautyGoals.value = savedProfile.beautyGoals.toValueSet()
                _skinConcerns.value = savedProfile.skinConcerns.toValueSet()
                _ageRange.value = savedProfile.ageRange
                _authType.value = savedProfile.authType
                _cameraPermissionGranted.value = savedProfile.cameraPermissionGranted
                _notificationPermissionGranted.value = savedProfile.notificationPermissionGranted
            }
        }
    }

    fun savePersonalization(goals: Set<String>, concerns: Set<String>, age: String) {
        _beautyGoals.value = goals
        _skinConcerns.value = concerns
        _ageRange.value = age

        viewModelScope.launch {
            val current = currentProfile()
            repository.upsert(
                current.copy(
                    beautyGoals = goals.joinToString(","),
                    skinConcerns = concerns.joinToString(","),
                    ageRange = age
                )
            )
        }
    }

    fun setAuthType(authType: String) {
        _authType.value = authType
    }

    fun setCameraPermissionGranted(granted: Boolean) {
        _cameraPermissionGranted.value = granted
    }

    fun setNotificationPermissionGranted(granted: Boolean) {
        _notificationPermissionGranted.value = granted
    }

    fun completeOnboarding(
        authType: String,
        cameraGranted: Boolean,
        notifGranted: Boolean
    ) {
        viewModelScope.launch {
            repository.upsert(
                currentProfile().copy(
                    hasCompletedOnboarding = true,
                    authType = authType,
                    beautyGoals = _beautyGoals.value.joinToString(","),
                    skinConcerns = _skinConcerns.value.joinToString(","),
                    ageRange = _ageRange.value,
                    cameraPermissionGranted = cameraGranted,
                    notificationPermissionGranted = notifGranted
                )
            )
        }
    }

    fun skipOnboarding() {
        _authType.value = "skip"

        viewModelScope.launch {
            repository.upsert(
                currentProfile().copy(
                    hasCompletedOnboarding = true,
                    authType = "skip"
                )
            )
        }
    }

    private suspend fun currentProfile(): UserProfileEntity =
        profile.value ?: repository.observeProfile().first() ?: UserProfileEntity()

    private fun String?.toValueSet(): Set<String> =
        this.orEmpty()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
}
