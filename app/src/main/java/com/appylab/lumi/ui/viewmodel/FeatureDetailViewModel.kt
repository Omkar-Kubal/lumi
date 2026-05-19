package com.appylab.lumi.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.appylab.lumi.data.db.LumiDatabase
import com.appylab.lumi.data.model.FaceAnalysis
import com.appylab.lumi.data.repository.FeatureDetailRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FeatureDetailUiState(
    val isLoading: Boolean = true,
    val analysis: FaceAnalysis? = null,
    val symmetryScore: Int = 75,
    val improvementPriorityJson: String = "[]"
)

class FeatureDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val db = LumiDatabase.getInstance(application)
    private val repository = FeatureDetailRepository(
        featureDetailDao = db.featureDetailDao(),
        faceAnalysisDao  = db.faceAnalysisDao()
    )

    private val _uiState = MutableStateFlow(FeatureDetailUiState())
    val uiState: StateFlow<FeatureDetailUiState> = _uiState.asStateFlow()

    fun load(faceAnalysisId: Long) {
        if (faceAnalysisId == 0L) {
            _uiState.update { it.copy(isLoading = false) }
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            // Load face analysis for feature details
            val faceAnalysis: FaceAnalysis? = repository.getFaceAnalysis(faceAnalysisId)

            _uiState.update { it.copy(analysis = faceAnalysis) }

            // Observe feature detail entity for AI symmetry score + improvement priority
            repository.observeFeatureDetail(faceAnalysisId).collectLatest { entity ->
                val defaultSymmetry = minOf(95, (faceAnalysis?.glowUpScore ?: 75) + 7)
                val entityScore = entity?.symmetryScore ?: -1
                val resolvedScore = if (entityScore > 0) entityScore else defaultSymmetry
                android.util.Log.d("FeatureDetailVM",
                    "symmetryScore source: ${if (entityScore > 0) "Gemini($entityScore)" else "formula($resolvedScore)"}")
                _uiState.update {
                    it.copy(
                        isLoading               = false,
                        symmetryScore           = resolvedScore,
                        improvementPriorityJson = entity?.improvementPriorityJson ?: "[]"
                    )
                }
            }
        }
    }
}
