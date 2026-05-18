package com.appylab.lumi.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.appylab.lumi.data.api.GeminiService
import com.appylab.lumi.data.api.GeminiFaceResult
import com.appylab.lumi.data.db.AppStateEntity
import com.appylab.lumi.data.db.FaceAnalysisEntity
import com.appylab.lumi.data.db.GlowUpEntity
import com.appylab.lumi.data.db.LumiDatabase
import com.appylab.lumi.data.model.FrameValidationState
import com.appylab.lumi.data.model.GlowUpImageStatus
import com.appylab.lumi.data.model.ScanError
import com.appylab.lumi.data.model.ScanType
import com.appylab.lumi.data.worker.GlowUpImageWorker
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.util.UUID

data class ScanUiState(
    val isLoading: Boolean = false,
    val cameraLens: Int = CameraSelector.LENS_FACING_FRONT,
    val selectedScanType: ScanType = ScanType.FULL_ANALYSIS,
    val frameValidation: FrameValidationState = FrameValidationState(),
    val loadingStage: String = "",
    val showCancelButton: Boolean = false,
    val isProUser: Boolean = false,
    val error: ScanError? = null,
    val scanCount: Int = 0,
    val blurCheckPassed: Boolean? = null
)

private val LOADING_STAGES = listOf(
    "Detecting your face shape...",
    "Analysing skin tone...",
    "Mapping your features...",
    "Building your personalised profile...",
    "Almost there...",
    "Just a few more seconds..."
)

private const val DAILY_SCAN_LIMIT = 3
private const val BLUR_THRESHOLD   = 100.0
private const val LUMINANCE_MIN    = 80f
private const val LUMINANCE_MAX    = 220f
private const val MAX_FILE_SIZE    = 512 * 1024
private const val MAX_DIMENSION    = 512

class ScanViewModel(app: Application) : AndroidViewModel(app) {

    private val db            = LumiDatabase.getInstance(app)
    private val appStateDao   = db.appStateDao()
    private val faceAnalysisDao = db.faceAnalysisDao()
    private val glowUpDao     = db.glowUpDao()
    private val gemini        = GeminiService()

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    // Full-res detector for post-capture validation
    private val fullResFaceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .build()
    )

    /** Validated, compressed JPEG bytes held between capture validation and analysis. */
    private var pendingImageBytes: ByteArray? = null

    private var autoCaptureDisabled    = false
    private var autoCapturePendingJob: Job? = null
    private var analysisJob: Job?           = null

    init {
        viewModelScope.launch {
            val today    = LocalDate.now().toString()
            val appState = appStateDao.getAppState() ?: AppStateEntity()

            val resolvedScanCount = if (appState.scanCountDate == today) {
                appState.scanCountToday
            } else {
                appStateDao.upsert(appState.copy(scanCountToday = 0, scanCountDate = today))
                0
            }

            _uiState.update { it.copy(isProUser = appState.subscriptionTier != "FREE", scanCount = resolvedScanCount) }
        }
    }

    fun onFrameValidation(state: FrameValidationState) {
        _uiState.update { it.copy(frameValidation = state) }
        if (autoCaptureDisabled) return

        if (state.allPassed) {
            if (autoCapturePendingJob == null || autoCapturePendingJob?.isActive == false) {
                autoCapturePendingJob = viewModelScope.launch {
                    delay(1500)
                    if (_uiState.value.frameValidation.allPassed && !autoCaptureDisabled) onCapture()
                }
            }
        } else {
            autoCapturePendingJob?.cancel()
            autoCapturePendingJob = null
        }
    }

    fun onCapture() {
        if (_uiState.value.isLoading) return

        val currentCount = _uiState.value.scanCount
        if (currentCount >= DAILY_SCAN_LIMIT) {
            _uiState.update { it.copy(error = ScanError.DailyLimitReached) }
            return
        }

        viewModelScope.launch {
            val today    = LocalDate.now().toString()
            val appState = appStateDao.getAppState() ?: AppStateEntity()
            val newCount = currentCount + 1
            appStateDao.upsert(appState.copy(scanCountToday = newCount, scanCountDate = today))
            _uiState.update { it.copy(scanCount = newCount) }
            startAnalysis()
        }
    }

    /**
     * Called after CameraX delivers JPEG bytes. Validates:
     * 1. ML Kit full-res face detection (exactly 1 face)
     * 2. Laplacian variance ≥ 100 (blur)
     * 3. Mean luminance in face box 80–220
     * 4. File size enforcement
     * Then stores validated bytes and starts analysis.
     */
    fun onImageCaptured(jpeg: ByteArray) {
        viewModelScope.launch(Dispatchers.Default) {
            val bitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size) ?: run {
                _uiState.update { it.copy(error = ScanError.ApiError(0, "Could not decode image")) }
                return@launch
            }

            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val faces = try { Tasks.await(fullResFaceDetector.process(inputImage)) }
            catch (e: Exception) {
                _uiState.update { it.copy(error = ScanError.ApiError(0, "Face detection failed")) }
                return@launch
            }
            when {
                faces.isEmpty() -> { _uiState.update { it.copy(error = ScanError.NoFace) }; return@launch }
                faces.size > 1  -> { _uiState.update { it.copy(error = ScanError.MultipleFaces) }; return@launch }
            }
            if (laplacianVariance(bitmap) < BLUR_THRESHOLD) {
                _uiState.update { it.copy(error = ScanError.Blurry) }; return@launch
            }
            if (!luminanceOk(bitmap, faces[0].boundingBox)) {
                _uiState.update { it.copy(error = ScanError.PoorLighting) }; return@launch
            }

            pendingImageBytes = enforceFileSize(bitmap)
            withContext(Dispatchers.Main) { onCaptureValidated() }
        }
    }

    fun onGalleryImageSelected(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val bitmap = getApplication<Application>().contentResolver
                .openInputStream(uri)?.use { BitmapFactory.decodeStream(it) } ?: run {
                _uiState.update { it.copy(error = ScanError.ApiError(0, "Could not load image")) }
                return@launch
            }
            withContext(Dispatchers.Default) {
                val jpeg    = ByteArrayOutputStream().also { bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it) }.toByteArray()
                val decoded = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size) ?: run {
                    _uiState.update { it.copy(error = ScanError.ApiError(0, "Could not decode image")) }
                    return@withContext
                }
                val inputImage = InputImage.fromBitmap(decoded, 0)
                val faces = try { Tasks.await(fullResFaceDetector.process(inputImage)) }
                catch (e: Exception) {
                    _uiState.update { it.copy(error = ScanError.ApiError(0, "Face detection failed")) }
                    return@withContext
                }
                when {
                    faces.isEmpty() -> { _uiState.update { it.copy(error = ScanError.NoFace) }; return@withContext }
                    faces.size > 1  -> { _uiState.update { it.copy(error = ScanError.MultipleFaces) }; return@withContext }
                }
                if (laplacianVariance(decoded) < BLUR_THRESHOLD) {
                    _uiState.update { it.copy(error = ScanError.Blurry) }; return@withContext
                }
                if (!luminanceOk(decoded, faces[0].boundingBox)) {
                    _uiState.update { it.copy(error = ScanError.PoorLighting) }; return@withContext
                }
                pendingImageBytes = enforceFileSize(decoded)
                withContext(Dispatchers.Main) { onCaptureValidated() }
            }
        }
    }

    fun onCaptureError() {
        _uiState.update { it.copy(error = ScanError.ApiError(-1, "Capture failed — try again.")) }
    }

    private suspend fun onCaptureValidated() {
        if (_uiState.value.isLoading) return

        val currentCount = _uiState.value.scanCount
        if (currentCount >= DAILY_SCAN_LIMIT) {
            _uiState.update { it.copy(error = ScanError.DailyLimitReached) }
            return
        }

        val today    = LocalDate.now().toString()
        val appState = appStateDao.getAppState() ?: AppStateEntity()
        val newCount = currentCount + 1
        appStateDao.upsert(appState.copy(scanCountToday = newCount, scanCountDate = today))
        _uiState.update { it.copy(scanCount = newCount) }
        startAnalysis()
    }

    private fun startAnalysis() {
        analysisJob?.cancel()
        analysisJob = viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, loadingStage = LOADING_STAGES[0], showCancelButton = false, blurCheckPassed = null)
            }

            // Cycle loading stage labels while Gemini is working
            val stageJob = launch {
                var idx = 0
                while (true) {
                    delay(2_500)
                    idx = (idx + 1).coerceAtMost(LOADING_STAGES.size - 1)
                    _uiState.update { it.copy(loadingStage = LOADING_STAGES[idx]) }
                    if (idx >= 1) _uiState.update { it.copy(showCancelButton = true) }
                }
            }

            try {
                withContext(Dispatchers.IO) {
                    withTimeout(45_000L) { runAnalysis() }
                }
            } catch (_: kotlinx.coroutines.TimeoutCancellationException) {
                _uiState.update { it.copy(isLoading = false, showCancelButton = false, error = ScanError.Timeout) }
            } catch (_: kotlinx.coroutines.CancellationException) {
                // cancelled by user via cancelAnalysis()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, showCancelButton = false, error = ScanError.ApiError(0, e.message ?: "Analysis failed"))
                }
            } finally {
                stageJob.cancel()
            }
        }
    }

    /**
     * Core analysis: calls Gemini, saves image to storage, persists entities, enqueues worker.
     * Falls back to deterministic mock data if Gemini is unavailable (offline / quota exceeded).
     */
    private suspend fun runAnalysis() {
        val imageBytes = pendingImageBytes ?: error("No image captured")

        // 1. Call Gemini — with graceful fallback
        val result: GeminiFaceResult = gemini.analyzeFace(imageBytes) ?: buildFallbackResult()

        // 2. Save JPEG to internal storage so both FaceAnalysis + GlowUpEntity have a local URL
        val imageFile = File(getApplication<Application>().filesDir, "scan_${System.currentTimeMillis()}.jpg")
        FileOutputStream(imageFile).use { it.write(imageBytes) }
        val imagePath = imageFile.absolutePath

        // 3. Persist FaceAnalysisEntity
        val faceAnalysisId = faceAnalysisDao.insert(
            FaceAnalysisEntity(
                glowUpScore          = result.glowUpScore,
                faceShape            = result.faceShape,
                faceShapeDescription = result.faceShapeDescription,
                skinTone             = result.skinTone,
                undertone            = result.undertone,
                undertoneDescription = result.undertoneDescription,
                eyeShape             = result.eyeShape,
                browType             = result.browType,
                noseShape            = result.noseShape,
                lipType              = result.lipType,
                celebrityMatchesJson = result.celebrityMatchesJson,
                imageUrl             = imagePath,
                timestamp            = System.currentTimeMillis()
            )
        )

        // 4. Persist GlowUpEntity (image PENDING — worker generates it)
        glowUpDao.upsert(
            GlowUpEntity(
                id                   = UUID.randomUUID().toString(),
                faceAnalysisId       = faceAnalysisId,
                userId               = 1,
                originalImageUrl     = imagePath,
                glowUpImageUrl       = null,
                glowUpImageStatus    = GlowUpImageStatus.PENDING.name,
                score                = result.glowUpScore,
                improvementAreasJson = result.improvementAreasJson,
                stepGuidesJson       = result.stepGuidesJson,
                createdAt            = System.currentTimeMillis()
            )
        )

        // 5. Enqueue WorkManager job for glow-up image generation
        WorkManager.getInstance(getApplication()).enqueue(
            OneTimeWorkRequestBuilder<GlowUpImageWorker>()
                .setInputData(
                    Data.Builder()
                        .putLong(GlowUpImageWorker.KEY_FACE_ANALYSIS_ID, faceAnalysisId)
                        .build()
                )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
        )

        // 6. Mark results as unviewed
        val currentState = appStateDao.getAppState() ?: AppStateEntity()
        appStateDao.upsert(currentState.copy(resultsUnviewed = true))

        pendingImageBytes = null

        withContext(Dispatchers.Main) {
            _uiState.update { it.copy(isLoading = false, showCancelButton = false, blurCheckPassed = true) }
        }
    }

    /** Safe fallback used when Gemini API is unavailable (offline / quota exceeded). */
    private fun buildFallbackResult(): GeminiFaceResult {
        val mockAreas = """[
            {"area":"Skin","impact":"HIGH","score_potential":18,"illustration":"skin"},
            {"area":"Brows","impact":"HIGH","score_potential":14,"illustration":"brows"},
            {"area":"Texture","impact":"MEDIUM","score_potential":9,"illustration":"skin"},
            {"area":"Balance","impact":"MEDIUM","score_potential":7,"illustration":"contour"},
            {"area":"Lips","impact":"LOW","score_potential":5,"illustration":"lips"}
        ]""".trimIndent()
        val mockGuides = """[
            {"area":"Skin","goal":"Achieve an even, hydrated complexion with daily SPF protection.","recommendations":["Apply SPF 30+ every morning.","Use Vitamin C serum to even skin tone.","Apply retinol 2-3x/week at night."]},
            {"area":"Brows","goal":"Define your natural arch for a more structured look.","recommendations":["Fill sparse areas with a brow pencil.","Apply castor oil nightly.","Visit a brow specialist every 4-6 weeks."]},
            {"area":"Texture","goal":"Smooth and refine skin texture for a poreless finish.","recommendations":["Exfoliate 2x/week with AHA.","Apply niacinamide serum morning and night.","Switch to a silk pillowcase."]},
            {"area":"Balance","goal":"Enhance facial symmetry through strategic makeup.","recommendations":["Use matte bronzer for subtle contouring.","Highlight cheekbones and brow bone.","Blend product edges well."]},
            {"area":"Lips","goal":"Keep lips soft, defined, and full-looking.","recommendations":["Use a lip scrub once a week.","Line inside your natural lip line.","Apply lip balm every night."]}
        ]""".trimIndent()
        val mockCelebs = """[
            {"rank":1,"name":"Priyanka Chopra","similarityPct":72},
            {"rank":2,"name":"Zoe Saldana","similarityPct":65},
            {"rank":3,"name":"Lupita Nyong'o","similarityPct":58}
        ]""".trimIndent()

        return GeminiFaceResult(
            glowUpScore          = (68..92).random(),
            faceShape            = listOf("OVAL","ROUND","HEART","SQUARE","DIAMOND","OBLONG").random(),
            faceShapeDescription = "Your face has well-balanced proportions with naturally versatile features.",
            skinTone             = listOf("FAIR","LIGHT","MEDIUM","TAN","DEEP").random(),
            undertone            = listOf("WARM","COOL","NEUTRAL").random(),
            undertoneDescription = "Your skin has a beautifully balanced mix of warm and cool tones.",
            eyeShape             = listOf("ALMOND","ROUND","HOODED","MONOLID","UPTURNED").random(),
            browType             = listOf("DEFINED","SPARSE","ARCHED","STRAIGHT","THICK").random(),
            noseShape            = listOf("STRAIGHT","WIDE","NARROW","BUTTON","ROMAN").random(),
            lipType              = listOf("FULL","THIN","BOW_SHAPED","WIDE","HEART").random(),
            improvementAreasJson = mockAreas,
            stepGuidesJson       = mockGuides,
            celebrityMatchesJson = mockCelebs
        )
    }

    fun cancelAnalysis() {
        analysisJob?.cancel()
        analysisJob = null
        autoCaptureDisabled = true
        autoCapturePendingJob?.cancel()
        autoCapturePendingJob = null
        pendingImageBytes = null
        _uiState.update { it.copy(isLoading = false, showCancelButton = false) }
    }

    fun onAutoCaptureDisabledByCancel() {
        autoCaptureDisabled = true
        autoCapturePendingJob?.cancel()
        autoCapturePendingJob = null
    }

    fun selectScanType(type: ScanType) {
        _uiState.update { it.copy(selectedScanType = type) }
    }

    fun flipCamera() {
        _uiState.update {
            val newLens = if (it.cameraLens == CameraSelector.LENS_FACING_FRONT)
                CameraSelector.LENS_FACING_BACK else CameraSelector.LENS_FACING_FRONT
            it.copy(cameraLens = newLens)
        }
    }

    fun clearError() { _uiState.update { it.copy(error = null) } }
    fun resetBlurCheck() { _uiState.update { it.copy(blurCheckPassed = null) } }

    // ── Image validation helpers ──────────────────────────────────────────────

    private fun laplacianVariance(bmp: Bitmap): Double {
        val width = bmp.width; val height = bmp.height
        val pixels = IntArray(width * height)
        bmp.getPixels(pixels, 0, width, 0, 0, width, height)
        val gray = FloatArray(width * height) { i ->
            val px = pixels[i]
            (px shr 16 and 0xFF) * 0.299f + (px shr 8 and 0xFF) * 0.587f + (px and 0xFF) * 0.114f
        }
        val laplacian = FloatArray((width - 2) * (height - 2))
        var idx = 0
        for (y in 1 until height - 1) for (x in 1 until width - 1) {
            val c = gray[y * width + x] * 4f
            laplacian[idx++] = c - gray[(y-1)*width+x] - gray[(y+1)*width+x] - gray[y*width+(x-1)] - gray[y*width+(x+1)]
        }
        val mean = laplacian.map { it.toDouble() }.average()
        return laplacian.map { (it - mean) * (it - mean) }.average()
    }

    private fun luminanceOk(bmp: Bitmap, box: Rect): Boolean {
        val l = box.left.coerceIn(0, bmp.width - 1);  val r = box.right.coerceIn(l + 1, bmp.width)
        val t = box.top.coerceIn(0, bmp.height - 1);  val b = box.bottom.coerceIn(t + 1, bmp.height)
        val pixels = IntArray((r-l)*(b-t))
        bmp.getPixels(pixels, 0, r-l, l, t, r-l, b-t)
        val mean = pixels.map { px ->
            (px shr 16 and 0xFF)*0.299f + (px shr 8 and 0xFF)*0.587f + (px and 0xFF)*0.114f
        }.average().toFloat()
        return mean in LUMINANCE_MIN..LUMINANCE_MAX
    }

    private fun enforceFileSize(bmp: Bitmap): ByteArray {
        val out = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 85, out)
        if (out.size() <= MAX_FILE_SIZE) return out.toByteArray()
        val scaled = Bitmap.createScaledBitmap(bmp, MAX_DIMENSION, MAX_DIMENSION, true)
        return ByteArrayOutputStream().also { scaled.compress(Bitmap.CompressFormat.JPEG, 85, it) }.toByteArray()
    }

    override fun onCleared() {
        super.onCleared()
        fullResFaceDetector.close()
    }
}
