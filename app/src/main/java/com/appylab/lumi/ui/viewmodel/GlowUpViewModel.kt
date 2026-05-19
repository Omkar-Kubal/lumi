package com.appylab.lumi.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.appylab.lumi.data.db.LumiDatabase
import com.appylab.lumi.data.model.GlowUpError
import com.appylab.lumi.data.model.GlowUpImageStatus
import com.appylab.lumi.data.model.ImpactLevel
import com.appylab.lumi.data.model.ImprovementArea
import com.appylab.lumi.data.model.ScanScorePoint
import com.appylab.lumi.data.model.StepGuide
import com.appylab.lumi.data.model.SubscriptionTier
import com.appylab.lumi.data.model.parseImprovementAreas
import com.appylab.lumi.data.model.parseStepGuides
import com.appylab.lumi.data.repository.GlowUpRepository
import com.appylab.lumi.data.worker.GlowUpImageWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ── UI State ──────────────────────────────────────────────────────────────────

data class GlowUpUiState(
    val isLoading: Boolean = true,
    val originalImageUrl: String? = null,
    val glowUpImageUrl: String? = null,
    val glowUpImageStatus: GlowUpImageStatus = GlowUpImageStatus.PENDING,
    val score: Int = 0,
    /** Null when fewer than 2 scans exist */
    val scoreDelta: Int? = null,
    val verdictLabel: String = "",
    val verdictBody: String = "",
    val improvementAreas: List<ImprovementArea> = emptyList(),
    /** Name of the currently active step guide tab */
    val selectedArea: String = "",
    val activeStepGuide: StepGuide? = null,
    val progressData: List<ScanScorePoint> = emptyList(),
    val isGeneratingShareCard: Boolean = false,
    val error: GlowUpError? = null
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

class GlowUpViewModel(application: Application) : AndroidViewModel(application) {

    private val db = LumiDatabase.getInstance(application)
    private val repository = GlowUpRepository(
        glowUpDao      = db.glowUpDao(),
        faceAnalysisDao = db.faceAnalysisDao(),
        appStateDao    = db.appStateDao()
    )

    private val _uiState = MutableStateFlow(GlowUpUiState())
    val uiState: StateFlow<GlowUpUiState> = _uiState.asStateFlow()

    /** Cached step guides for the current entity — updated inside collectLatest. */
    private val _cachedStepGuides = MutableStateFlow<List<StepGuide>>(emptyList())

    fun load(faceAnalysisId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Score history for progress chart (one-shot, before Flow observe)
            val history = repository.getScoreHistory()
            val progressData = history
            val scoreDelta = if (history.size >= 2)
                history.last().score - history[history.size - 2].score
            else null

            // 3. Observe GlowUpEntity as live Flow (updates when image generation completes)
            repository.observeGlowUp(faceAnalysisId).collectLatest { entity ->
                if (entity == null) {
                    _uiState.update {
                        it.copy(isLoading = false, error = GlowUpError.NotFound)
                    }
                    return@collectLatest
                }

                val areas = parseImprovementAreas(entity.improvementAreasJson)
                val guides = parseStepGuides(entity.stepGuidesJson)
                _cachedStepGuides.value = guides
                val selectedArea = _uiState.value.selectedArea
                    .ifBlank { areas.firstOrNull()?.area ?: "" }
                val activeGuide = guides.find { it.area == selectedArea }

                val status = runCatching {
                    GlowUpImageStatus.valueOf(entity.glowUpImageStatus)
                }.getOrDefault(GlowUpImageStatus.PENDING)

                _uiState.update {
                    it.copy(
                        isLoading          = false,
                        originalImageUrl   = entity.originalImageUrl.takeIf { s -> s.isNotBlank() },
                        glowUpImageUrl     = entity.glowUpImageUrl,
                        glowUpImageStatus  = status,
                        score              = entity.score,
                        scoreDelta         = scoreDelta,
                        verdictLabel       = verdictLabel(entity.score),
                        verdictBody        = verdictBody(entity.score),
                        improvementAreas   = areas,
                        selectedArea       = selectedArea,
                        activeStepGuide    = activeGuide,
                        progressData       = progressData,
                        error              = null
                    )
                }
            }
        }
    }

    /** Called when the user taps an improvement area card or a step guide tab. */
    fun selectArea(area: String) {
        val guides = _cachedStepGuides.value
        _uiState.update {
            it.copy(
                selectedArea    = area,
                activeStepGuide = guides.find { g -> g.area == area }
            )
        }
    }

    /**
     * Retries glow-up image generation after a FAILED status.
     * Re-queues [GlowUpImageWorker] with the given faceAnalysisId.
     */
    fun retryImageGeneration(faceAnalysisId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateImageStatus(faceAnalysisId, null, GlowUpImageStatus.PENDING)
        }
        val request = OneTimeWorkRequestBuilder<GlowUpImageWorker>()
            .setInputData(
                Data.Builder()
                    .putLong(GlowUpImageWorker.KEY_FACE_ANALYSIS_ID, faceAnalysisId)
                    .build()
            )
            .setConstraints(
                androidx.work.Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(getApplication()).enqueue(request)
    }

    /** Enqueues GlowUpImageWorker for the given faceAnalysisId (called by ScanViewModel post-scan). */
    fun enqueueImageGeneration(faceAnalysisId: Long) = retryImageGeneration(faceAnalysisId)

    companion object {
        fun verdictLabel(score: Int): String = when {
            score >= 90 -> "Excellent Glow-Up!"
            score >= 75 -> "Great Progress!"
            score >= 60 -> "Good Progress!"
            score >= 40 -> "Keep Going!"
            else        -> "Just Getting Started"
        }

        fun verdictBody(score: Int): String = when {
            score >= 90 -> "You're at the top of your glow game."
            score >= 75 -> "You're on the right track — keep it up."
            score >= 60 -> "Consistent care is paying off."
            score >= 40 -> "Small steps make big changes."
            else        -> "Every glow-up starts with a single step."
        }

        /** Default improvement areas used as fallback when Gemini data is absent. */
        val FALLBACK_IMPROVEMENT_AREAS = listOf(
            ImprovementArea("Skin",    ImpactLevel.HIGH,   18, "skin"),
            ImprovementArea("Brows",   ImpactLevel.HIGH,   15, "brows"),
            ImprovementArea("Texture", ImpactLevel.MEDIUM,  9, "skin"),
            ImprovementArea("Balance", ImpactLevel.MEDIUM,  7, "contour"),
            ImprovementArea("Lips",    ImpactLevel.MEDIUM,  5, "lips")
        )

        /** Default step guides used as fallback when Gemini data is absent. */
        val FALLBACK_STEP_GUIDES = listOf(
            StepGuide(
                area = "Skin",
                goal = "Achieve an even, hydrated complexion with daily SPF protection.",
                recommendations = listOf(
                    "Apply SPF 30+ every morning before leaving the house.",
                    "Use a Vitamin C serum to brighten and even skin tone.",
                    "Apply retinol at night 2–3×/week to boost cell turnover.",
                    "Drink at least 8 glasses of water daily for deep hydration."
                )
            ),
            StepGuide(
                area = "Brows",
                goal = "Define your natural arch for a more structured, polished look.",
                recommendations = listOf(
                    "Fill sparse areas lightly with a brow pencil matching your hair color.",
                    "Apply castor oil or brow serum nightly to promote growth.",
                    "Visit a brow specialist every 4–6 weeks for professional shaping."
                )
            ),
            StepGuide(
                area = "Texture",
                goal = "Smooth and refine skin texture for a healthy, poreless finish.",
                recommendations = listOf(
                    "Exfoliate 2×/week with a gentle AHA product.",
                    "Apply niacinamide serum morning and night to minimize pores.",
                    "Switch to a silk pillowcase to reduce friction overnight."
                )
            ),
            StepGuide(
                area = "Balance",
                goal = "Enhance your natural facial symmetry through strategic makeup.",
                recommendations = listOf(
                    "Use matte bronzer 2 shades darker to subtly contour.",
                    "Apply highlighter on cheekbones, brow bone, and cupid's bow.",
                    "Blend product edges well — harsh lines disrupt natural balance."
                )
            ),
            StepGuide(
                area = "Lips",
                goal = "Keep lips soft, defined, and full-looking every day.",
                recommendations = listOf(
                    "Use a lip scrub once a week to remove dead skin.",
                    "Line slightly inside your natural lip line for a fuller look.",
                    "Apply a thick lip balm or sleeping mask every night."
                )
            )
        )
    }
}
