package com.appylab.lumi.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Cameraswitch
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appylab.lumi.data.model.CentreStatus
import com.appylab.lumi.data.model.DistanceStatus
import com.appylab.lumi.data.model.FrameValidationState
import com.appylab.lumi.data.model.LightingStatus
import com.appylab.lumi.data.model.ScanError
import com.appylab.lumi.data.model.ScanType
import com.appylab.lumi.ui.viewmodel.ScanViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.math.abs

// ── Palette ───────────────────────────────────────────────────────────────────
private val SRose    = Color(0xFFFF637E)
private val SWhite   = Color.White
private val SWhite70 = Color.White.copy(alpha = 0.70f)
private val SWhite40 = Color.White.copy(alpha = 0.40f)
private val SDark    = Color(0xFF0A0A0A)
private val SChipBg  = Color(0xFF1C1C1C).copy(alpha = 0.85f)
private val SAmber   = Color(0xFFFBBF24)

// ── Static facial landmark dot positions (normalised within oval 0..1) ────────
private val faceLandmarkDots = listOf(
    0.50f to 0.10f, 0.36f to 0.14f, 0.64f to 0.14f,
    0.26f to 0.22f, 0.74f to 0.22f,
    0.16f to 0.33f, 0.84f to 0.33f,
    0.28f to 0.32f, 0.35f to 0.29f, 0.42f to 0.31f, 0.35f to 0.36f,
    0.58f to 0.31f, 0.65f to 0.29f, 0.72f to 0.32f, 0.65f to 0.36f,
    0.50f to 0.38f, 0.50f to 0.45f, 0.50f to 0.52f,
    0.43f to 0.50f, 0.57f to 0.50f,
    0.22f to 0.46f, 0.78f to 0.46f,
    0.18f to 0.54f, 0.82f to 0.54f,
    0.43f to 0.60f, 0.50f to 0.58f, 0.57f to 0.60f,
    0.44f to 0.66f, 0.50f to 0.68f, 0.56f to 0.66f,
    0.50f to 0.76f, 0.42f to 0.73f, 0.58f to 0.73f,
    0.30f to 0.65f, 0.70f to 0.65f,
    0.22f to 0.60f, 0.78f to 0.60f,
)

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun ScanScreen(
    viewModel: ScanViewModel = viewModel(),
    onBack: () -> Unit = {},
    onScanComplete: () -> Unit = {}
) {
    val context = LocalContext.current

    var cameraPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> cameraPermissionGranted = granted }

    LaunchedEffect(Unit) {
        if (!cameraPermissionGranted) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    if (cameraPermissionGranted) {
        ScanContent(viewModel, onBack, onScanComplete)
    } else {
        CameraPermissionDeniedScreen(
            onBack = onBack,
            onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) }
        )
    }
}

// ── Main scan content ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScanContent(
    viewModel: ScanViewModel,
    onBack: () -> Unit,
    onScanComplete: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val frameValidation = uiState.frameValidation

    var imageCaptureRef by remember { mutableStateOf<ImageCapture?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { viewModel.onGalleryImageSelected(it) } }

    // Reset stale blurCheckPassed from a previous scan so re-entering ScanScreen
    // doesn't immediately redirect back to Results.
    LaunchedEffect(Unit) {
        viewModel.resetBlurCheck()
    }

    // Navigate to results when analysis succeeds
    LaunchedEffect(uiState.blurCheckPassed) {
        if (uiState.blurCheckPassed == true) onScanComplete()
    }

    // Error dialog for daily limit
    if (uiState.error == ScanError.DailyLimitReached) {
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            title = { Text("Daily limit reached") },
            text = {
                Text(
                    if (uiState.isProUser)
                        "You've used all 3 scans for today. Come back tomorrow."
                    else
                        "You've used all 3 scans for today. Come back tomorrow or upgrade to Pro."
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::clearError) { Text("OK") }
            }
        )
    }

    // Capture validation error bottom sheet (NoFace, MultipleFaces, Blurry, PoorLighting)
    val captureErrorMsg = when (uiState.error) {
        ScanError.NoFace        -> "No face found — position your face in the oval and try again."
        ScanError.MultipleFaces -> "Multiple faces detected — use a solo selfie."
        ScanError.Blurry        -> "Photo is too blurry — hold still and try again."
        ScanError.PoorLighting  -> "Lighting is too dark or bright — find better-lit surroundings."
        else -> null
    }
    if (captureErrorMsg != null) {
        ModalBottomSheet(
            onDismissRequest = viewModel::clearError,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    captureErrorMsg,
                    style = TextStyle(fontSize = 15.sp, color = SDark, textAlign = TextAlign.Center)
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = viewModel::clearError,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SRose),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Try again", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SWhite))
                }
                Spacer(Modifier.navigationBarsPadding())
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    // Timeout / API error snackbar-style banner handled inline below

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SDark)
    ) {
        // Camera preview + ML Kit analysis
        CameraPreviewWithAnalysis(
            cameraLens = uiState.cameraLens,
            onFrameValidation = viewModel::onFrameValidation,
            onImageCaptureReady = { imageCaptureRef = it },
            modifier = Modifier.fillMaxSize()
        )

        // Top gradient scrim
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Brush.verticalGradient(listOf(Color.Black.copy(0.75f), Color.Transparent)))
                .align(Alignment.TopCenter)
        )

        // Bottom gradient scrim
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.92f))))
                .align(Alignment.BottomCenter)
        )

        // Main column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            ScanHeader(onBack = onBack, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))

            // Camera area — oval overlay + chips
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                FaceOverlayCanvas(
                    faceDetected = frameValidation.faceDetected,
                    modifier = Modifier.fillMaxSize()
                )

                // Lighting chip — always visible
                LightingChip(
                    status = frameValidation.lightingStatus,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 16.dp, top = 8.dp)
                )

                // Distance chip — hidden when OK (Column gives ColumnScope for AnimatedVisibility)
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 16.dp, top = 8.dp)
                ) {
                    AnimatedVisibility(
                        visible = frameValidation.faceDetected && frameValidation.distanceStatus != DistanceStatus.OK,
                        enter = fadeIn(tween(150)),
                        exit = fadeOut(tween(150))
                    ) {
                        StatusChip(
                            icon = Icons.Outlined.Face,
                            title = if (frameValidation.distanceStatus == DistanceStatus.TOO_FAR) "Move closer" else "Move back",
                            subtitle = "Ideal distance"
                        )
                    }
                }

                // Centre chip — hidden when OK
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                ) {
                    AnimatedVisibility(
                        visible = frameValidation.faceDetected && frameValidation.centreStatus == CentreStatus.OFF_CENTRE,
                        enter = fadeIn(tween(150)),
                        exit = fadeOut(tween(150))
                    ) {
                        StatusChip(
                            icon = Icons.Outlined.Face,
                            title = "Center your face",
                            subtitle = "Keep your face in the oval"
                        )
                    }
                }
            }

            // Bottom panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Positioned status bar — only when all checks pass
                AnimatedVisibility(
                    visible = frameValidation.allPassed,
                    enter = slideInVertically { it / 2 } + fadeIn(tween(200)),
                    exit = fadeOut(tween(150))
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = SWhite
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = SRose,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Great! Your face is well positioned",
                                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SDark)
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = frameValidation.allPassed) {
                    Spacer(Modifier.height(10.dp))
                }

                // Image quality checklist
                ImageQualityChecklist(
                    faceDetected = frameValidation.faceDetected && !frameValidation.multipleFaces,
                    lightingOk = frameValidation.lightingStatus == LightingStatus.OK
                )

                Spacer(Modifier.height(14.dp))

                // Error banner for timeout / API errors
                val errorMsg = when (uiState.error) {
                    ScanError.Timeout -> "Analysis timed out — tap capture to try again."
                    ScanError.ParseFailure -> "Analysis failed — tap capture to try again."
                    is ScanError.ApiError -> "Analysis failed — tap capture to try again."
                    else -> null
                }
                if (errorMsg != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = SRose.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(errorMsg, style = TextStyle(fontSize = 11.sp, color = SRose), modifier = Modifier.weight(1f))
                            TextButton(onClick = viewModel::clearError) {
                                Text("Dismiss", style = TextStyle(fontSize = 11.sp, color = SRose))
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }

                // Camera controls
                CameraControlsRow(
                    allPassed = frameValidation.allPassed,
                    onCapture = viewModel::onCapture,
                    onGalleryClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    imageCapture = imageCaptureRef,
                    captureExecutor = ContextCompat.getMainExecutor(context),
                    onImageCaptured = viewModel::onImageCaptured,
                    onCaptureError = viewModel::onCaptureError,
                    onFlipClick = viewModel::flipCamera
                )

                Spacer(Modifier.height(14.dp))

                // Scan type selector
                ScanTypeSelector(
                    selectedType = uiState.selectedScanType,
                    isProUser = uiState.isProUser,
                    onTypeSelected = viewModel::selectScanType
                )

                Spacer(Modifier.height(10.dp))

                // Privacy footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Lock, contentDescription = null, tint = SWhite40, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Your images are private and secure", style = TextStyle(fontSize = 11.sp, color = SWhite40))
                }

                Spacer(Modifier.navigationBarsPadding())
                Spacer(Modifier.height(8.dp))
            }
        }

        // Loading overlay
        if (uiState.isLoading) {
            LoadingOverlay(
                stage = uiState.loadingStage,
                scanCount = uiState.scanCount,
                showCancel = uiState.showCancelButton,
                onCancel = viewModel::cancelAnalysis
            )
        }
    }
}

// ── CameraX preview + ML Kit ImageAnalysis ────────────────────────────────────

@androidx.camera.core.ExperimentalGetImage
private class FaceFrameAnalyzer(
    private val onValidation: (FrameValidationState) -> Unit
) : ImageAnalysis.Analyzer {

    private val faceDetector = run {
        val opts = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setMinFaceSize(0.15f)
            .build()
        FaceDetection.getClient(opts)
    }

    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run { imageProxy.close(); return }

        // Copy Y-channel data synchronously before async ML Kit call
        val yPlane = imageProxy.planes[0]
        val yBuffer = yPlane.buffer
        val yBytes = ByteArray(yBuffer.remaining())
        yBuffer.get(yBytes)
        val rowStride = yPlane.rowStride
        val frameW = imageProxy.width
        val frameH = imageProxy.height

        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        faceDetector.process(inputImage)
            .addOnSuccessListener { faces ->
                val state = when {
                    faces.isEmpty() -> FrameValidationState()
                    faces.size > 1 -> FrameValidationState(faceDetected = true, multipleFaces = true)
                    else -> {
                        val face = faces[0]
                        val box = face.boundingBox

                        // Check 2: distance
                        val faceWidthRatio = box.width().toFloat() / frameW
                        val distanceStatus = when {
                            faceWidthRatio < 0.30f -> DistanceStatus.TOO_FAR
                            faceWidthRatio > 0.75f -> DistanceStatus.TOO_CLOSE
                            else -> DistanceStatus.OK
                        }

                        // Check 3: centre
                        val offsetX = abs(box.exactCenterX() - frameW / 2f) / frameW
                        val offsetY = abs(box.exactCenterY() - frameH / 2f) / frameH
                        val centreStatus = if (offsetX > 0.15f || offsetY > 0.15f)
                            CentreStatus.OFF_CENTRE else CentreStatus.OK

                        // Check 4: lighting — mean Y in face region
                        val left = box.left.coerceIn(0, frameW - 1)
                        val top = box.top.coerceIn(0, frameH - 1)
                        val right = box.right.coerceIn(0, frameW)
                        val bottom = box.bottom.coerceIn(0, frameH)
                        var sum = 0L
                        var count = 0
                        for (row in top until bottom) {
                            for (col in left until right) {
                                val idx = row * rowStride + col
                                if (idx < yBytes.size) {
                                    sum += (yBytes[idx].toInt() and 0xFF)
                                    count++
                                }
                            }
                        }
                        val meanLum = if (count > 0) sum.toFloat() / count else 0f
                        val lightingStatus = when {
                            meanLum < 80f -> LightingStatus.TOO_DARK
                            meanLum > 220f -> LightingStatus.TOO_BRIGHT
                            else -> LightingStatus.OK
                        }

                        FrameValidationState(
                            faceDetected = true,
                            multipleFaces = false,
                            distanceStatus = distanceStatus,
                            centreStatus = centreStatus,
                            lightingStatus = lightingStatus
                        )
                    }
                }
                onValidation(state)
            }
            .addOnCompleteListener { imageProxy.close() }
    }
}

@Suppress("OPT_IN_USAGE")
@Composable
private fun CameraPreviewWithAnalysis(
    cameraLens: Int,
    onFrameValidation: (FrameValidationState) -> Unit,
    onImageCaptureReady: (ImageCapture) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val analyzer = remember { FaceFrameAnalyzer(onFrameValidation) }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

    AndroidView(factory = { previewView }, modifier = modifier)

    LaunchedEffect(cameraLens) {
        val cameraProvider = withContext(Dispatchers.IO) {
            ProcessCameraProvider.getInstance(context).get()
        }
        val selector = if (cameraLens == CameraSelector.LENS_FACING_FRONT)
            CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(480, 480))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { it.setAnalyzer(analysisExecutor, analyzer) }

        val imageCapture = ImageCapture.Builder()
            .setJpegQuality(85)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, imageAnalysis, imageCapture)
            onImageCaptureReady(imageCapture)
        } catch (_: Exception) { }
    }

    DisposableEffect(Unit) {
        onDispose {
            analysisExecutor.shutdown()
            ProcessCameraProvider.getInstance(context).addListener({
                ProcessCameraProvider.getInstance(context).get().unbindAll()
            }, ContextCompat.getMainExecutor(context))
        }
    }
}

// ── Face oval + landmark dots ─────────────────────────────────────────────────

@Composable
private fun FaceOverlayCanvas(
    faceDetected: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dot_pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )

    androidx.compose.foundation.Canvas(modifier = modifier) {
        val ovalW = size.width * 0.60f
        val ovalH = size.height * 0.56f
        val ovalLeft = (size.width - ovalW) / 2f
        val ovalTop = (size.height - ovalH) / 2f - size.height * 0.03f

        val ovalRect = androidx.compose.ui.geometry.Rect(
            left = ovalLeft, top = ovalTop,
            right = ovalLeft + ovalW, bottom = ovalTop + ovalH
        )

        val bracketLen = 22.dp.toPx()
        val bracketW = 2.5.dp.toPx()
        val ovalColor = SWhite.copy(alpha = 0.90f)

        // Corner brackets
        drawLine(ovalColor, Offset(ovalRect.left, ovalRect.top + bracketLen), Offset(ovalRect.left, ovalRect.top), bracketW)
        drawLine(ovalColor, Offset(ovalRect.left, ovalRect.top), Offset(ovalRect.left + bracketLen, ovalRect.top), bracketW)
        drawLine(ovalColor, Offset(ovalRect.right - bracketLen, ovalRect.top), Offset(ovalRect.right, ovalRect.top), bracketW)
        drawLine(ovalColor, Offset(ovalRect.right, ovalRect.top), Offset(ovalRect.right, ovalRect.top + bracketLen), bracketW)
        drawLine(ovalColor, Offset(ovalRect.left, ovalRect.bottom - bracketLen), Offset(ovalRect.left, ovalRect.bottom), bracketW)
        drawLine(ovalColor, Offset(ovalRect.left, ovalRect.bottom), Offset(ovalRect.left + bracketLen, ovalRect.bottom), bracketW)
        drawLine(ovalColor, Offset(ovalRect.right - bracketLen, ovalRect.bottom), Offset(ovalRect.right, ovalRect.bottom), bracketW)
        drawLine(ovalColor, Offset(ovalRect.right, ovalRect.bottom - bracketLen), Offset(ovalRect.right, ovalRect.bottom), bracketW)

        // Dashed oval outline
        drawPath(
            path = Path().apply { addOval(ovalRect) },
            color = ovalColor.copy(alpha = 0.80f),
            style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 7f)))
        )

        // Landmark dots — shown only when face is detected
        if (faceDetected) {
            faceLandmarkDots.forEach { (nx, ny) ->
                val dx = (nx - 0.5f) * 2f
                val dy = (ny - 0.5f) * 2f
                if (dx * dx + dy * dy <= 1.05f) {
                    val isKeyFeature = (nx == 0.50f && ny == 0.52f) // nose tip
                        || (nx == 0.43f && ny == 0.60f) || (nx == 0.57f && ny == 0.60f) // mouth corners
                    drawCircle(
                        color = (if (isKeyFeature) SRose else SWhite).copy(alpha = dotAlpha),
                        radius = 2.8.dp.toPx(),
                        center = Offset(ovalLeft + nx * ovalW, ovalTop + ny * ovalH)
                    )
                }
            }
        }
    }
}

// ── Scan header ───────────────────────────────────────────────────────────────

@Composable
private fun ScanHeader(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Close", tint = SWhite, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Scan your face",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = SWhite, textAlign = TextAlign.Center)
            )
            Text(
                "We'll analyse and personalise for you",
                style = TextStyle(fontSize = 11.sp, color = SWhite70, textAlign = TextAlign.Center)
            )
        }
        IconButton(onClick = {}) {
            Icon(Icons.Outlined.HelpOutline, "Help", tint = SWhite, modifier = Modifier.size(22.dp))
        }
    }
}

// ── Status chips ──────────────────────────────────────────────────────────────

@Composable
private fun LightingChip(status: LightingStatus, modifier: Modifier = Modifier) {
    val (title, subtitle) = when (status) {
        LightingStatus.OK -> "Good lighting" to "Keep it up!"
        LightingStatus.TOO_DARK -> "Low lighting" to "Find better lighting"
        LightingStatus.TOO_BRIGHT -> "Too bright" to "Reduce light behind you"
    }
    StatusChip(
        icon = Icons.Outlined.WbSunny,
        iconTint = SAmber,
        title = title,
        subtitle = subtitle,
        modifier = modifier
    )
}

@Composable
private fun StatusChip(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    iconTint: Color = SWhite70
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SChipBg)
            .border(0.5.dp, SWhite.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(14.dp))
        Column {
            Text(title, style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SWhite))
            Text(subtitle, style = TextStyle(fontSize = 9.sp, color = SWhite70))
        }
    }
}

// ── Image quality checklist ───────────────────────────────────────────────────

@Composable
private fun ImageQualityChecklist(
    faceDetected: Boolean,
    lightingOk: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = SWhite.copy(alpha = 0.95f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "Image quality",
                style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SDark.copy(alpha = 0.6f))
            )
            Spacer(Modifier.height(8.dp))
            ChecklistRow("Face detected", passed = faceDetected)
            Spacer(Modifier.height(6.dp))
            ChecklistRow("Good lighting", passed = lightingOk)
        }
    }
}

@Composable
private fun ChecklistRow(label: String, passed: Boolean?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = TextStyle(fontSize = 12.sp, color = SDark))
        when (passed) {
            true -> Icon(Icons.Outlined.CheckCircle, null, tint = SRose, modifier = Modifier.size(16.dp))
            false -> Icon(Icons.Outlined.RadioButtonUnchecked, null, tint = Color(0xFFD1D5DB), modifier = Modifier.size(16.dp))
            null -> Icon(Icons.Outlined.RadioButtonUnchecked, null, tint = Color(0xFFD1D5DB), modifier = Modifier.size(16.dp))
        }
    }
}

// ── Camera controls ───────────────────────────────────────────────────────────

@Composable
private fun CameraControlsRow(
    allPassed: Boolean,
    onCapture: () -> Unit,
    onGalleryClick: () -> Unit,
    imageCapture: ImageCapture?,
    captureExecutor: Executor,
    onImageCaptured: (ByteArray) -> Unit,
    onCaptureError: () -> Unit,
    onFlipClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val shakeOffset = remember { Animatable(0f) }

    // Pulse ring scale when allPassed
    val infiniteTransition = rememberInfiniteTransition(label = "capture_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Gallery
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(
                onClick = onGalleryClick,
                modifier = Modifier.size(48.dp).clip(CircleShape).background(SWhite.copy(0.12f))
            ) {
                Icon(Icons.Outlined.Image, "Gallery", tint = SWhite, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(4.dp))
            Text("Gallery", style = TextStyle(fontSize = 10.sp, color = SWhite70))
        }

        // Capture button
        Box(
            modifier = Modifier
                .offset(x = shakeOffset.value.dp)
                .then(if (allPassed) Modifier.scale(pulseScale) else Modifier)
                .size(72.dp)
                .clip(CircleShape)
                .background(if (allPassed) SWhite else SWhite.copy(0.45f))
                .clickable {
                    // Always start the analysis (mock path works regardless of frame state).
                    // When the frame is valid and a real camera is available, also capture a
                    // photo so onImageCaptured can run additional validation; the isLoading
                    // guard in onCaptureValidated prevents a double-start.
                    onCapture()
                    if (allPassed && imageCapture != null) {
                        imageCapture.takePicture(
                            captureExecutor,
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(proxy: ImageProxy) {
                                    val buf = proxy.planes[0].buffer
                                    val bytes = ByteArray(buf.remaining()).also { buf.get(it) }
                                    proxy.close()
                                    onImageCaptured(bytes)
                                }
                                override fun onError(e: ImageCaptureException) {
                                    onCaptureError()
                                }
                            }
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .border(2.dp, SDark.copy(0.15f), CircleShape)
            )
        }

        // Flip camera
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(
                onClick = onFlipClick,
                modifier = Modifier.size(48.dp).clip(CircleShape).background(SWhite.copy(0.12f))
            ) {
                Icon(Icons.Outlined.Cameraswitch, "Flip camera", tint = SWhite, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(4.dp))
            Text("Flip camera", style = TextStyle(fontSize = 10.sp, color = SWhite70))
        }
    }
}

// ── Scan type selector ────────────────────────────────────────────────────────

@Composable
private fun ScanTypeSelector(
    selectedType: ScanType,
    isProUser: Boolean,
    onTypeSelected: (ScanType) -> Unit
) {
    Column {
        Text("Scan type", style = TextStyle(fontSize = 11.sp, color = SWhite70), modifier = Modifier.padding(start = 2.dp, bottom = 6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SWhite.copy(0.10f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ScanTypeTab(
                type = ScanType.FULL_ANALYSIS,
                icon = Icons.Outlined.Face,
                selected = selectedType == ScanType.FULL_ANALYSIS,
                locked = false,
                onClick = { onTypeSelected(ScanType.FULL_ANALYSIS) },
                modifier = Modifier.weight(1f)
            )
            ScanTypeTab(
                type = ScanType.COLOR_ONLY,
                icon = Icons.Outlined.Palette,
                selected = selectedType == ScanType.COLOR_ONLY,
                locked = !isProUser,
                onClick = { onTypeSelected(ScanType.COLOR_ONLY) },
                modifier = Modifier.weight(1f)
            )
            ScanTypeTab(
                type = ScanType.GLOWUP_ONLY,
                icon = Icons.Outlined.AutoAwesome,
                selected = selectedType == ScanType.GLOWUP_ONLY,
                locked = !isProUser,
                onClick = { onTypeSelected(ScanType.GLOWUP_ONLY) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ScanTypeTab(
    type: ScanType,
    icon: ImageVector,
    selected: Boolean,
    locked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(if (selected) SRose.copy(0.20f) else Color.Transparent)
                .border(
                    width = if (selected) 1.dp else 0.dp,
                    color = if (selected) SRose.copy(0.50f) else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon, null,
                tint = if (selected) SRose else SWhite70,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                type.displayLabel,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selected) SRose else SWhite70
                )
            )
        }

        // Lock badge for PRO-gated tabs
        if (locked) {
            Icon(
                Icons.Outlined.Lock,
                contentDescription = "Pro feature",
                tint = SWhite70,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 2.dp, end = 2.dp)
                    .size(9.dp)
            )
        }
    }
}

// ── Loading overlay ───────────────────────────────────────────────────────────

@Composable
private fun LoadingOverlay(
    stage: String,
    scanCount: Int,
    showCancel: Boolean,
    onCancel: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = SRose, strokeWidth = 3.dp, modifier = Modifier.size(48.dp))

            Text(
                stage,
                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = SWhite, textAlign = TextAlign.Center)
            )

            Text(
                "$scanCount of 3 scans used today",
                style = TextStyle(fontSize = 11.sp, color = SWhite70)
            )

            AnimatedVisibility(visible = showCancel) {
                TextButton(onClick = onCancel) {
                    Text("Cancel", style = TextStyle(fontSize = 13.sp, color = SWhite70))
                }
            }
        }
    }
}

// ── Camera permission denied ──────────────────────────────────────────────────

@Composable
private fun CameraPermissionDeniedScreen(
    onBack: () -> Unit,
    onRequestPermission: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(SDark), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(Icons.Outlined.Face, null, tint = SRose, modifier = Modifier.size(56.dp))
            Spacer(Modifier.height(16.dp))
            Text(
                "Camera access needed",
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SWhite),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Allow camera access so we can analyse your features and personalise recommendations.",
                style = TextStyle(fontSize = 13.sp, color = SWhite70, lineHeight = 20.sp),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = onRequestPermission,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SRose),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Allow Camera", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SWhite))
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onBack,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SWhite.copy(0.10f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Go back", style = TextStyle(fontSize = 14.sp, color = SWhite70))
            }
        }
    }
}
