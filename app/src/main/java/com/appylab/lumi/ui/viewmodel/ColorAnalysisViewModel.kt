package com.appylab.lumi.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.appylab.lumi.data.db.AppStateEntity
import com.appylab.lumi.data.db.LumiDatabase
import com.appylab.lumi.data.db.NotificationEntity
import com.appylab.lumi.data.model.FaceAnalysis
import com.appylab.lumi.data.repository.ColorAnalysisRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ColorAnalysisUiState(
    val isLoading: Boolean = true,
    val skinTone: String = "",
    val undertone: String = "",
    val colorSeason: String = "",
    val personalPaletteJson: String = "[]",
    val avoidColorsJson: String = "[]",
    val clothingRecsJson: String = "[]",
    val hairColorRecsJson: String = "[]",
    val lipColorsJson: String = "[]",
    val eyeColorsJson: String = "[]",
    val isPaletteSaved: Boolean = false
)

class ColorAnalysisViewModel(application: Application) : AndroidViewModel(application) {

    private val db = LumiDatabase.getInstance(application)
    private val repository = ColorAnalysisRepository(
        colorAnalysisDao = db.colorAnalysisDao(),
        faceAnalysisDao  = db.faceAnalysisDao()
    )

    private val _uiState = MutableStateFlow(ColorAnalysisUiState())
    val uiState: StateFlow<ColorAnalysisUiState> = _uiState.asStateFlow()

    private var currentFaceAnalysisId: Long = 0L

    fun load(faceAnalysisId: Long) {
        if (faceAnalysisId == 0L) {
            _uiState.update { it.copy(isLoading = false) }
            return
        }
        currentFaceAnalysisId = faceAnalysisId
        viewModelScope.launch(Dispatchers.IO) {
            // Load face analysis for skinTone + undertone
            val faceAnalysis: FaceAnalysis? = repository.getFaceAnalysis(faceAnalysisId)

            _uiState.update {
                it.copy(
                    skinTone  = faceAnalysis?.skinTone.orEmpty(),
                    undertone = faceAnalysis?.undertone.orEmpty()
                )
            }

            // Observe color analysis entity for live AI data
            repository.observeColorAnalysis(faceAnalysisId).collectLatest { entity ->
                _uiState.update {
                    it.copy(
                        isLoading           = false,
                        colorSeason         = entity?.colorSeason.orEmpty(),
                        personalPaletteJson = entity?.personalPaletteJson ?: "[]",
                        avoidColorsJson     = entity?.avoidColorsJson ?: "[]",
                        clothingRecsJson    = entity?.clothingRecsJson ?: "[]",
                        hairColorRecsJson   = entity?.hairColorRecsJson ?: "[]",
                        lipColorsJson       = entity?.lipColorsJson ?: "[]",
                        eyeColorsJson       = entity?.eyeColorsJson ?: "[]",
                        isPaletteSaved      = entity?.isSaved ?: false
                    )
                }
            }
        }
    }

    fun toggleSavePalette(faceAnalysisId: Long, saved: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleSaved(faceAnalysisId, saved)
            if (saved) {
                db.notificationDao().insert(
                    NotificationEntity(
                        type      = "palette_saved",
                        title     = "Palette saved to profile",
                        body      = "Your colour palette is now saved in your profile",
                        timestamp = System.currentTimeMillis()
                    )
                )
                val state = db.appStateDao().getAppState() ?: AppStateEntity()
                db.appStateDao().upsert(state.copy(unreadNotificationCount = state.unreadNotificationCount + 1))
            }
        }
    }
}
