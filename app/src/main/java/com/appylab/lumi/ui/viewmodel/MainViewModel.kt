package com.appylab.lumi.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.appylab.lumi.data.db.LumiDatabase
import com.appylab.lumi.data.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UserRepository(
        LumiDatabase.getInstance(application).userProfileDao()
    )

    val isOnboardingComplete: StateFlow<Boolean?> =
        repository.observeProfile()
            .map { profile -> profile?.hasCompletedOnboarding ?: false }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null
            )

    /**
     * True when an email-auth user's session has expired (> 15 days since last login).
     * Skip/guest users always return false — no login gate for them.
     */
    val requiresLogin: StateFlow<Boolean?> =
        repository.observeProfile()
            .map { profile ->
                if (profile == null) null
                else {
                    val isEmailUser = profile.authType == "email" && profile.passwordHash != null
                    val ttlExpired  = System.currentTimeMillis() - profile.lastLoginAt > SESSION_TTL_MS
                    isEmailUser && ttlExpired
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null
            )

    companion object {
        private const val SESSION_TTL_MS = 15L * 24 * 60 * 60 * 1_000 // 15 days
    }
}
