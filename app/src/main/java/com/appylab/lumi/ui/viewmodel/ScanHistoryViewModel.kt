package com.appylab.lumi.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.appylab.lumi.data.db.FaceAnalysisEntity
import com.appylab.lumi.data.db.LumiDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ScanFilter(val label: String) {
    ALL("All"), FACE("Face + Skin"), COLOR("Color Analysis"), GLOWUP("Glow-Up")
}

data class ScanHistoryUiState(
    val isLoading: Boolean = true,
    val allScans: List<FaceAnalysisEntity> = emptyList(),
    val filteredScans: List<FaceAnalysisEntity> = emptyList(),
    val progressData: List<FaceAnalysisEntity> = emptyList(),
    val activeFilter: ScanFilter = ScanFilter.ALL,
    val projectedScore: Int? = null,
    val projectedDate: Long? = null,
    val pendingDeleteId: Long? = null,
    val recentlyDeleted: FaceAnalysisEntity? = null
)

class ScanHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = LumiDatabase.getInstance(application)
    private val faceAnalysisDao = db.faceAnalysisDao()
    private val colorAnalysisDao = db.colorAnalysisDao()
    private val featureDetailDao = db.featureDetailDao()
    private val glowUpDao = db.glowUpDao()

    private val _uiState = MutableStateFlow(ScanHistoryUiState())
    val uiState: StateFlow<ScanHistoryUiState> = _uiState

    private val activeFilter = MutableStateFlow(ScanFilter.ALL)
    private var undoJob: Job? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                faceAnalysisDao.getRecent(limit = Int.MAX_VALUE),
                colorAnalysisDao.observeAllFaceAnalysisIds(),
                glowUpDao.observeAllFaceAnalysisIds(),
                activeFilter
            ) { scans, colorIds, glowIds, filter ->
                val colorIdSet = colorIds.toSet()
                val glowIdSet = glowIds.toSet()

                val filteredScans = when (filter) {
                    ScanFilter.ALL -> scans
                    ScanFilter.FACE -> scans.filter { it.glowUpScore > 0 }
                    ScanFilter.COLOR -> scans.filter { it.id in colorIdSet }
                    ScanFilter.GLOWUP -> scans.filter { it.id in glowIdSet }
                }

                // Oldest → newest for progress chart
                val progressData = scans.reversed()

                val (projectedScore, projectedDate) = if (progressData.size >= 2) {
                    val first = progressData.first()
                    val last = progressData.last()
                    val avgDelta = (last.glowUpScore - first.glowUpScore).toFloat() / (progressData.size - 1)
                    val projected = (last.glowUpScore + avgDelta).toInt().coerceAtMost(100)
                    if (projected > last.glowUpScore && projected < 100) {
                        val avgInterval = (last.timestamp - first.timestamp) / (progressData.size - 1)
                        projected to (last.timestamp + avgInterval)
                    } else null to null
                } else null to null

                Triple(
                    Triple(scans, filteredScans, progressData),
                    filter,
                    projectedScore to projectedDate
                )
            }.collect { (scanTriple, filter, projPair) ->
                val (scans, filteredScans, progressData) = scanTriple
                val (projectedScore, projectedDate) = projPair
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        allScans = scans,
                        filteredScans = filteredScans,
                        progressData = progressData,
                        activeFilter = filter,
                        projectedScore = projectedScore,
                        projectedDate = projectedDate
                    )
                }
            }
        }
    }

    fun setFilter(filter: ScanFilter) {
        activeFilter.value = filter
    }

    fun requestDelete(id: Long) = _uiState.update { it.copy(pendingDeleteId = id) }
    fun dismissDelete() = _uiState.update { it.copy(pendingDeleteId = null) }

    fun confirmDelete(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = _uiState.value.allScans.firstOrNull { it.id == id } ?: run {
                _uiState.update { it.copy(pendingDeleteId = null) }
                return@launch
            }
            // Cascade delete all related tables before the parent row
            colorAnalysisDao.deleteByFaceAnalysisId(id)
            featureDetailDao.deleteByFaceAnalysisId(id)
            glowUpDao.deleteByFaceAnalysisId(id)
            faceAnalysisDao.deleteById(id)

            _uiState.update { it.copy(pendingDeleteId = null, recentlyDeleted = entity) }
            undoJob = launch {
                delay(5_000)
                _uiState.update { it.copy(recentlyDeleted = null) }
            }
        }
    }

    fun undoDelete() {
        undoJob?.cancel()
        undoJob = null
        val entity = _uiState.value.recentlyDeleted ?: return
        viewModelScope.launch(Dispatchers.IO) {
            faceAnalysisDao.restore(entity)
            _uiState.update { it.copy(recentlyDeleted = null) }
        }
    }
}
