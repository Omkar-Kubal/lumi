package com.appylab.lumi.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.appylab.lumi.data.db.LumiDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest

data class EmailSignUpUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isSaving: Boolean = false
)

/**
 * Handles the email/password sign-up step in the onboarding flow.
 * Saves email + password hash to the existing UserProfile row.
 * Does NOT set hasCompletedOnboarding — Screen 8 owns that via finalizeOnboarding().
 */
class EmailSignUpViewModel(application: Application) : AndroidViewModel(application) {

    private val userProfileDao = LumiDatabase.getInstance(application).userProfileDao()

    private val _uiState = MutableStateFlow(EmailSignUpUiState())
    val uiState: StateFlow<EmailSignUpUiState> = _uiState

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events: SharedFlow<String> = _events.asSharedFlow()

    fun updateEmail(value: String) =
        _uiState.update { it.copy(email = value.trim(), emailError = null) }

    fun updatePassword(value: String) =
        _uiState.update { it.copy(password = value, passwordError = null) }

    fun updateConfirmPassword(value: String) =
        _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = null) }

    fun signUp(onSuccess: () -> Unit) {
        val state = _uiState.value
        var hasError = false

        if (state.email.isBlank()) {
            _uiState.update { it.copy(emailError = "Email is required") }
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _uiState.update { it.copy(emailError = "Enter a valid email address") }
            hasError = true
        }

        if (state.password.length < 8) {
            _uiState.update { it.copy(passwordError = "Password must be at least 8 characters") }
            hasError = true
        }

        if (state.confirmPassword != state.password) {
            _uiState.update { it.copy(confirmPasswordError = "Passwords do not match") }
            hasError = true
        }

        if (hasError) return

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val current = userProfileDao.observe().first()
                    ?: run {
                        _events.emit("Something went wrong — please try again.")
                        _uiState.update { it.copy(isSaving = false) }
                        return@launch
                    }
                userProfileDao.upsert(
                    current.copy(
                        email        = state.email,
                        passwordHash = state.password.sha256(),
                        authType     = "email",
                        // displayName defaults to the part before @ if not set
                        displayName  = current.displayName.ifEmpty {
                            state.email.substringBefore("@")
                        }
                        // hasCompletedOnboarding intentionally NOT set here —
                        // Screen 8 calls finalizeOnboarding() for that.
                    )
                )
                _uiState.update { it.copy(isSaving = false) }
                withContext(Dispatchers.Main) { onSuccess() }
            } catch (_: Exception) {
                _uiState.update { it.copy(isSaving = false) }
                _events.emit("Something went wrong — please try again.")
            }
        }
    }
}

private fun String.sha256(): String =
    MessageDigest.getInstance("SHA-256")
        .digest(toByteArray())
        .joinToString("") { "%02x".format(it) }
