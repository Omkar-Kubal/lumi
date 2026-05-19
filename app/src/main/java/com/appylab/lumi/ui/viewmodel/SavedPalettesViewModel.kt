package com.appylab.lumi.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.appylab.lumi.data.db.ColorAnalysisEntity
import com.appylab.lumi.data.db.LumiDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray

data class PaletteSummary(
    val faceAnalysisId: Long,
    val season: String,
    val attributes: String,
    val updatedAt: Long,
    val swatchHexes: List<String>
)

data class SavedPalettesUiState(
    val isLoading: Boolean = true,
    val palettes: List<PaletteSummary> = emptyList(),
    val pendingDeleteId: Long? = null,
    val recentlyDeleted: Pair<Long, ColorAnalysisEntity>? = null
)

class SavedPalettesViewModel(application: Application) : AndroidViewModel(application) {

    private val db = LumiDatabase.getInstance(application)
    private val colorAnalysisDao = db.colorAnalysisDao()

    private val _uiState = MutableStateFlow(SavedPalettesUiState())
    val uiState: StateFlow<SavedPalettesUiState> = _uiState

    private var undoJob: Job? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            colorAnalysisDao.observeSaved().collectLatest { entities ->
                _uiState.update { it.copy(isLoading = false, palettes = entities.map { e -> e.toPaletteSummary() }) }
            }
        }
    }

    fun requestDelete(id: Long) = _uiState.update { it.copy(pendingDeleteId = id) }
    fun dismissDelete() = _uiState.update { it.copy(pendingDeleteId = null) }

    fun confirmDelete(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = colorAnalysisDao.getByFaceAnalysisId(id)
            if (entity != null) {
                colorAnalysisDao.updateSaved(id, false, 0L)
                _uiState.update { it.copy(pendingDeleteId = null, recentlyDeleted = Pair(id, entity)) }
                undoJob = launch {
                    delay(5_000)
                    _uiState.update { it.copy(recentlyDeleted = null) }
                }
            } else {
                _uiState.update { it.copy(pendingDeleteId = null) }
            }
        }
    }

    fun undoDelete() {
        undoJob?.cancel()
        undoJob = null
        val (id, _) = _uiState.value.recentlyDeleted ?: return
        viewModelScope.launch(Dispatchers.IO) {
            colorAnalysisDao.updateSaved(id, true, System.currentTimeMillis())
            _uiState.update { it.copy(recentlyDeleted = null) }
        }
    }

    private fun ColorAnalysisEntity.toPaletteSummary() = PaletteSummary(
        faceAnalysisId = faceAnalysisId,
        season = colorSeason.ifEmpty { "My Palette" },
        attributes = seasonAttributes(colorSeason),
        updatedAt = if (savedAt > 0L) savedAt else createdAt,
        swatchHexes = parseHexes(personalPaletteJson).take(5)
    )

    private fun parseHexes(json: String): List<String> = try {
        val arr = JSONArray(json)
        (0 until arr.length()).map { arr.getJSONObject(it).optString("hex", "#CCCCCC") }
    } catch (_: Exception) { emptyList() }

    private fun seasonAttributes(season: String) = when {
        season.contains("Soft Summer", true) -> "Cool • Soft • Light"
        season.contains("True Summer", true) -> "Cool • Muted • Medium"
        season.contains("Light Summer", true) -> "Cool • Light • Muted"
        season.contains("Dark Winter", true) -> "Cool • Dark • Deep"
        season.contains("Bright Winter", true) -> "Cool • Bright • Clear"
        season.contains("True Winter", true) || season.contains("Cool Winter", true) -> "Cool • Bright • Deep"
        season.contains("Soft Autumn", true) -> "Warm • Soft • Muted"
        season.contains("Dark Autumn", true) -> "Warm • Dark • Deep"
        season.contains("True Autumn", true) || season.contains("Warm Autumn", true) -> "Warm • Rich • Deep"
        season.contains("Bright Spring", true) -> "Warm • Bright • Clear"
        season.contains("Light Spring", true) -> "Warm • Light • Clear"
        season.contains("True Spring", true) -> "Warm • Fresh • Clear"
        season.contains("Summer", true) -> "Cool • Soft"
        season.contains("Winter", true) -> "Cool • Deep"
        season.contains("Autumn", true) -> "Warm • Deep"
        season.contains("Spring", true) -> "Warm • Clear"
        else -> ""
    }
}
