package com.appylab.lumi.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.appylab.lumi.data.db.AppStateEntity
import com.appylab.lumi.data.db.LumiDatabase
import com.appylab.lumi.data.db.UserProfileEntity
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

data class LoginUiState(
    val isLoading: Boolean = true,
    val storedEmail: String = "",
    val password: String = "",
    val passwordError: String? = null,
    val isSubmitting: Boolean = false
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val db             = LumiDatabase.getInstance(application)
    private val userProfileDao = db.userProfileDao()
    private val faceAnalysisDao = db.faceAnalysisDao()
    private val savedTipDao    = db.savedTipDao()
    private val appStateDao    = db.appStateDao()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events: SharedFlow<String> = _events.asSharedFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = userProfileDao.observe().first()
            _uiState.update { it.copy(isLoading = false, storedEmail = profile?.email.orEmpty()) }
        }
    }

    fun updatePassword(value: String) =
        _uiState.update { it.copy(password = value, passwordError = null) }

    fun login(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Enter your password") }
            return
        }
        _uiState.update { it.copy(isSubmitting = true) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val storedHash = userProfileDao.getPasswordHash()
                if (storedHash == null || state.password.sha256() != storedHash) {
                    _uiState.update { it.copy(isSubmitting = false, passwordError = "Incorrect password") }
                    return@launch
                }
                userProfileDao.updateLastLogin(System.currentTimeMillis())
                _uiState.update { it.copy(isSubmitting = false) }
                withContext(Dispatchers.Main) { onSuccess() }
            } catch (_: Exception) {
                _uiState.update { it.copy(isSubmitting = false) }
                _events.emit("Login failed — please try again.")
            }
        }
    }

    fun deleteAccountAndStartOver() {
        viewModelScope.launch(Dispatchers.IO) {
            faceAnalysisDao.deleteAll()
            savedTipDao.deleteAll()
            userProfileDao.upsert(UserProfileEntity(hasCompletedOnboarding = false))
            appStateDao.upsert(AppStateEntity())
        }
    }
}

private fun String.sha256(): String =
    MessageDigest.getInstance("SHA-256")
        .digest(toByteArray())
        .joinToString("") { "%02x".format(it) }
