package com.appylab.lumi.ui.screens

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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
internal fun GlowUpResultScreen(
    faceAnalysisId: Long,
    viewModel: GlowUpViewModel = viewModel(),
    onBack: () -> Unit = {},
    onPaywall: () -> Unit = {}
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
        uiState.error == GlowUpError.AccessDenied -> {
            LaunchedEffect(Unit) { onPaywall() }
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

@Composable
private fun GlowUpContent(
    uiState: GlowUpUiState,
    navBottom: Float,
    onBack: () -> Unit,
    onAreaSelect: (String) -> Unit,
    onRetry: () -> Unit
) {
    // Resolve improvement areas — prefer live data, fall back to defaults
    val areas = uiState.improvementAreas
        .ifEmpty { GlowUpViewModel.FALLBACK_IMPROVEMENT_AREAS }
    // Resolve active tab (area names for the ScrollableTabRow)
    val areaNames = areas.map { it.area }
    val selectedTabIndex = areaNames.indexOf(uiState.selectedArea).coerceAtLeast(0)

    // Resolve step guide — prefer live data, fall back to hardcoded guide
    val activeGuide: StepGuide? = uiState.activeStepGuide
        ?: GlowUpViewModel.FALLBACK_STEP_GUIDES.find { it.area == uiState.selectedArea }
        ?: GlowUpViewModel.FALLBACK_STEP_GUIDES.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(GBackground),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = 8.dp, bottom = navBottom.dp + 76.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Top bar
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
                    Text("Your Glow-Up Results",
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = GText))
                    Text("Here's how your features have improved",
                        style = TextStyle(fontSize = 11.sp, color = GMuted, textAlign = TextAlign.Center))
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Outlined.Share, "Share", tint = GText, modifier = Modifier.size(22.dp))
                }
            }
        }

        // ── Before / After comparison
        item {
            BeforeAfterCard(
                originalImageUrl  = uiState.originalImageUrl,
                glowUpImageUrl    = uiState.glowUpImageUrl,
                glowUpImageStatus = uiState.glowUpImageStatus,
                onRetry           = onRetry
            )
        }

        // ── Glow-Up Score
        item {
            GlowScoreCard(
                score      = uiState.score,
                scoreDelta = uiState.scoreDelta,
                label      = uiState.verdictLabel,
                body       = uiState.verdictBody
            )
        }

        // ── Improvement Areas
        if (areas.isNotEmpty()) {
            item {
                GlowCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Improvement Areas (Priority)",
                            style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = GText))
                        Icon(Icons.Outlined.Info, null, tint = GMuted, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(areas) { area ->
                            ImprovementAreaCard(
                                area       = area,
                                isSelected = area.area == uiState.selectedArea,
                                onClick    = { onAreaSelect(area.area) }
                            )
                        }
                    }
                }
            }
        }

        // ── Actionable Step Guide
        item {
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
                                onClick  = { onAreaSelect(name) },
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
                if (activeGuide != null) {
                    StepGuideContent(guide = activeGuide)
                }
            }
        }

        // ── Progress Tracker (shown only for 2+ scans)
        if (uiState.progressData.size >= 2) {
            item {
                GlowCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Progress Tracker",
                            style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = GText))
                        Icon(Icons.Outlined.Info, null, tint = GMuted, modifier = Modifier.size(16.dp))
                    }
                    Text("Your glow-up journey over time",
                        style = TextStyle(fontSize = 11.sp, color = GMuted))
                    Spacer(Modifier.height(16.dp))
                    ProgressChart(dataPoints = uiState.progressData)
                    Spacer(Modifier.height(8.dp))
                    // Stats row
                    val totalScans = uiState.progressData.size
                    val avgImprovement = if (totalScans >= 2) {
                        val delta = uiState.progressData.last().score - uiState.progressData.first().score
                        delta.toFloat() / (totalScans - 1)
                    } else null
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Scans: $totalScans",
                            style = TextStyle(fontSize = 11.sp, color = GMuted))
                        Text(
                            if (avgImprovement != null) "Avg: +%.1f/scan".format(avgImprovement)
                            else "Avg: —",
                            style = TextStyle(fontSize = 11.sp, color = GMuted)
                        )
                    }
                }
            }
        }

        // ── Share section
        item {
            GlowCard {
                Text("Share Your Glow-Up",
                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = GText))
                Spacer(Modifier.height(4.dp))
                Text("Share your before & after and inspire others",
                    style = TextStyle(fontSize = 11.sp, color = GMuted))
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SharePreviewImage(url = uiState.originalImageUrl, label = "Before", modifier = Modifier.weight(1f))
                    SharePreviewImage(url = uiState.glowUpImageUrl, label = "After", modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                val shareEnabled = uiState.glowUpImageStatus == GlowUpImageStatus.COMPLETE
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Review Card", style = TextStyle(fontSize = 12.sp, color = GText))
                    }
                    Button(
                        onClick  = {},
                        enabled  = shareEnabled,
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor         = GRose,
                            disabledContainerColor = GMutedBg
                        )
                    ) {
                        Icon(Icons.Outlined.Share, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            if (shareEnabled) "Generate & Share" else "Image Generating…",
                            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }
        }
    }
}

// ── Before / After comparison card ───────────────────────────────────────────

@Composable
private fun BeforeAfterCard(
    originalImageUrl: String?,
    glowUpImageUrl: String?,
    glowUpImageStatus: GlowUpImageStatus,
    onRetry: () -> Unit
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
            Icon(Icons.Outlined.Info, null, tint = GMuted, modifier = Modifier.size(16.dp))
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
                        val newFraction = change.position.x / size.width.toFloat()
                        sliderFraction = newFraction.coerceIn(0.05f, 0.95f)
                    }
                }
        ) {
            // Before image (full width behind)
            if (originalImageUrl != null) {
                AsyncImage(
                    model            = originalImageUrl,
                    contentDescription = "Before",
                    contentScale     = ContentScale.Crop,
                    modifier         = Modifier.fillMaxSize()
                )
            } else {
                ImagePlaceholder("Before", Modifier.fillMaxSize())
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
                            ImagePlaceholder("After", Modifier.fillMaxSize())
                        }
                    }
                    GlowUpImageStatus.GENERATING -> GeneratingPlaceholder()
                    GlowUpImageStatus.FAILED     -> FailedPlaceholder(onRetry)
                    GlowUpImageStatus.PENDING    -> ShimmerPlaceholder()
                }
            }

            // Divider handle (only when COMPLETE)
            if (glowUpImageStatus == GlowUpImageStatus.COMPLETE) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = (sliderFraction * 1000).dp.coerceAtMost(10000.dp))
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(Color.White)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = (sliderFraction * 1000).dp.coerceAtMost(10000.dp))
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text("<>", style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GDark))
                }
            }

            // Before / After label pills
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) { Text("Before", style = TextStyle(fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.SemiBold)) }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(GRose.copy(alpha = 0.85f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) { Text("After (AI)", style = TextStyle(fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.SemiBold)) }
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
    Box(
        modifier = Modifier.fillMaxSize().background(GMutedBg),
        contentAlignment = Alignment.Center
    ) {
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
private fun ImagePlaceholder(label: String, modifier: Modifier) {
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
        animate(
            initialValue = 0f,
            targetValue  = score / 100f,
            animationSpec = tween(durationMillis = 600, easing = LinearEasing)
        ) { v, _ -> animatedProgress = v }
    }

    GlowCard {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Score ring
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
                // Delta badge
                if (scoreDelta != null) {
                    val (bgColor, fgColor, prefix) = when {
                        scoreDelta > 0  -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "+")
                        scoreDelta < 0  -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "")
                        else            -> Triple(GMutedBg, GMuted, "")
                    }
                    val text = when {
                        scoreDelta == 0 -> "Same as last scan"
                        else            -> "$prefix$scoreDelta vs last scan"
                    }
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

// ── Improvement area card ─────────────────────────────────────────────────────

@Composable
private fun ImprovementAreaCard(area: ImprovementArea, isSelected: Boolean, onClick: () -> Unit) {
    val (bgColor, borderColor, textColor) = when {
        isSelected              -> Triple(GRose.copy(alpha = 0.15f), GRose, GRose)
        area.impact == ImpactLevel.HIGH -> Triple(GRose.copy(alpha = 0.08f), GRose.copy(alpha = 0.3f), GText)
        else                    -> Triple(GMutedBg, Color.Transparent, GText)
    }
    val impactLabel = when (area.impact) {
        ImpactLevel.HIGH   -> "High Impact"
        ImpactLevel.MEDIUM -> "Medium Impact"
        ImpactLevel.LOW    -> "Low Impact"
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(area.area, style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textColor))
        Spacer(Modifier.height(2.dp))
        Text(impactLabel, style = TextStyle(fontSize = 10.sp, color = GMuted))
        if (area.scorePotential > 0) {
            Text("+${area.scorePotential} potential", style = TextStyle(fontSize = 10.sp, color = GRose))
        }
    }
}

// ── Step guide ────────────────────────────────────────────────────────────────

@Composable
private fun StepGuideContent(guide: StepGuide) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
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
        Icon(Icons.Outlined.CheckCircle, null, tint = GRose, modifier = Modifier.size(16.dp).padding(top = 1.dp))
        Text(text, style = TextStyle(fontSize = 11.sp, color = GText, lineHeight = 15.sp))
    }
}

// ── Progress chart ────────────────────────────────────────────────────────────

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

        // Grid lines + Y labels
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

        // Projected next point (dashed) — extend from last point
        if (dataPoints.size >= 2) {
            val delta = dataPoints.last().score - dataPoints[dataPoints.size - 2].score
            val projected = (dataPoints.last().score + delta.coerceIn(-20, 20)).coerceIn(0, 100)
            val projX = pts.last().x + xStep
            val projY = topPad + ch * (1f - projected / 100f)
            val dashPath = Path().apply { moveTo(pts.last().x, pts.last().y); lineTo(projX, projY) }
            drawPath(dashPath, GRose.copy(alpha = 0.4f), style = Stroke(1.5.dp.toPx()))
            drawCircle(GRose.copy(alpha = 0.5f), 4.dp.toPx(), Offset(projX, projY), style = Stroke(1.5.dp.toPx()))
        }

        // Data point dots
        pts.forEachIndexed { i, pt ->
            val isLatest = i == pts.lastIndex
            if (isLatest) {
                // Latest: open circle
                drawCircle(GRose, 5.dp.toPx(), pt, style = Stroke(2.dp.toPx()))
            } else {
                drawCircle(GRose, 4.dp.toPx(), pt)
                drawCircle(Color.White, 2.5.dp.toPx(), pt)
            }
        }
    }

    // X-axis date labels
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

// ── Share preview ─────────────────────────────────────────────────────────────

@Composable
private fun SharePreviewImage(url: String?, label: String, modifier: Modifier) {
    Box(
        modifier = modifier
            .height(80.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(GMutedBg),
        contentAlignment = Alignment.Center
    ) {
        if (url != null) {
            AsyncImage(
                model              = url,
                contentDescription = label,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize()
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.Person, null, tint = GMuted, modifier = Modifier.size(24.dp))
                Text(label, style = TextStyle(fontSize = 10.sp, color = GMuted))
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

// ── GlowCard container ────────────────────────────────────────────────────────

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
