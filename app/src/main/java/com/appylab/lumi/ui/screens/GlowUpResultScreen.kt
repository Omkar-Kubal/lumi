package com.appylab.lumi.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.appylab.lumi.data.model.GlowUpError
import com.appylab.lumi.data.model.GlowUpImageStatus
import com.appylab.lumi.data.model.ImpactLevel
import com.appylab.lumi.data.model.ImprovementArea
import com.appylab.lumi.data.model.ScanScorePoint
import com.appylab.lumi.data.model.StepGuide
import com.appylab.lumi.ui.viewmodel.GlowUpUiState
import com.appylab.lumi.ui.viewmodel.GlowUpViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Palette ───────────────────────────────────────────────────────────────────
private val GRose       = Color(0xFFFF637E)
private val GBackground = Color(0xFFFCFCFC)
private val GCard       = Color.White
private val GBorder     = Color(0xFFFFCCD3)
private val GText       = Color(0xFF0A0A0A)
private val GMuted      = Color(0xFF525252)
private val GMutedBg    = Color(0xFFF5F5F5)
private val GDark       = Color(0xFF0A0A0A)

// ── Screen entry point ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GlowUpResultScreen(
    faceAnalysisId: Long,
    viewModel: GlowUpViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    LaunchedEffect(faceAnalysisId) { viewModel.load(faceAnalysisId) }

    when {
        uiState.isLoading -> {
            Box(Modifier.fillMaxSize().background(GBackground), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GRose)
            }
        }
        uiState.error == GlowUpError.NotFound -> {
            GlowUpEmptyState(onBack = onBack)
        }
        else -> {
            GlowUpContent(
                uiState      = uiState,
                navBottom    = navBottom.value,
                onBack       = onBack,
                onAreaSelect = { area -> viewModel.selectArea(area) },
                onRetry      = { viewModel.retryImageGeneration(faceAnalysisId) }
            )
        }
    }
}

// ── Main content ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GlowUpContent(
    uiState: GlowUpUiState,
    navBottom: Float,
    onBack: () -> Unit,
    onAreaSelect: (String) -> Unit,
    onRetry: () -> Unit
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val areas = uiState.improvementAreas.ifEmpty { GlowUpViewModel.FALLBACK_IMPROVEMENT_AREAS }
    val areaNames = areas.map { it.area }
    val selectedTabIndex = areaNames.indexOf(uiState.selectedArea).coerceAtLeast(0)

    val activeGuide: StepGuide? = uiState.activeStepGuide
        ?: GlowUpViewModel.FALLBACK_STEP_GUIDES.find { it.area == uiState.selectedArea }
        ?: GlowUpViewModel.FALLBACK_STEP_GUIDES.firstOrNull()

    // LazyColumn item indices (0-based): topbar=0, before/after=1, score=2,
    // areas=3 (if non-empty), step guide=4(areas)/3(no areas)
    val stepGuideItemIndex = if (areas.isNotEmpty()) 4 else 3

    val shareReady = uiState.glowUpImageStatus == GlowUpImageStatus.COMPLETE
    var isGeneratingShare by remember { mutableStateOf(false) }

    // Info sheet state
    var showBeforeAfterInfo by remember { mutableStateOf(false) }
    var showImprovementInfo by remember { mutableStateOf(false) }
    var showProgressInfo    by remember { mutableStateOf(false) }

    if (showBeforeAfterInfo) {
        ModalBottomSheet(
            onDismissRequest = { showBeforeAfterInfo = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            GlowInfoSheet(
                title = "About Your Glow-Up Preview",
                body  = "This glow-up preview is AI-generated using your personalised recommendations. Results shown are for inspiration only."
            )
        }
    }
    if (showImprovementInfo) {
        ModalBottomSheet(
            onDismissRequest = { showImprovementInfo = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            GlowInfoSheet(
                title = "Improvement Priority",
                body  = "These areas were identified as having the highest impact on your overall glow-up score. Tap each to see your personalised guide."
            )
        }
    }
    if (showProgressInfo) {
        ModalBottomSheet(
            onDismissRequest = { showProgressInfo = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            GlowInfoSheet(
                title = "Progress Tracker",
                body  = "This chart shows your Glow Score across all your scans. Re-scan regularly to track your improvement."
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(GBackground)) {

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = 8.dp, bottom = navBottom.dp + 88.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 0 — Top bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = GText, modifier = Modifier.size(22.dp))
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Feature Glow Up",
                            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = GText))
                        Text("See your transformation and track your progress",
                            style = TextStyle(fontSize = 11.sp, color = GMuted, textAlign = TextAlign.Center))
                    }
                    IconButton(onClick = {
                        scope.launch {
                            isGeneratingShare = true
                            shareGlowUpCard(context, uiState)
                            isGeneratingShare = false
                        }
                    }) {
                        Icon(Icons.Outlined.Share, "Share", tint = GText, modifier = Modifier.size(22.dp))
                    }
                }
            }

            // 1 — Before / After comparison
            item {
                BeforeAfterCard(
                    originalImageUrl  = uiState.originalImageUrl,
                    glowUpImageUrl    = uiState.glowUpImageUrl,
                    glowUpImageStatus = uiState.glowUpImageStatus,
                    onRetry           = onRetry,
                    onInfoClick       = { showBeforeAfterInfo = true }
                )
            }

            // 2 — Glow-Up Score
            item {
                GlowScoreCard(
                    score      = uiState.score,
                    scoreDelta = uiState.scoreDelta,
                    label      = uiState.verdictLabel,
                    body       = uiState.verdictBody
                )
            }

            // 3 — Improvement Areas (conditional)
            if (areas.isNotEmpty()) {
                item {
                    ImprovementAreasCard(
                        areas        = areas,
                        selectedArea = uiState.selectedArea,
                        onInfoClick  = { showImprovementInfo = true },
                        onAreaTap    = { area ->
                            onAreaSelect(area)
                            scope.launch { listState.animateScrollToItem(stepGuideItemIndex) }
                        }
                    )
                }
            }

            // 4 (or 3) — Actionable Step Guide
            item {
                StepGuideCard(
                    areaNames        = areaNames,
                    selectedTabIndex = selectedTabIndex,
                    activeGuide      = activeGuide,
                    onTabSelect      = onAreaSelect
                )
            }

            // Progress Tracker (conditional — 2+ scans only)
            if (uiState.progressData.size >= 2) {
                item {
                    ProgressTrackerCard(
                        progressData = uiState.progressData,
                        onInfoClick  = { showProgressInfo = true }
                    )
                }
            }

            // Share preview row
            item {
                SharePreviewCard(
                    originalImageUrl = uiState.originalImageUrl,
                    glowUpImageUrl   = uiState.glowUpImageUrl,
                    score            = uiState.score,
                    scoreDelta       = uiState.scoreDelta,
                    verdictLabel     = uiState.verdictLabel,
                    shareReady       = shareReady,
                    isGenerating     = isGeneratingShare,
                    onShareClick     = {
                        scope.launch {
                            isGeneratingShare = true
                            shareGlowUpCard(context, uiState)
                            isGeneratingShare = false
                        }
                    }
                )
            }
        }

        // Sticky bottom bar
        StickyShareBar(
            modifier     = Modifier.align(Alignment.BottomCenter),
            navBottom    = navBottom,
            shareReady   = shareReady,
            isGenerating = isGeneratingShare,
            onShareClick = {
                scope.launch {
                    isGeneratingShare = true
                    shareGlowUpCard(context, uiState)
                    isGeneratingShare = false
                }
            }
        )
    }
}

// ── Before / After comparison card ───────────────────────────────────────────

@Composable
private fun BeforeAfterCard(
    originalImageUrl: String?,
    glowUpImageUrl: String?,
    glowUpImageStatus: GlowUpImageStatus,
    onRetry: () -> Unit,
    onInfoClick: () -> Unit
) {
    var sliderFraction by remember { mutableFloatStateOf(0.5f) }

    GlowCard {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Before / After Comparison",
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = GText))
            IconButton(onClick = onInfoClick, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Outlined.Info, null, tint = GMuted, modifier = Modifier.size(16.dp))
            }
        }
        Text("Drag the slider to compare",
            style = TextStyle(fontSize = 11.sp, color = GMuted))
        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(10.dp))
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, _ ->
                        change.consume()
                        sliderFraction = (change.position.x / size.width).coerceIn(0.05f, 0.95f)
                    }
                }
        ) {
            // Before image (full width behind)
            if (originalImageUrl != null) {
                AsyncImage(
                    model              = originalImageUrl,
                    contentDescription = "Before",
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )
            } else {
                GlowImagePlaceholder("Before", Modifier.fillMaxSize())
            }

            // After image (right portion revealed by slider)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(1f - sliderFraction)
                    .align(Alignment.CenterEnd)
                    .clip(RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp))
            ) {
                when (glowUpImageStatus) {
                    GlowUpImageStatus.COMPLETE -> {
                        if (glowUpImageUrl != null) {
                            AsyncImage(
                                model              = glowUpImageUrl,
                                contentDescription = "After",
                                contentScale       = ContentScale.Crop,
                                modifier           = Modifier.fillMaxSize()
                            )
                        } else {
                            GlowImagePlaceholder("After", Modifier.fillMaxSize())
                        }
                    }
                    GlowUpImageStatus.GENERATING -> GeneratingPlaceholder()
                    GlowUpImageStatus.FAILED     -> FailedPlaceholder(onRetry)
                    GlowUpImageStatus.PENDING    -> ShimmerPlaceholder()
                }
            }

            // Divider + handle drawn via Canvas overlay (avoids padding math)
            if (glowUpImageStatus == GlowUpImageStatus.COMPLETE) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val x = sliderFraction * size.width
                    drawLine(Color.White, Offset(x, 0f), Offset(x, size.height), 2.dp.toPx())
                    drawCircle(Color.White, 14.dp.toPx(), Offset(x, size.height / 2f))
                    val arr = 4.dp.toPx()
                    val cy = size.height / 2f
                    // Left arrow
                    drawLine(GDark, Offset(x - arr, cy - arr), Offset(x - arr * 2, cy), 1.5.dp.toPx())
                    drawLine(GDark, Offset(x - arr, cy + arr), Offset(x - arr * 2, cy), 1.5.dp.toPx())
                    // Right arrow
                    drawLine(GDark, Offset(x + arr, cy - arr), Offset(x + arr * 2, cy), 1.5.dp.toPx())
                    drawLine(GDark, Offset(x + arr, cy + arr), Offset(x + arr * 2, cy), 1.5.dp.toPx())
                }
            }

            // Pill badges
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart).padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) { Text("Before", style = TextStyle(fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.SemiBold)) }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd).padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(GRose.copy(alpha = 0.85f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) { Text("After (AI Glow-Up)", style = TextStyle(fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.SemiBold)) }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "AI-generated glow-up using your personalised recommendations.",
            style = TextStyle(fontSize = 10.sp, color = GMuted, textAlign = TextAlign.Center),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ShimmerPlaceholder() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue  = 0.3f,
        targetValue   = 0.7f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label         = "shimmerAlpha"
    )
    Box(Modifier.fillMaxSize().background(GMutedBg.copy(alpha = alpha)))
}

@Composable
private fun GeneratingPlaceholder() {
    val transition = rememberInfiniteTransition(label = "generating")
    val offset by transition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
        label         = "gradientOffset"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFFFCCD3), Color(0xFFFF637E).copy(alpha = 0.4f), Color(0xFFFFCCD3)),
                    start  = Offset(offset * 1000f, 0f),
                    end    = Offset(offset * 1000f + 500f, 500f)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = GRose, modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
            Spacer(Modifier.height(8.dp))
            Text("Generating your glow-up…",
                style = TextStyle(fontSize = 11.sp, color = GDark, fontWeight = FontWeight.Medium))
        }
    }
}

@Composable
private fun FailedPlaceholder(onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(GMutedBg), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Generation failed", style = TextStyle(fontSize = 12.sp, color = GMuted))
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onRetry, shape = RoundedCornerShape(8.dp)) {
                Icon(Icons.Outlined.Refresh, null, modifier = Modifier.size(14.dp), tint = GRose)
                Spacer(Modifier.width(4.dp))
                Text("Retry", style = TextStyle(fontSize = 12.sp, color = GRose))
            }
        }
    }
}

@Composable
private fun GlowImagePlaceholder(label: String, modifier: Modifier) {
    Box(modifier.background(GMutedBg), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.Person, null, tint = GMuted, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, style = TextStyle(fontSize = 11.sp, color = GMuted, fontWeight = FontWeight.SemiBold))
        }
    }
}

// ── Score card ────────────────────────────────────────────────────────────────

@Composable
private fun GlowScoreCard(score: Int, scoreDelta: Int?, label: String, body: String) {
    var animatedProgress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(score) {
        animate(0f, score / 100f, animationSpec = tween(600, easing = LinearEasing)) { v, _ -> animatedProgress = v }
    }

    GlowCard {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(90.dp)) {
                Canvas(Modifier.size(90.dp)) {
                    val stroke = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round)
                    drawArc(GMutedBg, -90f, 360f, false, style = stroke)
                    drawArc(GRose, -90f, 360f * animatedProgress, false, style = stroke)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$score", style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = GText))
                    Text("/100", style = TextStyle(fontSize = 9.sp, color = GMuted))
                }
            }

            Column(Modifier.weight(1f)) {
                Text(label, style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = GText))
                Spacer(Modifier.height(2.dp))
                Text(body, style = TextStyle(fontSize = 11.sp, color = GMuted))
                Spacer(Modifier.height(8.dp))
                if (scoreDelta != null) {
                    val (bgColor, fgColor, prefix) = when {
                        scoreDelta > 0  -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "+")
                        scoreDelta < 0  -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "")
                        else            -> Triple(GMutedBg, GMuted, "")
                    }
                    val text = if (scoreDelta == 0) "Same as last scan" else "$prefix$scoreDelta vs last scan"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(bgColor)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text, style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = fgColor))
                    }
                }
            }
        }
    }
}

// ── Improvement Areas card ────────────────────────────────────────────────────

@Composable
private fun ImprovementAreasCard(
    areas: List<ImprovementArea>,
    selectedArea: String,
    onInfoClick: () -> Unit,
    onAreaTap: (String) -> Unit
) {
    GlowCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Improvement Areas (Priority)",
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = GText))
            IconButton(onClick = onInfoClick, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Outlined.Info, null, tint = GMuted, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(areas) { area ->
                ImprovementAreaCard(
                    area       = area,
                    isSelected = area.area == selectedArea,
                    onClick    = { onAreaTap(area.area) }
                )
            }
        }
    }
}

@Composable
private fun ImprovementAreaCard(area: ImprovementArea, isSelected: Boolean, onClick: () -> Unit) {
    val (bgColor, borderColor) = when {
        isSelected              -> GRose.copy(alpha = 0.15f) to GRose
        area.impact == ImpactLevel.HIGH -> GRose.copy(alpha = 0.08f) to GRose.copy(alpha = 0.3f)
        else                    -> GMutedBg to Color.Transparent
    }
    val impactLabel = when (area.impact) {
        ImpactLevel.HIGH   -> "High Impact"
        ImpactLevel.MEDIUM -> "Medium Impact"
        ImpactLevel.LOW    -> "Low Impact"
    }
    val impactColor = when (area.impact) {
        ImpactLevel.HIGH   -> GRose
        ImpactLevel.MEDIUM -> Color(0xFF9C27B0)
        ImpactLevel.LOW    -> GMuted
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .width(110.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AreaIllustration(key = area.illustrationAsset, modifier = Modifier.size(44.dp))
        Spacer(Modifier.height(8.dp))
        Text(
            area.area,
            style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = GText),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(impactColor.copy(alpha = 0.12f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(impactLabel, style = TextStyle(fontSize = 9.sp, color = impactColor, fontWeight = FontWeight.SemiBold))
        }
        if (area.scorePotential > 0) {
            Spacer(Modifier.height(3.dp))
            Text("+${area.scorePotential} potential", style = TextStyle(fontSize = 10.sp, color = GRose))
        }
    }
}

// ── Actionable Step Guide card ────────────────────────────────────────────────

@Composable
private fun StepGuideCard(
    areaNames: List<String>,
    selectedTabIndex: Int,
    activeGuide: StepGuide?,
    onTabSelect: (String) -> Unit
) {
    GlowCard(padding = 0.dp) {
        Column(modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)) {
            Text("Actionable Step Guide",
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = GText))
        }
        Spacer(Modifier.height(8.dp))

        if (areaNames.isNotEmpty()) {
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor   = GCard,
                contentColor     = GRose,
                edgePadding      = 16.dp,
                divider          = {}
            ) {
                areaNames.forEachIndexed { index, name ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick  = { onTabSelect(name) },
                        text = {
                            Text(name, style = TextStyle(
                                fontSize   = 12.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.SemiBold else FontWeight.Normal,
                                color      = if (selectedTabIndex == index) GRose else GMuted
                            ))
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        AnimatedContent(
            targetState  = activeGuide,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
            label        = "stepGuideContent"
        ) { guide ->
            if (guide != null) {
                StepGuideContent(guide = guide)
            } else {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun StepGuideContent(guide: StepGuide) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        // Area illustration (large)
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(GRose.copy(alpha = 0.08f))
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            AreaIllustration(key = guide.area.lowercase(), modifier = Modifier.size(64.dp))
        }
        Spacer(Modifier.height(14.dp))

        if (guide.goal.isNotBlank()) {
            Text("Your Goal", style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = GText))
            Spacer(Modifier.height(4.dp))
            Text(guide.goal, style = TextStyle(fontSize = 12.sp, color = GMuted, lineHeight = 17.sp))
            Spacer(Modifier.height(12.dp))
        }
        if (guide.recommendations.isNotEmpty()) {
            Text("Top Recommendations",
                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = GText))
            Spacer(Modifier.height(8.dp))
            guide.recommendations.forEach { rec ->
                RecommendationRow(text = rec)
                Spacer(Modifier.height(6.dp))
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun RecommendationRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(GMutedBg)
            .padding(10.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Outlined.CheckCircle, null, tint = GRose,
            modifier = Modifier.size(16.dp).padding(top = 1.dp))
        Text(text, style = TextStyle(fontSize = 11.sp, color = GText, lineHeight = 15.sp))
    }
}

// ── Progress Tracker card ─────────────────────────────────────────────────────

@Composable
private fun ProgressTrackerCard(
    progressData: List<ScanScorePoint>,
    onInfoClick: () -> Unit
) {
    GlowCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Progress Tracker",
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = GText))
            IconButton(onClick = onInfoClick, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Outlined.Info, null, tint = GMuted, modifier = Modifier.size(16.dp))
            }
        }
        Text("Your glow-up journey over time", style = TextStyle(fontSize = 11.sp, color = GMuted))
        Spacer(Modifier.height(16.dp))
        ProgressChart(dataPoints = progressData)
        Spacer(Modifier.height(8.dp))
        val totalScans = progressData.size
        val avgImprovement = if (totalScans >= 2) {
            val delta = progressData.last().score - progressData.first().score
            delta.toFloat() / (totalScans - 1)
        } else null
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total Scans: $totalScans", style = TextStyle(fontSize = 11.sp, color = GMuted))
            Text(
                if (avgImprovement != null) "Avg: +%.1f/scan".format(avgImprovement) else "Avg: —",
                style = TextStyle(fontSize = 11.sp, color = GMuted)
            )
        }
    }
}

private val DATE_FMT = SimpleDateFormat("MMM d", Locale.getDefault())

@Composable
private fun ProgressChart(dataPoints: List<ScanScorePoint>) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 9.sp, color = GMuted)

    Canvas(modifier = Modifier.fillMaxWidth().height(130.dp)) {
        val leftPad  = 32.dp.toPx()
        val rightPad = 8.dp.toPx()
        val topPad   = 8.dp.toPx()
        val botPad   = 8.dp.toPx()
        val cw = size.width - leftPad - rightPad
        val ch = size.height - topPad - botPad
        val xStep = if (dataPoints.size > 1) cw / (dataPoints.size - 1) else cw

        // Y grid lines
        listOf(25, 50, 75, 100).forEach { yVal ->
            val y = topPad + ch * (1f - yVal / 100f)
            drawLine(GMuted.copy(alpha = 0.15f), Offset(leftPad, y), Offset(size.width - rightPad, y), 1.dp.toPx())
            drawText(textMeasurer, "$yVal", Offset(0f, y - 6.dp.toPx()), labelStyle)
        }

        val pts = dataPoints.mapIndexed { i, sp ->
            Offset(leftPad + i * xStep, topPad + ch * (1f - sp.score.coerceIn(0, 100) / 100f))
        }

        // Connecting line
        val path = Path().apply {
            moveTo(pts[0].x, pts[0].y)
            for (i in 1 until pts.size) lineTo(pts[i].x, pts[i].y)
        }
        drawPath(path, GRose, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))

        // Projected dashed line
        if (dataPoints.size >= 2) {
            val avgDelta = (dataPoints.last().score - dataPoints.first().score).toFloat() / (dataPoints.size - 1)
            val projected = (dataPoints.last().score + avgDelta.toInt().coerceIn(-20, 20)).coerceIn(0, 100)
            val projX = pts.last().x + xStep
            val projY = topPad + ch * (1f - projected / 100f)
            val dashPath = Path().apply { moveTo(pts.last().x, pts.last().y); lineTo(projX, projY) }
            drawPath(dashPath, GRose.copy(alpha = 0.4f), style = Stroke(1.5.dp.toPx()))
            drawCircle(GRose.copy(alpha = 0.5f), 4.dp.toPx(), Offset(projX, projY), style = Stroke(1.5.dp.toPx()))
        }

        // Data point dots
        pts.forEachIndexed { i, pt ->
            if (i == pts.lastIndex) {
                drawCircle(GRose, 5.dp.toPx(), pt, style = Stroke(2.dp.toPx()))
            } else {
                drawCircle(GRose, 4.dp.toPx(), pt)
                drawCircle(Color.White, 2.5.dp.toPx(), pt)
            }
        }
    }

    Spacer(Modifier.height(4.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        dataPoints.forEach { sp ->
            Text(
                DATE_FMT.format(Date(sp.date)),
                style = TextStyle(fontSize = 8.sp, color = GMuted, textAlign = TextAlign.Center),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Share preview card ────────────────────────────────────────────────────────

@Composable
private fun SharePreviewCard(
    originalImageUrl: String?,
    glowUpImageUrl: String?,
    score: Int,
    scoreDelta: Int?,
    verdictLabel: String,
    shareReady: Boolean,
    isGenerating: Boolean,
    onShareClick: () -> Unit
) {
    GlowCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = shareReady && !isGenerating) { onShareClick() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Selfie thumbnail with score badge overlay
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(GMutedBg),
                    contentAlignment = Alignment.Center
                ) {
                    if (originalImageUrl != null) {
                        AsyncImage(
                            model = originalImageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Icon(Icons.Outlined.Person, null, tint = GMuted, modifier = Modifier.size(24.dp))
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(GRose)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text("$score", style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White))
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text("My Glow-Up Score", style = TextStyle(fontSize = 10.sp, color = GMuted))
                Text("$score / 100", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GText))
                if (scoreDelta != null && scoreDelta != 0) {
                    val deltaColor = if (scoreDelta > 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                    val prefix = if (scoreDelta > 0) "+" else ""
                    Text("$prefix$scoreDelta vs last scan", style = TextStyle(fontSize = 10.sp, color = deltaColor))
                }
                Text(verdictLabel, style = TextStyle(fontSize = 11.sp, color = GRose))
                Text("Generated by Lumi", style = TextStyle(fontSize = 9.sp, color = GMuted))
            }

            // Mini share card preview
            Box(
                modifier = Modifier
                    .width(56.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(GMutedBg),
                contentAlignment = Alignment.Center
            ) {
                if (glowUpImageUrl != null) {
                    AsyncImage(
                        model = glowUpImageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Outlined.AutoAwesome, null, tint = GRose, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

// ── Sticky bottom share bar ───────────────────────────────────────────────────

@Composable
private fun StickyShareBar(
    modifier: Modifier = Modifier,
    navBottom: Float,
    shareReady: Boolean,
    isGenerating: Boolean,
    onShareClick: () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (shareReady) GDark else GMutedBg,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (shareReady && !isGenerating) Modifier.clickable { onShareClick() } else Modifier)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isGenerating) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
                Text("Generating…",
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                        color = if (shareReady) Color.White else GMuted))
            } else {
                Icon(
                    Icons.Outlined.Share, null,
                    tint = if (shareReady) Color.White else GMuted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    if (shareReady) "Generate & Share Card"
                    else "Share in Progress — image generating",
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                        color = if (shareReady) Color.White else GMuted)
                )
            }
        }
    }
}

// ── Info bottom sheet content ─────────────────────────────────────────────────

@Composable
private fun GlowInfoSheet(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 24.dp)
    ) {
        Text(title, style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GText))
        Spacer(Modifier.height(10.dp))
        Text(body, style = TextStyle(fontSize = 13.sp, color = GMuted, lineHeight = 20.sp))
    }
}

// ── Area illustration canvas ──────────────────────────────────────────────────

@Composable
private fun AreaIllustration(key: String, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)

        when (key.lowercase().trim()) {
            "brows" -> {
                val left = Path().apply {
                    moveTo(w * 0.08f, h * 0.62f)
                    cubicTo(w * 0.20f, h * 0.28f, w * 0.36f, h * 0.20f, w * 0.48f, h * 0.42f)
                }
                drawPath(left, GRose, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                val right = Path().apply {
                    moveTo(w * 0.52f, h * 0.42f)
                    cubicTo(w * 0.64f, h * 0.20f, w * 0.80f, h * 0.28f, w * 0.92f, h * 0.62f)
                }
                drawPath(right, GRose, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
            }
            "skin" -> {
                drawOval(GRose.copy(alpha = 0.25f),
                    topLeft = Offset(w * 0.15f, h * 0.05f),
                    size = Size(w * 0.70f, h * 0.90f),
                    style = stroke)
                listOf(0.30f to 0.28f, 0.65f to 0.32f, 0.50f to 0.50f, 0.35f to 0.60f, 0.70f to 0.64f).forEach { (xF, yF) ->
                    drawCircle(GRose, 2.5.dp.toPx(), Offset(w * xF, h * yF))
                }
            }
            "eye" -> {
                val path = Path()
                path.moveTo(w * 0.08f, h * 0.50f)
                path.cubicTo(w * 0.16f, h * 0.32f, w * 0.34f, h * 0.28f, w * 0.45f, h * 0.50f)
                path.cubicTo(w * 0.34f, h * 0.68f, w * 0.16f, h * 0.68f, w * 0.08f, h * 0.50f)
                path.moveTo(w * 0.55f, h * 0.50f)
                path.cubicTo(w * 0.66f, h * 0.28f, w * 0.84f, h * 0.32f, w * 0.92f, h * 0.50f)
                path.cubicTo(w * 0.84f, h * 0.68f, w * 0.66f, h * 0.68f, w * 0.55f, h * 0.50f)
                drawPath(path, GRose, style = stroke)
                drawCircle(GRose.copy(alpha = 0.5f), w * 0.055f, Offset(w * 0.265f, h * 0.50f))
                drawCircle(GRose.copy(alpha = 0.5f), w * 0.055f, Offset(w * 0.735f, h * 0.50f))
            }
            "lips" -> {
                val upper = Path().apply {
                    moveTo(w * 0.12f, h * 0.50f)
                    cubicTo(w * 0.25f, h * 0.28f, w * 0.40f, h * 0.24f, w * 0.50f, h * 0.40f)
                    cubicTo(w * 0.60f, h * 0.24f, w * 0.75f, h * 0.28f, w * 0.88f, h * 0.50f)
                }
                val lower = Path().apply {
                    moveTo(w * 0.12f, h * 0.50f)
                    cubicTo(w * 0.30f, h * 0.80f, w * 0.70f, h * 0.80f, w * 0.88f, h * 0.50f)
                }
                drawPath(upper, GRose, style = stroke)
                drawPath(lower, GRose, style = stroke)
                drawPath(lower, GRose.copy(alpha = 0.12f))
            }
            "hair" -> {
                listOf(0.18f to 0.12f, 0.35f to 0.06f, 0.50f to 0.04f, 0.65f to 0.06f, 0.82f to 0.12f).forEachIndexed { i, (sx, sy) ->
                    val curvePath = Path().apply {
                        moveTo(w * sx, h * sy)
                        cubicTo(w * (sx - 0.10f), h * 0.45f, w * (sx - 0.12f), h * 0.72f, w * (sx - 0.08f), h)
                    }
                    drawPath(curvePath, GRose.copy(alpha = if (i == 2) 1f else 0.45f),
                        style = Stroke(width = if (i == 2) 2.5.dp.toPx() else 1.5.dp.toPx(), cap = StrokeCap.Round))
                }
            }
            "jawline" -> {
                val path = Path().apply {
                    moveTo(w * 0.18f, h * 0.12f)
                    cubicTo(w * 0.06f, h * 0.50f, w * 0.18f, h * 0.90f, w * 0.50f, h * 0.97f)
                    cubicTo(w * 0.82f, h * 0.90f, w * 0.94f, h * 0.50f, w * 0.82f, h * 0.12f)
                }
                drawPath(path, GRose, style = stroke)
                drawCircle(GRose, 3.5.dp.toPx(), Offset(w * 0.50f, h * 0.96f))
            }
            "contour" -> {
                drawOval(GRose.copy(alpha = 0.22f),
                    topLeft = Offset(w * 0.15f, h * 0.05f),
                    size = Size(w * 0.70f, h * 0.90f),
                    style = stroke)
                val leftPath = Path().apply { moveTo(w * 0.15f, h * 0.40f); lineTo(w * 0.38f, h * 0.56f) }
                drawPath(leftPath, GRose, style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round))
                val rightPath = Path().apply { moveTo(w * 0.85f, h * 0.40f); lineTo(w * 0.62f, h * 0.56f) }
                drawPath(rightPath, GRose, style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round))
            }
            else -> {
                // Sparkle / star default
                val cx = w * 0.50f; val cy = h * 0.50f
                val outerR = w * 0.34f; val innerR = w * 0.15f
                val starPath = Path()
                for (i in 0 until 8) {
                    val angle = (Math.PI / 4 * i - Math.PI / 2).toFloat()
                    val r = if (i % 2 == 0) outerR else innerR
                    val x = cx + r * Math.cos(angle.toDouble()).toFloat()
                    val y = cy + r * Math.sin(angle.toDouble()).toFloat()
                    if (i == 0) starPath.moveTo(x, y) else starPath.lineTo(x, y)
                }
                starPath.close()
                drawPath(starPath, GRose.copy(alpha = 0.15f))
                drawPath(starPath, GRose, style = stroke)
            }
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun GlowUpEmptyState(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GBackground)
            .statusBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(Icons.Outlined.AutoAwesome, null, tint = GRose, modifier = Modifier.size(56.dp))
            Spacer(Modifier.height(16.dp))
            Text("No glow-up yet",
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = GText))
            Spacer(Modifier.height(8.dp))
            Text(
                "Complete a scan to see your AI-powered glow-up transformation.",
                style = TextStyle(fontSize = 13.sp, color = GMuted, textAlign = TextAlign.Center)
            )
            Spacer(Modifier.height(24.dp))
            OutlinedButton(onClick = onBack, shape = RoundedCornerShape(10.dp)) {
                Text("Go Back", color = GRose)
            }
        }
    }
}

// ── Shared card container ─────────────────────────────────────────────────────

@Composable
private fun GlowCard(
    modifier: Modifier = Modifier,
    padding: androidx.compose.ui.unit.Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(GCard)
            .border(1.dp, GBorder, RoundedCornerShape(12.dp))
            .padding(padding)
    ) { content() }
}

// ── Share card generation ─────────────────────────────────────────────────────

private fun generateGlowUpShareBitmap(uiState: GlowUpUiState): Bitmap {
    val size = 1080
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)

    val bgPaint = Paint().apply { color = android.graphics.Color.parseColor("#FCFCFC") }
    canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), bgPaint)

    val rosePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#FF637E")
        textSize = 72f
        typeface = Typeface.DEFAULT_BOLD
    }
    canvas.drawText("Lumi", 80f, 120f, rosePaint)

    val divPaint = Paint().apply { color = android.graphics.Color.parseColor("#FFCCD3") }
    canvas.drawRect(80f, 145f, (size - 80).toFloat(), 148f, divPaint)

    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#0A0A0A")
        textSize = 86f
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText("Glow-Up Score", size / 2f, 280f, titlePaint)

    val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#FF637E")
        textSize = 160f
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText("${uiState.score}", size / 2f, 470f, scorePaint)

    val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#525252")
        textSize = 48f
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText(uiState.verdictLabel, size / 2f, 550f, subPaint)

    val delta = uiState.scoreDelta
    if (delta != null) {
        val deltaText = if (delta >= 0) "+$delta vs last scan" else "$delta vs last scan"
        val deltaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (delta >= 0) android.graphics.Color.parseColor("#2E7D32")
            else android.graphics.Color.parseColor("#C62828")
            textSize = 40f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(deltaText, size / 2f, 615f, deltaPaint)
    }

    canvas.drawRect(80f, 660f, (size - 80).toFloat(), 663f, divPaint)

    val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#525252")
        textSize = 38f
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText(uiState.verdictBody, size / 2f, 730f, bodyPaint)

    val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#A4A4A4")
        textSize = 34f
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText("Generated by Lumi", size / 2f, 1050f, footerPaint)

    return bitmap
}

private suspend fun shareGlowUpCard(context: Context, uiState: GlowUpUiState) {
    val bitmap = withContext(Dispatchers.Default) { generateGlowUpShareBitmap(uiState) }
    val uri = withContext(Dispatchers.IO) {
        val file = File(context.cacheDir, "lumi_glowup_card.jpg")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }
    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "image/jpeg"
        putExtra(android.content.Intent.EXTRA_STREAM, uri)
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    withContext(Dispatchers.Main) {
        context.startActivity(android.content.Intent.createChooser(intent, "Share your glow-up"))
    }
}
