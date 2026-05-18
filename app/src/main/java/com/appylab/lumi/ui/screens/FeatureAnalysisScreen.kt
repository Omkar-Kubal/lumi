package com.appylab.lumi.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appylab.lumi.data.model.FaceAnalysis

// ── Palette ───────────────────────────────────────────────────────────────────
private val FRose       = Color(0xFFFF637E)
private val FBackground = Color(0xFFFCFCFC)
private val FCardBg     = Color.White
private val FBorder     = Color(0xFFFFCCD3)
private val FText       = Color(0xFF0A0A0A)
private val FMuted      = Color(0xFF525252)
private val FMutedBg    = Color(0xFFF5F5F5)
private val FDark       = Color(0xFF0A0A0A)

// ── Static data ───────────────────────────────────────────────────────────────
private data class FeatureCard(val name: String, val label: String, val score: Int)

private val LANDMARK_TABS = listOf("Eyes", "Brows", "Nose", "Lips", "Face Contour")

private val IMPROVEMENT_LIST = listOf(
    Triple(1, "Undereye Area",    "High"),
    Triple(2, "Skin Texture",     "Medium"),
    Triple(3, "Brow Definition",  "Medium"),
    Triple(4, "Jawline Contour",  "Medium"),
    Triple(5, "Lip Definition",   "Low")
)

// Landmark dot positions (normalized 0-1 within face oval)
// Format: (xFraction, yFraction) relative to face bounding box
private val LANDMARK_POSITIONS = listOf(
    // Left eye region
    0.33f to 0.38f, 0.38f to 0.35f, 0.43f to 0.38f, 0.38f to 0.41f,
    // Right eye region
    0.57f to 0.38f, 0.62f to 0.35f, 0.67f to 0.38f, 0.62f to 0.41f,
    // Brows
    0.36f to 0.29f, 0.50f to 0.27f, 0.64f to 0.29f,
    // Nose
    0.50f to 0.52f, 0.44f to 0.60f, 0.56f to 0.60f,
    // Lips
    0.50f to 0.73f, 0.42f to 0.78f, 0.58f to 0.78f
)

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
internal fun FeatureAnalysisScreen(
    analysis: FaceAnalysis? = null,
    onBack: () -> Unit = {}
) {
    var selectedLandmarkTab by remember { mutableIntStateOf(0) }

    val featureCards = listOf(
        FeatureCard("Eyes",       analysis?.eyeShape?.toFeatureLabel() ?: "Balanced",      85),
        FeatureCard("Brows",      analysis?.browType?.toFeatureLabel() ?: "Ideal",         88),
        FeatureCard("Nose",       analysis?.noseShape?.toFeatureLabel() ?: "Proportioned", 82),
        FeatureCard("Lips",       analysis?.lipType?.toFeatureLabel() ?: "Balanced",       84),
        FeatureCard("Jawline",    "Well-Defined",                                          83),
        FeatureCard("Cheekbones", "Prominent",                                             87)
    )

    val faceShape = analysis?.faceShape?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Oval"
    val faceShapeDesc = analysis?.faceShapeDescription
        ?.takeIf { it.isNotEmpty() }
        ?: "Your face has excellent symmetry and proportioned features. Focus on enhancing your natural balance."

    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(FBackground),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = 8.dp, bottom = navBottom + 76.dp
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
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = FText, modifier = Modifier.size(22.dp))
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Feature Detail Analysis",
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = FText)
                    )
                    Text(
                        "In-depth analysis of your facial features",
                        style = TextStyle(fontSize = 11.sp, color = FMuted)
                    )
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Outlined.Share, "Share", tint = FText, modifier = Modifier.size(22.dp))
                }
            }
        }

        // ── Landmark Detection
        item {
            FCard(padding = 0.dp) {
                Column(modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)) {
                    Text("Landmark Detection", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = FText))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "We detected key landmarks to analyze your unique features and proportions.",
                        style = TextStyle(fontSize = 11.sp, color = FMuted, lineHeight = 16.sp)
                    )
                    Spacer(Modifier.height(14.dp))
                }

                // Face illustration with landmarks
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    LandmarkFaceIllustration(
                        modifier = Modifier
                            .size(180.dp)
                            .align(Alignment.Center),
                        highlightCategory = LANDMARK_TABS[selectedLandmarkTab]
                    )

                    // Landmarks badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp, end = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(FRose)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "16 landmarks detected",
                            style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Category tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedLandmarkTab,
                    containerColor = FCardBg,
                    contentColor = FRose,
                    edgePadding = 16.dp,
                    divider = {}
                ) {
                    LANDMARK_TABS.forEachIndexed { i, name ->
                        Tab(
                            selected = selectedLandmarkTab == i,
                            onClick = { selectedLandmarkTab = i },
                            text = {
                                Text(
                                    name,
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedLandmarkTab == i) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (selectedLandmarkTab == i) FRose else FMuted
                                    )
                                )
                            }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        // ── Feature Analysis grid (2 columns × 3 rows)
        item {
            FCard {
                Text("Feature Analysis", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = FText))
                Spacer(Modifier.height(12.dp))
                featureCards.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        pair.forEach { card ->
                            FeatureScoreCard(card = card, modifier = Modifier.weight(1f))
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }
        }

        // ── Symmetry Score
        item { SymmetryScoreCard() }

        // ── Improvement Priority
        item {
            FCard {
                Text("Improvement Priority", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = FText))
                Spacer(Modifier.height(12.dp))
                IMPROVEMENT_LIST.forEach { (rank, area, priority) ->
                    ImprovementRow(rank = rank, area = area, priority = priority)
                    if (rank < IMPROVEMENT_LIST.size) Spacer(Modifier.height(8.dp))
                }
            }
        }

        // ── Face Shape
        item {
            FCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Reuse FaceShapeIllustration pattern inline
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(FMutedBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(36.dp)) {
                            drawOval(
                                color = FRose,
                                topLeft = Offset(size.width * 0.15f, size.height * 0.04f),
                                size = Size(size.width * 0.70f, size.height * 0.92f),
                                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(faceShape, style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = FText))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFE8F5E9))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "Well Balanced",
                                    style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7D32))
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(faceShapeDesc, style = TextStyle(fontSize = 11.sp, color = FMuted, lineHeight = 16.sp))
                    }
                }
            }
        }

    }
}

// ── Feature Score card ────────────────────────────────────────────────────────

@Composable
private fun FeatureScoreCard(card: FeatureCard, modifier: Modifier = Modifier) {
    var animatedScore by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(card.score) {
        animate(
            initialValue = 0f,
            targetValue = card.score / 100f,
            animationSpec = tween(durationMillis = 600, easing = LinearEasing)
        ) { v, _ -> animatedScore = v }
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(FMutedBg)
            .padding(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(card.name, style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = FMuted))
        Spacer(Modifier.height(4.dp))
        Text(card.label, style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FText))
        Spacer(Modifier.height(8.dp))

        // Animated score bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedScore)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(FRose)
            )
        }

        Spacer(Modifier.height(6.dp))
        Text("Score", style = TextStyle(fontSize = 9.sp, color = FMuted))
        Text("${card.score}/100", style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = FText))
    }
}

// ── Symmetry Score card ───────────────────────────────────────────────────────

@Composable
private fun SymmetryScoreCard() {
    val score = 87
    var animatedProgress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(score) {
        animate(
            initialValue = 0f,
            targetValue = score / 100f,
            animationSpec = tween(durationMillis = 800, easing = LinearEasing)
        ) { v, _ -> animatedProgress = v }
    }

    FCard {
        Text("Symmetry Score", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = FText))
        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Animated ring
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                Canvas(modifier = Modifier.size(80.dp)) {
                    val stroke = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    drawArc(color = FMutedBg, startAngle = -90f, sweepAngle = 360f, useCenter = false, style = stroke)
                    drawArc(color = FRose, startAngle = -90f, sweepAngle = 360f * animatedProgress, useCenter = false, style = stroke)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$score", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = FText))
                    Text("/100", style = TextStyle(fontSize = 8.sp, color = FMuted))
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text("Very Good", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FText))
                Spacer(Modifier.height(8.dp))

                // 4-level scale
                val levels = listOf("Low", "Average", "High", "Excellent")
                val activeLevel = 2 // "High"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    levels.forEachIndexed { i, level ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (i <= activeLevel) FRose else FMutedBg)
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                level,
                                style = TextStyle(
                                    fontSize = 8.sp,
                                    color = if (i == activeLevel) FRose else FMuted,
                                    fontWeight = if (i == activeLevel) FontWeight.SemiBold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    "Your face has excellent symmetry with well-balanced and proportioned features.",
                    style = TextStyle(fontSize = 10.sp, color = FMuted, lineHeight = 14.sp)
                )
            }
        }
    }
}

// ── Improvement row ───────────────────────────────────────────────────────────

@Composable
private fun ImprovementRow(rank: Int, area: String, priority: String) {
    val (badgeColor, textColor) = when (priority) {
        "High"   -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        "Medium" -> Color(0xFFFFF8E1) to Color(0xFFF57F17)
        else     -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Rank badge
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(FMutedBg),
            contentAlignment = Alignment.Center
        ) {
            Text("$rank", style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FText))
        }

        Text(area, modifier = Modifier.weight(1f), style = TextStyle(fontSize = 12.sp, color = FText))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(badgeColor)
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(priority, style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = textColor))
        }
    }
}

// ── Landmark face illustration ────────────────────────────────────────────────

@Composable
private fun LandmarkFaceIllustration(
    modifier: Modifier = Modifier,
    highlightCategory: String = "Eyes"
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)

        // Face oval
        drawOval(
            color = FRose.copy(alpha = 0.5f),
            topLeft = Offset(w * 0.15f, h * 0.04f),
            size = Size(w * 0.70f, h * 0.92f),
            style = stroke
        )

        // Landmark dots — highlight relevant ones based on category
        val highlightIndices = when (highlightCategory) {
            "Eyes"         -> 0..7
            "Brows"        -> 8..10
            "Nose"         -> 11..13
            "Lips"         -> 14..16
            "Face Contour" -> (0..16)
            else           -> (0..16)
        }

        LANDMARK_POSITIONS.forEachIndexed { i, (xFrac, yFrac) ->
            val cx = w * 0.15f + w * 0.70f * xFrac
            val cy = h * 0.04f + h * 0.92f * yFrac
            val isHighlighted = i in highlightIndices

            drawCircle(
                color = if (isHighlighted) FRose else FMuted.copy(alpha = 0.35f),
                radius = if (isHighlighted) 4.dp.toPx() else 3.dp.toPx(),
                center = Offset(cx, cy)
            )
            if (isHighlighted) {
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = Offset(cx, cy)
                )
            }
        }
    }
}

// ── Shared card wrapper ───────────────────────────────────────────────────────

@Composable
private fun FCard(
    modifier: Modifier = Modifier,
    padding: androidx.compose.ui.unit.Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FCardBg)
            .border(1.dp, FBorder, RoundedCornerShape(12.dp))
            .padding(padding)
    ) {
        content()
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun String.toFeatureLabel(): String =
    this.replace("_", " ").lowercase()
        .split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
        .ifEmpty { "—" }
