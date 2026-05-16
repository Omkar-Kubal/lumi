package com.appylab.lumi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appylab.lumi.data.model.FaceDetectionStatus
import com.appylab.lumi.data.model.ScanHint
import com.appylab.lumi.data.model.ScanType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ScanUiState(
    val scanType: ScanType = ScanType.FACE,
    val isFrontCamera: Boolean = true,
    val detectionStatus: FaceDetectionStatus = FaceDetectionStatus(),
    val isCapturing: Boolean = false,
    val captureComplete: Boolean = false
) {
    val isWellPositioned: Boolean
        get() = detectionStatus.run { imageQuality && faceDetected && notBlurry && goodLighting }

    val statusMessage: String
        get() = when {
            isWellPositioned -> "Great! Your face is well positioned"
            detectionStatus.faceDetected -> "Almost there — adjust your position"
            else -> "Position your face in the oval"
        }

    val lightingHint: ScanHint?
        get() = when {
            detectionStatus.goodLighting -> ScanHint("Good lighting", "Keep it up!")
            detectionStatus.faceDetected -> ScanHint("Poor lighting", "Find better light")
            else -> null
        }

    val positionHints: List<ScanHint>
        get() {
            val hints = mutableListOf<ScanHint>()
            if (detectionStatus.faceDetected) {
                hints += ScanHint("Move closer", "Ideal distance")
            } else {
                hints += ScanHint("Move closer", "Ideal distance")
            }
            hints += ScanHint("Center your face", "Keep your face in the oval")
            return hints
        }
}

class ScanViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    fun selectScanType(type: ScanType) {
        _uiState.update { it.copy(scanType = type) }
    }

    fun flipCamera() {
        _uiState.update { it.copy(isFrontCamera = !it.isFrontCamera) }
    }

    fun capture() {
        if (!_uiState.value.isWellPositioned) return
        _uiState.update { it.copy(isCapturing = true) }
        // Capture logic will be implemented when CameraX image capture is wired
    }

    fun onCaptureComplete() {
        _uiState.update { it.copy(isCapturing = false, captureComplete = true) }
    }

    /**
     * Called by the camera analysis pipeline with real-time face detection results.
     * In MVP this is driven by simulated data from the UI layer.
     */
    fun onFaceDetectionUpdate(status: FaceDetectionStatus) {
        _uiState.update { it.copy(detectionStatus = status) }
    }

    fun resetCapture() {
        _uiState.update { it.copy(isCapturing = false, captureComplete = false) }
    }
}
