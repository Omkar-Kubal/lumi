package com.appylab.lumi.data.model

enum class ScanType(val displayLabel: String) {
    FULL_ANALYSIS("Face"),
    COLOR_ONLY("Color"),
    GLOWUP_ONLY("Glow-Up")
}

enum class DistanceStatus { TOO_CLOSE, TOO_FAR, OK }
enum class CentreStatus { OFF_CENTRE, OK }
enum class LightingStatus { TOO_DARK, TOO_BRIGHT, OK }

data class FrameValidationState(
    val faceDetected: Boolean = false,
    val multipleFaces: Boolean = false,
    val distanceStatus: DistanceStatus = DistanceStatus.TOO_FAR,
    val centreStatus: CentreStatus = CentreStatus.OFF_CENTRE,
    val lightingStatus: LightingStatus = LightingStatus.TOO_DARK
) {
    val allPassed: Boolean
        get() = faceDetected && !multipleFaces
            && distanceStatus == DistanceStatus.OK
            && centreStatus == CentreStatus.OK
            && lightingStatus == LightingStatus.OK
}

sealed class ScanError {
    object NoFace : ScanError()
    object MultipleFaces : ScanError()
    object Blurry : ScanError()
    object PoorLighting : ScanError()
    object DailyLimitReached : ScanError()
    object Timeout : ScanError()
    object ParseFailure : ScanError()
    data class ApiError(val code: Int, val message: String) : ScanError()
}
