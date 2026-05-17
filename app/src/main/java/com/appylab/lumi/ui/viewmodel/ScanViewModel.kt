package com.appylab.lumi.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.appylab.lumi.data.db.AppStateEntity
import com.appylab.lumi.data.db.LumiDatabase
import com.appylab.lumi.data.model.FrameValidationState
import com.appylab.lumi.data.model.ScanError
import com.appylab.lumi.data.model.ScanType
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
import java.io.ByteArrayOutputStream
import java.time.LocalDate

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
private const val BLUR_THRESHOLD = 100.0
private const val LUMINANCE_MIN = 80f
private const val LUMINANCE_MAX = 220f
private const val MAX_FILE_SIZE = 512 * 1024
private const val MAX_DIMENSION = 512

class ScanViewModel(app: Application) : AndroidViewModel(app) {

    private val db = LumiDatabase.getInstance(app)
    private val appStateDao = db.appStateDao()

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    // Full-res detector for post-capture validation (ACCURATE, no landmarks needed)
    private val fullResFaceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .build()
    )

    // Internal flags not exposed in UiState
    private var autoCaptureDisabled = false
    private var autoCapturePendingJob: Job? = null
    private var analysisJob: Job? = null

    init {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val appState = appStateDao.getAppState() ?: AppStateEntity()

            val resolvedScanCount = if (appState.scanCountDate == today) {
                appState.scanCountToday
            } else {
                // Date mismatch → reset counter in DB
                val reset = appState.copy(scanCountToday = 0, scanCountDate = today)
                appStateDao.upsert(reset)
                0
            }

            _uiState.update { state ->
                state.copy(
                    isProUser = appState.subscriptionTier != "FREE",
                    scanCount = resolvedScanCount
                )
            }
        }
    }

    fun onFrameValidation(state: FrameValidationState) {
        _uiState.update { it.copy(frameValidation = state) }

        if (autoCaptureDisabled) return

        if (state.allPassed) {
            if (autoCapturePendingJob == null || autoCapturePendingJob?.isActive == false) {
                autoCapturePendingJob = viewModelScope.launch {
                    delay(1500)
                    // Re-check after debounce
                    if (_uiState.value.frameValidation.allPassed && !autoCaptureDisabled) {
                        onCapture()
                    }
                }
            }
        } else {
            autoCapturePendingJob?.cancel()
            autoCapturePendingJob = null
        }
    }

    /** Called from auto-capture debounce or manual capture button tap (before image is taken). */
    fun onCapture() {
        if (_uiState.value.isLoading) return

        val currentCount = _uiState.value.scanCount
        if (currentCount >= DAILY_SCAN_LIMIT) {
            _uiState.update { it.copy(error = ScanError.DailyLimitReached) }
            return
        }

        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val appState = appStateDao.getAppState() ?: AppStateEntity()
            val newCount = currentCount + 1
            appStateDao.upsert(
                appState.copy(
                    scanCountToday = newCount,
                    scanCountDate = today
                )
            )
            _uiState.update { it.copy(scanCount = newCount) }
            startAnalysis()
        }
    }

    /**
     * Called after CameraX delivers JPEG bytes. Runs post-capture validation:
     * 1. Decode bitmap
     * 2. ML Kit full-res face check (exactly 1 face)
     * 3. Laplacian variance ≥ 100 (blur check)
     * 4. Mean luminance in face bounding box 80–220
     * 5. Enforce file size (JPEG 85%, resize to 512×512 if > 512KB)
     * Then increments daily counter and starts analysis.
     */
    fun onImageCaptured(jpeg: ByteArray) {
        viewModelScope.launch(Dispatchers.Default) {
            val bitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size)
            if (bitmap == null) {
                _uiState.update { it.copy(error = ScanError.ApiError(0, "Could not decode image")) }
                return@launch
            }

            // Check 1: ML Kit face detection on full-res bitmap
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val faces = try {
                Tasks.await(fullResFaceDetector.process(inputImage))
            } catch (e: Exception) {
                _uiState.update { it.copy(error = ScanError.ApiError(0, "Face detection failed")) }
                return@launch
            }
            when {
                faces.isEmpty() -> {
                    _uiState.update { it.copy(error = ScanError.NoFace) }
                    return@launch
                }
                faces.size > 1 -> {
                    _uiState.update { it.copy(error = ScanError.MultipleFaces) }
                    return@launch
                }
            }

            // Check 2: Laplacian variance blur check
            if (laplacianVariance(bitmap) < BLUR_THRESHOLD) {
                _uiState.update { it.copy(error = ScanError.Blurry) }
                return@launch
            }

            // Check 3: Luminance check on face bounding box
            val faceBox = faces[0].boundingBox
            if (!luminanceOk(bitmap, faceBox)) {
                _uiState.update { it.copy(error = ScanError.PoorLighting) }
                return@launch
            }

            // Check 4: File size enforcement
            enforceFileSize(bitmap)

            // All checks passed — increment counter and start analysis
            withContext(Dispatchers.Main) {
                onCaptureValidated()
            }
        }
    }

    /** Called after gallery URI selected. Loads bitmap, compresses to JPEG, runs same validation pipeline. */
    fun onGalleryImageSelected(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val bitmap = getApplication<Application>().contentResolver
                .openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
            if (bitmap == null) {
                _uiState.update { it.copy(error = ScanError.ApiError(0, "Could not load image")) }
                return@launch
            }
            val jpeg = ByteArrayOutputStream().also {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it)
            }.toByteArray()
            // Switch to Default for CPU-bound validation
            withContext(Dispatchers.Default) {
                // Re-enter via onImageCaptured logic inline to avoid dispatcher hop overhead
                val decoded = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size) ?: run {
                    _uiState.update { it.copy(error = ScanError.ApiError(0, "Could not decode image")) }
                    return@withContext
                }
                val inputImage = InputImage.fromBitmap(decoded, 0)
                val faces = try {
                    Tasks.await(fullResFaceDetector.process(inputImage))
                } catch (e: Exception) {
                    _uiState.update { it.copy(error = ScanError.ApiError(0, "Face detection failed")) }
                    return@withContext
                }
                when {
                    faces.isEmpty() -> { _uiState.update { it.copy(error = ScanError.NoFace) }; return@withContext }
                    faces.size > 1 -> { _uiState.update { it.copy(error = ScanError.MultipleFaces) }; return@withContext }
                }
                if (laplacianVariance(decoded) < BLUR_THRESHOLD) {
                    _uiState.update { it.copy(error = ScanError.Blurry) }; return@withContext
                }
                if (!luminanceOk(decoded, faces[0].boundingBox)) {
                    _uiState.update { it.copy(error = ScanError.PoorLighting) }; return@withContext
                }
                enforceFileSize(decoded)
                withContext(Dispatchers.Main) { onCaptureValidated() }
            }
        }
    }

    /** Called when CameraX ImageCapture fails. */
    fun onCaptureError() {
        _uiState.update { it.copy(error = ScanError.ApiError(-1, "Capture failed — try again.")) }
    }

    /** Increments daily counter and starts analysis after validation passes. */
    private suspend fun onCaptureValidated() {
        if (_uiState.value.isLoading) return

        val currentCount = _uiState.value.scanCount
        if (currentCount >= DAILY_SCAN_LIMIT) {
            _uiState.update { it.copy(error = ScanError.DailyLimitReached) }
            return
        }

        val today = LocalDate.now().toString()
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
                it.copy(
                    isLoading = true,
                    loadingStage = LOADING_STAGES[0],
                    showCancelButton = false,
                    blurCheckPassed = null
                )
            }

            var stageIndex = 0
            var elapsed = 0L
            val timeout = 15_000L
            val stageInterval = 2_000L
            val cancelButtonDelay = 5_000L

            // Simulate analysis: cycle stages every 2s, success at ~3s
            val simulatedSuccessDelay = 3_000L

            try {
                while (elapsed < timeout) {
                    val sleepFor = minOf(stageInterval, timeout - elapsed)
                    delay(sleepFor)
                    elapsed += sleepFor

                    if (elapsed >= cancelButtonDelay && !_uiState.value.showCancelButton) {
                        _uiState.update { it.copy(showCancelButton = true) }
                    }

                    stageIndex = ((elapsed / stageInterval).toInt()).coerceAtMost(LOADING_STAGES.size - 1)
                    _uiState.update { it.copy(loadingStage = LOADING_STAGES[stageIndex]) }

                    if (elapsed >= simulatedSuccessDelay) {
                        onAnalysisSuccess()
                        return@launch
                    }
                }

                // Timeout reached
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        showCancelButton = false,
                        error = ScanError.Timeout
                    )
                }
            } catch (_: kotlinx.coroutines.CancellationException) {
                // Cancelled via cancelAnalysis() — state already updated there
            }
        }
    }

    private fun onAnalysisSuccess() {
        _uiState.update {
            it.copy(
                isLoading = false,
                showCancelButton = false,
                blurCheckPassed = true
            )
        }
    }

    fun cancelAnalysis() {
        analysisJob?.cancel()
        analysisJob = null
        autoCaptureDisabled = true
        autoCapturePendingJob?.cancel()
        autoCapturePendingJob = null
        _uiState.update {
            it.copy(
                isLoading = false,
                showCancelButton = false
            )
        }
    }

    fun onAutoCaptureDisabledByCancel() {
        autoCaptureDisabled = true
        autoCapturePendingJob?.cancel()
        autoCapturePendingJob = null
    }

    fun selectScanType(type: ScanType) {
        // Always update selectedScanType; UI shows lock icon for non-pro users
        _uiState.update { it.copy(selectedScanType = type) }
    }

    fun flipCamera() {
        _uiState.update {
            val newLens = if (it.cameraLens == CameraSelector.LENS_FACING_FRONT) {
                CameraSelector.LENS_FACING_BACK
            } else {
                CameraSelector.LENS_FACING_FRONT
            }
            it.copy(cameraLens = newLens)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // ── Image validation helpers ─────────────────────────────────────────────

    /**
     * Computes the variance of a Laplacian-filtered grayscale version of [bmp].
     * Higher variance = sharper image. Threshold ≥ 100 considered acceptable.
     */
    private fun laplacianVariance(bmp: Bitmap): Double {
        val width = bmp.width
        val height = bmp.height
        val pixels = IntArray(width * height)
        bmp.getPixels(pixels, 0, width, 0, 0, width, height)

        // Convert to grayscale luminance values
        val gray = FloatArray(width * height) { i ->
            val px = pixels[i]
            val r = (px shr 16 and 0xFF)
            val g = (px shr 8 and 0xFF)
            val b = (px and 0xFF)
            r * 0.299f + g * 0.587f + b * 0.114f
        }

        // Apply 3×3 Laplacian kernel: [0,-1,0,-1,4,-1,0,-1,0]
        val laplacian = FloatArray((width - 2) * (height - 2))
        var idx = 0
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val center = gray[y * width + x] * 4f
                val top    = gray[(y - 1) * width + x]
                val bottom = gray[(y + 1) * width + x]
                val left   = gray[y * width + (x - 1)]
                val right  = gray[y * width + (x + 1)]
                laplacian[idx++] = center - top - bottom - left - right
            }
        }

        val mean = laplacian.map { it.toDouble() }.average()
        return laplacian.map { (it - mean) * (it - mean) }.average()
    }

    /**
     * Checks mean luminance in the face bounding box is within [80, 220].
     * Clamps [box] to bitmap bounds before sampling.
     */
    private fun luminanceOk(bmp: Bitmap, box: Rect): Boolean {
        val left   = box.left.coerceIn(0, bmp.width - 1)
        val top    = box.top.coerceIn(0, bmp.height - 1)
        val right  = box.right.coerceIn(left + 1, bmp.width)
        val bottom = box.bottom.coerceIn(top + 1, bmp.height)

        val w = right - left
        val h = bottom - top
        val pixels = IntArray(w * h)
        bmp.getPixels(pixels, 0, w, left, top, w, h)

        val mean = pixels.map { px ->
            val r = (px shr 16 and 0xFF)
            val g = (px shr 8 and 0xFF)
            val b = (px and 0xFF)
            r * 0.299f + g * 0.587f + b * 0.114f
        }.average().toFloat()

        return mean in LUMINANCE_MIN..LUMINANCE_MAX
    }

    /**
     * Compresses [bmp] to JPEG at 85% quality.
     * If the result exceeds 512KB, scales to 512×512 first and re-compresses.
     */
    private fun enforceFileSize(bmp: Bitmap): ByteArray {
        val out = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 85, out)
        if (out.size() <= MAX_FILE_SIZE) return out.toByteArray()

        val scaled = Bitmap.createScaledBitmap(bmp, MAX_DIMENSION, MAX_DIMENSION, true)
        val out2 = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, out2)
        return out2.toByteArray()
    }

    override fun onCleared() {
        super.onCleared()
        fullResFaceDetector.close()
    }
}
