package com.appylab.lumi.ui.viewmodel

import android.app.Application
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.appylab.lumi.data.db.LumiDatabase
import com.appylab.lumi.data.db.UserProfileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class EditProfileUiState(
    val isLoading: Boolean = true,
    val displayName: String = "",
    val displayNameDraft: String = "",
    val displayNameError: String? = null,
    val email: String = "",
    val photoUrl: String = "",
    val pendingPhotoUri: Uri? = null,
    val isGoogleConnected: Boolean = false,
    val isEmailAuth: Boolean = false,
    val isSaving: Boolean = false,
    val hasChanges: Boolean = false
)

class EditProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val db = LumiDatabase.getInstance(application)
    private val userProfileDao = db.userProfileDao()

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState

    // One-shot messages consumed by the screen as snackbar text
    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val events: SharedFlow<String> = _events.asSharedFlow()

    private var cachedProfile: UserProfileEntity? = null
    private var initialised = false

    init {
        viewModelScope.launch(Dispatchers.IO) {
            userProfileDao.observe().collectLatest { profile ->
                cachedProfile = profile
                _uiState.update { s ->
                    val draft = if (!initialised) profile?.displayName.orEmpty() else s.displayNameDraft
                    initialised = true
                    val isGoogle = profile?.authType == "google"
                    s.copy(
                        isLoading = false,
                        displayName = profile?.displayName.orEmpty(),
                        displayNameDraft = draft,
                        email = profile?.email.orEmpty(),
                        photoUrl = profile?.photoUrl.orEmpty(),
                        isGoogleConnected = isGoogle,
                        isEmailAuth = !isGoogle,
                        hasChanges = s.pendingPhotoUri != null ||
                                draft.trim() != profile?.displayName?.trim().orEmpty()
                    )
                }
            }
        }
    }

    fun updateDisplayNameDraft(value: String) {
        if (value.length > 40) return
        _uiState.update { s ->
            s.copy(
                displayNameDraft = value,
                displayNameError = null,
                hasChanges = s.pendingPhotoUri != null || value.trim() != s.displayName.trim()
            )
        }
    }

    /**
     * Validates photo dimensions (min 100×100) before accepting.
     * Emits a snackbar event on failure.
     * Returns true if accepted, false if rejected.
     */
    fun validateAndSetPhoto(uri: Uri): Boolean {
        val ctx = getApplication<Application>()
        return try {
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            ctx.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, opts)
            }
            if (opts.outWidth < 100 || opts.outHeight < 100) {
                viewModelScope.launch { _events.emit("Photo too small — try a larger image.") }
                false
            } else {
                _uiState.update { s ->
                    s.copy(
                        pendingPhotoUri = uri,
                        hasChanges = true
                    )
                }
                true
            }
        } catch (_: Exception) {
            viewModelScope.launch { _events.emit("Could not read photo — please try again.") }
            false
        }
    }

    fun save(onSuccess: () -> Unit) {
        val state = _uiState.value
        val trimmedName = state.displayNameDraft.trim()
        if (trimmedName.isEmpty()) {
            _uiState.update { it.copy(displayNameError = "Name cannot be empty") }
            return
        }
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val base = cachedProfile ?: UserProfileEntity()
                userProfileDao.upsert(
                    base.copy(
                        displayName = trimmedName,
                        photoUrl = state.pendingPhotoUri?.toString() ?: state.photoUrl
                    )
                )
                _uiState.update { it.copy(isSaving = false, hasChanges = false, pendingPhotoUri = null) }
                withContext(Dispatchers.Main) { onSuccess() }
            } catch (_: Exception) {
                _uiState.update { it.copy(isSaving = false) }
                _events.emit("Update failed — try again.")
            }
        }
    }
}
