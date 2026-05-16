package com.appylab.lumi.data.model

enum class ScanType(val displayLabel: String) {
    FACE("Face"),
    COLOR("Color"),
    GLOW_UP("Glow-Up")
}

data class FaceDetectionStatus(
    val imageQuality: Boolean = false,
    val faceDetected: Boolean = false,
    val notBlurry: Boolean = false,
    val goodLighting: Boolean = false
)

data class ScanHint(val title: String, val subtitle: String)
