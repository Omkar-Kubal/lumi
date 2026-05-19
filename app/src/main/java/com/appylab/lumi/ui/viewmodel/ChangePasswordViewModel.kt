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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest

data class ChangePasswordUiState(
    val isLoading: Boolean = true,
    val hasExistingPassword: Boolean = false,
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val currentPasswordError: String? = null,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val isSaving: Boolean = false
)

class ChangePasswordViewModel(application: Application) : AndroidViewModel(application) {

    private val userProfileDao = LumiDatabase.getInstance(application).userProfileDao()

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val events: SharedFlow<String> = _events.asSharedFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = userProfileDao.getPasswordHash()
            _uiState.update { it.copy(isLoading = false, hasExistingPassword = existing != null) }
        }
    }

    fun updateCurrentPassword(value: String) =
        _uiState.update { it.copy(currentPassword = value, currentPasswordError = null) }

    fun updateNewPassword(value: String) =
        _uiState.update { it.copy(newPassword = value, newPasswordError = null) }

    fun updateConfirmPassword(value: String) =
        _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = null) }

    fun save(onSuccess: () -> Unit) {
        val state = _uiState.value

        var hasError = false
        if (state.hasExistingPassword && state.currentPassword.isBlank()) {
            _uiState.update { it.copy(currentPasswordError = "Enter your current password") }
            hasError = true
        }
        if (state.newPassword.length < 8) {
            _uiState.update { it.copy(newPasswordError = "Password must be at least 8 characters") }
            hasError = true
        }
        if (state.hasExistingPassword && state.newPassword == state.currentPassword) {
            _uiState.update { it.copy(newPasswordError = "New password must be different") }
            hasError = true
        }
        if (state.confirmPassword != state.newPassword) {
            _uiState.update { it.copy(confirmPasswordError = "Passwords do not match") }
            hasError = true
        }
        if (hasError) return

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (state.hasExistingPassword) {
                    val storedHash = userProfileDao.getPasswordHash()
                    if (storedHash != null && state.currentPassword.sha256() != storedHash) {
                        _uiState.update { it.copy(isSaving = false, currentPasswordError = "Current password is incorrect") }
                        return@launch
                    }
                }
                userProfileDao.updatePasswordHash(state.newPassword.sha256())
                _uiState.update { it.copy(isSaving = false) }
                _events.emit("Password updated successfully.")
                withContext(Dispatchers.Main) { onSuccess() }
            } catch (_: Exception) {
                _uiState.update { it.copy(isSaving = false) }
                _events.emit("Update failed — try again.")
            }
        }
    }
}

private fun String.sha256(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    return digest.digest(toByteArray()).joinToString("") { "%02x".format(it) }
}
