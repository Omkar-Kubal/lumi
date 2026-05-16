package com.appylab.lumi.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
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
import com.appylab.lumi.data.model.FaceDetectionStatus
import com.appylab.lumi.data.model.ScanHint
import com.appylab.lumi.data.model.ScanType
import com.appylab.lumi.ui.viewmodel.ScanViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ── Scan screen colours (dark-themed, overlays camera) ────────────────────────
private val SRose        = Color(0xFFFF637E)
private val SWhite       = Color.White
private val SWhite70     = Color.White.copy(alpha = 0.70f)
private val SWhite40     = Color.White.copy(alpha = 0.40f)
private val SDark        = Color(0xFF0A0A0A)
private val SChipBg      = Color(0xFF1C1C1C).copy(alpha = 0.85f)
private val SGreen       = Color(0xFF22C55E)
private val SBorder      = Color(0xFFEDEDED)

// ── Normalized facial landmark dot positions (within oval bounding box 0..1) ──
private val faceLandmarkDots = listOf(
    // Forehead
    0.50f to 0.10f, 0.36f to 0.14f, 0.64f to 0.14f,
    0.26f to 0.22f, 0.74f to 0.22f,
    // Temples
    0.16f to 0.33f, 0.84f to 0.33f,
    // Left eye area
    0.28f to 0.32f, 0.35f to 0.29f, 0.42f to 0.31f, 0.35f to 0.36f,
    // Right eye area
    0.58f to 0.31f, 0.65f to 0.29f, 0.72f to 0.32f, 0.65f to 0.36f,
    // Nose bridge & tip
    0.50f to 0.38f, 0.50f to 0.45f, 0.50f to 0.52f,
    0.43f to 0.50f, 0.57f to 0.50f,
    // Cheeks
    0.22f to 0.46f, 0.78f to 0.46f,
    0.18f to 0.54f, 0.82f to 0.54f,
    // Upper lip
    0.43f to 0.60f, 0.50f to 0.58f, 0.57f to 0.60f,
    // Lower lip
    0.44f to 0.66f, 0.50f to 0.68f, 0.56f to 0.66f,
    // Chin & jaw
    0.50f to 0.76f, 0.42f to 0.73f, 0.58f to 0.73f,
    0.30f to 0.65f, 0.70f to 0.65f,
    0.22f to 0.60f, 0.78f to 0.60f,
)

@Composable
fun ScanScreen(
    viewModel: ScanViewModel = viewModel(),
    onBack: () -> Unit = {},
    onGalleryClick: () -> Unit = {},
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
        if (!cameraPermissionGranted) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (cameraPermissionGranted) {
        ScanContent(
            viewModel = viewModel,
            onBack = onBack,
            onGalleryClick = onGalleryClick,
            onScanComplete = onScanComplete
        )
    } else {
        CameraPermissionDeniedScreen(
            onBack = onBack,
            onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) }
        )
    }
}

// ── Main scan content (camera granted) ───────────────────────────────────────

@Composable
private fun ScanContent(
    viewModel: ScanViewModel,
    onBack: () -> Unit,
    onGalleryClick: () -> Unit,
    onScanComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Simulate face detection status in MVP after brief delay
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1_200)
        viewModel.onFaceDetectionUpdate(
            FaceDetectionStatus(
                imageQuality = true,
                faceDetected = true,
                notBlurry = true,
                goodLighting = true
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SDark)
    ) {
        // ── Camera preview (full screen background) ──────────────────────────
        CameraPreview(
            isFrontCamera = uiState.isFrontCamera,
            modifier = Modifier.fillMaxSize()
        )

        // ── Top gradient scrim ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.75f), Color.Transparent)
                    )
                )
                .align(Alignment.TopCenter)
        )

        // ── Bottom gradient scrim ─────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.92f))
                    )
                )
                .align(Alignment.BottomCenter)
        )

        // ── Main layout column ────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header
            ScanHeader(
                onBack = onBack,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )

            // Camera area (fills remaining space above bottom panel)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Face oval + mesh canvas
                FaceOverlayCanvas(
                    isDetecting = uiState.detectionStatus.faceDetected,
                    isWellPositioned = uiState.isWellPositioned,
                    modifier = Modifier.fillMaxSize()
                )

                // Hint chips
                uiState.lightingHint?.let { hint ->
                    HintChip(
                        icon = Icons.Outlined.WbSunny,
                        hint = hint,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 16.dp, top = 8.dp)
                    )
                }

                uiState.positionHints.getOrNull(0)?.let { hint ->
                    HintChip(
                        icon = null,
                        hint = hint,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 16.dp, top = 8.dp)
                    )
                }

                uiState.positionHints.getOrNull(1)?.let { hint ->
                    HintChip(
                        icon = null,
                        hint = hint,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 16.dp)
                    )
                }
            }

            // ── Bottom panel ─────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Detection status card
                DetectionStatusCard(
                    message = uiState.statusMessage,
                    status = uiState.detectionStatus,
                    isWellPositioned = uiState.isWellPositioned
                )

                Spacer(Modifier.height(16.dp))

                // Camera controls
                CameraControlsRow(
                    isCapturing = uiState.isCapturing,
                    isReadyToCapture = uiState.isWellPositioned,
                    onGalleryClick = onGalleryClick,
                    onCaptureClick = viewModel::capture,
                    onFlipClick = viewModel::flipCamera
                )

                Spacer(Modifier.height(14.dp))

                // Scan type selector
                ScanTypeSelector(
                    selectedType = uiState.scanType,
                    onTypeSelected = viewModel::selectScanType
                )

                Spacer(Modifier.height(10.dp))

                // Privacy note
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = SWhite40
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Your images are private and secure",
                        style = TextStyle(fontSize = 11.sp, color = SWhite40)
                    )
                }
            }

            Spacer(Modifier.navigationBarsPadding())
            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── CameraX preview ───────────────────────────────────────────────────────────

@Composable
private fun CameraPreview(
    isFrontCamera: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )

    LaunchedEffect(isFrontCamera) {
        val cameraProvider = withContext(Dispatchers.IO) {
            ProcessCameraProvider.getInstance(context).get()
        }
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        val selector = if (isFrontCamera) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview)
        } catch (_: Exception) { }
    }

    DisposableEffect(Unit) {
        onDispose {
            ProcessCameraProvider.getInstance(context).addListener({
                ProcessCameraProvider.getInstance(context).get().unbindAll()
            }, ContextCompat.getMainExecutor(context))
        }
    }
}

// ── Face oval + landmark dots canvas ─────────────────────────────────────────

@Composable
private fun FaceOverlayCanvas(
    isDetecting: Boolean,
    isWellPositioned: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scan_pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )

    val ovalColor = when {
        isWellPositioned -> SGreen
        isDetecting -> SRose
        else -> SWhite70
    }

    androidx.compose.foundation.Canvas(modifier = modifier) {
        val ovalW = size.width * 0.60f
        val ovalH = size.height * 0.56f
        val ovalLeft = (size.width - ovalW) / 2f
        val ovalTop = (size.height - ovalH) / 2f - size.height * 0.03f

        val ovalRect = Rect(
            left = ovalLeft,
            top = ovalTop,
            right = ovalLeft + ovalW,
            bottom = ovalTop + ovalH
        )

        // ── Corner bracket indicators ─────────────────────────────────────────
        val bracketLen = 22.dp.toPx()
        val bracketStroke = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        val bracketColor = ovalColor.copy(alpha = 0.9f)

        // Top-left
        drawLine(bracketColor, Offset(ovalRect.left, ovalRect.top + bracketLen), Offset(ovalRect.left, ovalRect.top), bracketStroke.width)
        drawLine(bracketColor, Offset(ovalRect.left, ovalRect.top), Offset(ovalRect.left + bracketLen, ovalRect.top), bracketStroke.width)
        // Top-right
        drawLine(bracketColor, Offset(ovalRect.right - bracketLen, ovalRect.top), Offset(ovalRect.right, ovalRect.top), bracketStroke.width)
        drawLine(bracketColor, Offset(ovalRect.right, ovalRect.top), Offset(ovalRect.right, ovalRect.top + bracketLen), bracketStroke.width)
        // Bottom-left
        drawLine(bracketColor, Offset(ovalRect.left, ovalRect.bottom - bracketLen), Offset(ovalRect.left, ovalRect.bottom), bracketStroke.width)
        drawLine(bracketColor, Offset(ovalRect.left, ovalRect.bottom), Offset(ovalRect.left + bracketLen, ovalRect.bottom), bracketStroke.width)
        // Bottom-right
        drawLine(bracketColor, Offset(ovalRect.right - bracketLen, ovalRect.bottom), Offset(ovalRect.right, ovalRect.bottom), bracketStroke.width)
        drawLine(bracketColor, Offset(ovalRect.right, ovalRect.bottom - bracketLen), Offset(ovalRect.right, ovalRect.bottom), bracketStroke.width)

        // ── Dashed oval outline ───────────────────────────────────────────────
        val ovalPath = Path().apply {
            addOval(ovalRect)
        }
        drawPath(
            path = ovalPath,
            color = ovalColor.copy(alpha = 0.80f),
            style = Stroke(
                width = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 7f))
            )
        )

        // ── Facial landmark dots ──────────────────────────────────────────────
        if (isDetecting) {
            faceLandmarkDots.forEach { (nx, ny) ->
                val x = ovalLeft + nx * ovalW
                val y = ovalTop + ny * ovalH
                // Only draw dots inside the oval
                val dx = (nx - 0.5f) * 2f
                val dy = (ny - 0.5f) * 2f
                if (dx * dx + dy * dy <= 1.05f) {
                    drawCircle(
                        color = ovalColor.copy(alpha = dotAlpha),
                        radius = 2.8.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}

// ── Scan header ───────────────────────────────────────────────────────────────

@Composable
private fun ScanHeader(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Close",
                tint = SWhite,
                modifier = Modifier.size(22.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Scan your face",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SWhite,
                    textAlign = TextAlign.Center
                )
            )
            Text(
                text = "We'll analyse and personalise for you",
                style = TextStyle(
                    fontSize = 11.sp,
                    color = SWhite70,
                    textAlign = TextAlign.Center
                )
            )
        }

        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Outlined.HelpOutline,
                contentDescription = "Help",
                tint = SWhite,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ── Hint chip ─────────────────────────────────────────────────────────────────

@Composable
private fun HintChip(
    icon: ImageVector?,
    hint: ScanHint,
    modifier: Modifier = Modifier
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
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFFBBF24),
                modifier = Modifier.size(14.dp)
            )
        }
        Column {
            Text(
                text = hint.title,
                style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SWhite)
            )
            Text(
                text = hint.subtitle,
                style = TextStyle(fontSize = 9.sp, color = SWhite70)
            )
        }
    }
}

// ── Detection status card ─────────────────────────────────────────────────────

@Composable
private fun DetectionStatusCard(
    message: String,
    status: FaceDetectionStatus,
    isWellPositioned: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isWellPositioned) Icons.Outlined.CheckCircle
                    else Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isWellPositioned) SGreen else Color(0xFF737373),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    text = message,
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SDark
                    )
                )
            }

            Spacer(Modifier.height(9.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DetectionItem("Image quality", status.imageQuality, Modifier.weight(1f))
                DetectionItem("Face detected", status.faceDetected, Modifier.weight(1f))
                DetectionItem("Not blurry", status.notBlurry, Modifier.weight(1f))
                DetectionItem("Good lighting", status.goodLighting, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DetectionItem(label: String, passed: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = TextStyle(fontSize = 9.sp, color = Color(0xFF737373)),
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = if (passed) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (passed) SGreen else Color(0xFFD1D5DB),
            modifier = Modifier.size(13.dp)
        )
    }
}

// ── Camera controls row ───────────────────────────────────────────────────────

@Composable
private fun CameraControlsRow(
    isCapturing: Boolean,
    isReadyToCapture: Boolean,
    onGalleryClick: () -> Unit,
    onCaptureClick: () -> Unit,
    onFlipClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Gallery
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(
                onClick = onGalleryClick,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SWhite.copy(alpha = 0.12f))
            ) {
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = "Gallery",
                    tint = SWhite,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text("Gallery", style = TextStyle(fontSize = 10.sp, color = SWhite70))
        }

        // Capture button
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(if (isReadyToCapture) SWhite else SWhite.copy(alpha = 0.5f))
                .clickable(enabled = isReadyToCapture && !isCapturing) { onCaptureClick() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .border(2.dp, SDark.copy(alpha = 0.15f), CircleShape)
                    .background(if (isCapturing) SRose else Color.Transparent)
            )
        }

        // Flip camera
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(
                onClick = onFlipClick,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SWhite.copy(alpha = 0.12f))
            ) {
                Icon(
                    imageVector = Icons.Outlined.Cameraswitch,
                    contentDescription = "Flip camera",
                    tint = SWhite,
                    modifier = Modifier.size(22.dp)
                )
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
    onTypeSelected: (ScanType) -> Unit
) {
    Column {
        Text(
            text = "Scan type",
            style = TextStyle(fontSize = 11.sp, color = SWhite70),
            modifier = Modifier.padding(start = 2.dp, bottom = 6.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SWhite.copy(alpha = 0.10f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ScanTypeTab(
                type = ScanType.FACE,
                icon = Icons.Outlined.Face,
                selected = selectedType == ScanType.FACE,
                onClick = { onTypeSelected(ScanType.FACE) },
                modifier = Modifier.weight(1f)
            )
            ScanTypeTab(
                type = ScanType.COLOR,
                icon = Icons.Outlined.Palette,
                selected = selectedType == ScanType.COLOR,
                onClick = { onTypeSelected(ScanType.COLOR) },
                modifier = Modifier.weight(1f)
            )
            ScanTypeTab(
                type = ScanType.GLOW_UP,
                icon = Icons.Outlined.AutoAwesome,
                selected = selectedType == ScanType.GLOW_UP,
                onClick = { onTypeSelected(ScanType.GLOW_UP) },
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) SRose.copy(alpha = 0.20f) else Color.Transparent)
            .border(
                width = if (selected) 1.dp else 0.dp,
                color = if (selected) SRose.copy(alpha = 0.50f) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) SRose else SWhite70,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = type.displayLabel,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) SRose else SWhite70
            )
        )
    }
}

// ── Permission denied fallback ─────────────────────────────────────────────────

@Composable
private fun CameraPermissionDeniedScreen(
    onBack: () -> Unit,
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Face,
                contentDescription = null,
                tint = SRose,
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Camera access needed",
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SWhite),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Allow camera access so we can analyse your features and personalise recommendations.",
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
                Text(
                    "Allow Camera",
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SWhite)
                )
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onBack,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SWhite.copy(alpha = 0.10f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Go back",
                    style = TextStyle(fontSize = 14.sp, color = SWhite70)
                )
            }
        }
    }
}
