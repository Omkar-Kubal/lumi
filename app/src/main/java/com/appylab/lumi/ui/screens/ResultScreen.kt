package com.appylab.lumi.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Share
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appylab.lumi.data.model.CelebrityMatch
import com.appylab.lumi.data.model.FaceAnalysis
import com.appylab.lumi.data.model.GlowUpPotential
import com.appylab.lumi.data.model.ResultError
import com.appylab.lumi.data.model.SubscriptionTier
import com.appylab.lumi.ui.viewmodel.ResultViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Palette ──────────────────────────────────────────────────────────────────
private val RRose       = Color(0xFFFF637E)
private val RBackground = Color(0xFFFCFCFC)
private val RCard       = Color.White
private val RBorder     = Color(0xFFFFCCD3)
private val RText       = Color(0xFF0A0A0A)
private val RMuted      = Color(0xFF525252)
private val RMutedBg    = Color(0xFFF5F5F5)
private val RDark       = Color(0xFF0A0A0A)

// ── Sub-screen navigation ─────────────────────────────────────────────────────
private sealed class ResultSubScreen {
    object GlowUp          : ResultSubScreen()
    object ColorAnalysis   : ResultSubScreen()
    object FeatureAnalysis : ResultSubScreen()
}

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun ResultScreen(
    viewModel: ResultViewModel = viewModel(),
    onBack: () -> Unit = {},
    onRescan: () -> Unit = {},
    onPaywall: (context: String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val appContext = LocalContext.current
    var subScreen by remember { mutableStateOf<ResultSubScreen?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RBackground)
    ) {
        val sub = subScreen
        if (sub != null) {
            when (sub) {
                ResultSubScreen.GlowUp -> GlowUpResultScreen(
                    faceAnalysisId = uiState.analysis?.id ?: 0L,
                    onBack     = { subScreen = null },
                    onPaywall  = { onPaywall("glow_up") }
                )
                ResultSubScreen.ColorAnalysis -> ColorAnalysisScreen(
                    skinTone = uiState.analysis?.skinTone.orEmpty(),
                    undertone = uiState.analysis?.undertone.orEmpty(),
                    onBack = { subScreen = null }
                )
                ResultSubScreen.FeatureAnalysis -> FeatureAnalysisScreen(
                    analysis = uiState.analysis,
                    onBack = { subScreen = null }
                )
            }
        } else {
            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = RRose)
                    }
                }

                uiState.error == ResultError.NotFound -> {
                    ResultEmptyState(onStartScan = onRescan)
                }

                uiState.error == ResultError.LoadFailed -> {
                    ResultErrorState(onRescan = onRescan)
                }

                uiState.analysis != null -> {
                    ResultContent(
                        analysis = uiState.analysis!!,
                        subscriptionTier = uiState.subscriptionTier,
                        glowUpPotential = uiState.glowUpPotential,
                        verdictLabel = uiState.verdictLabel,
                        verdictBody = uiState.verdictBody,
                        isGeneratingShare = uiState.isGeneratingShareCard,
                        onBack = onBack,
                        onRescan = onRescan,
                        onPaywall = onPaywall,
                        onShare = { viewModel.shareResult(appContext) },
                        onViewGlowUp = { subScreen = ResultSubScreen.GlowUp },
                        onViewColorAnalysis = { subScreen = ResultSubScreen.ColorAnalysis },
                        onViewFeatureAnalysis = { subScreen = ResultSubScreen.FeatureAnalysis }
                    )
                }

                else -> ResultEmptyState(onStartScan = onRescan)
            }
        }
    }
}

// ── Loaded state ──────────────────────────────────────────────────────────────

@Composable
private fun ResultContent(
    analysis: FaceAnalysis,
    subscriptionTier: SubscriptionTier,
    glowUpPotential: GlowUpPotential,
    verdictLabel: String,
    verdictBody: String,
    isGeneratingShare: Boolean,
    onBack: () -> Unit,
    onRescan: () -> Unit,
    onPaywall: (String) -> Unit,
    onShare: () -> Unit,
    onViewGlowUp: () -> Unit,
    onViewColorAnalysis: () -> Unit,
    onViewFeatureAnalysis: () -> Unit
) {
    val isFree = subscriptionTier == SubscriptionTier.FREE
    val formattedDate = remember(analysis.timestamp) {
        SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
            .format(Date(analysis.timestamp))
    }

    var showRescanDialog by remember { mutableStateOf(false) }
    var showLearnMoreShape by remember { mutableStateOf<String?>(null) }

    if (showRescanDialog) {
        RescanConfirmDialog(
            onConfirm = { showRescanDialog = false; onRescan() },
            onDismiss = { showRescanDialog = false }
        )
    }

    if (showLearnMoreShape != null) {
        FaceShapeLearnMoreSheet(
            shape = showLearnMoreShape!!,
            onDismiss = { showLearnMoreShape = null }
        )
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            // Fixed top bar
            ResultTopBar(
                dateText = "Scanned on $formattedDate",
                onBack = onBack,
                onShare = onShare
            )

            // Scrollable content
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = 12.dp, bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // 1. Upsell banner (FREE only)
                if (isFree) {
                    item { UpsellBannerCard(onUpgrade = { onPaywall("results_banner") }) }
                }

                // 2. Face Shape + Skin Tone row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FaceShapeCard(
                            shape = analysis.faceShape,
                            description = analysis.faceShapeDescription,
                            modifier = Modifier.weight(1f),
                            onLearnMore = { showLearnMoreShape = analysis.faceShape }
                        )
                        SkinToneCard(
                            skinTone = analysis.skinTone,
                            undertone = analysis.undertone,
                            undertoneDescription = analysis.undertoneDescription,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 3. Color Analysis teaser
                item { ColorAnalysisTeaserCard(onClick = onViewColorAnalysis) }

                // 4. Eyes & Features
                item {
                    EyesFeaturesCard(
                        eyeShape = analysis.eyeShape,
                        browType = analysis.browType,
                        noseShape = analysis.noseShape,
                        lipType = analysis.lipType,
                        onDetailClick = onViewFeatureAnalysis
                    )
                }

                // 5. Feature lock banner (FREE only)
                if (isFree) {
                    item { FeatureLockBannerCard(onUpgrade = { onPaywall("feature_analysis") }) }
                }

                // 6. Celebrity Lookalikes (only if data present)
                if (analysis.celebrityMatches.isNotEmpty()) {
                    item {
                        CelebrityLookalikesCard(
                            matches = analysis.celebrityMatches,
                            onPremiumClick = { onPaywall("celebrity") }
                        )
                    }
                }

                // 7. Score + Glow-Up row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OverallScoreCard(
                            score = analysis.glowUpScore,
                            verdictLabel = verdictLabel,
                            verdictBody = verdictBody,
                            modifier = Modifier.weight(1f)
                        )
                        GlowUpPotentialCard(
                            potential = glowUpPotential,
                            modifier = Modifier.weight(1f),
                            onViewFullReport = onViewGlowUp
                        )
                    }
                }

                // 8. Share card
                item {
                    ShareResultCard(
                        onShare = onShare,
                        isGenerating = isGeneratingShare
                    )
                }
            }
        }

        // Sticky Re-scan bar — sits above the shared bottom nav bar
        RescanBar(
            onClick = { showRescanDialog = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 60.dp)
        )
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun ResultTopBar(
    dateText: String,
    onBack: () -> Unit,
    onShare: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RBackground)
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = RText, modifier = Modifier.size(22.dp))
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Your Results",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = RText)
            )
            Text(
                dateText,
                style = TextStyle(fontSize = 11.sp, color = RMuted)
            )
        }

        IconButton(onClick = onShare) {
            Icon(Icons.Outlined.Share, "Share", tint = RText, modifier = Modifier.size(22.dp))
        }
    }
}

// ── 1. Upsell banner ─────────────────────────────────────────────────────────

@Composable
private fun UpsellBannerCard(onUpgrade: () -> Unit) {
    ResultCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(RMutedBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Lock, null, tint = RText, modifier = Modifier.size(20.dp))
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Unlock your full analysis",
                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = RText)
                )
                Text(
                    "Get detailed insights, personalized recommendations & more.",
                    style = TextStyle(fontSize = 11.sp, color = RMuted, lineHeight = 16.sp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = onUpgrade,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RDark),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("Upgrade Now", style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White))
                }
                Text("7-day free trial", style = TextStyle(fontSize = 10.sp, color = RMuted))
            }
        }
    }
}

// ── 2a. Face Shape card ───────────────────────────────────────────────────────

@Composable
private fun FaceShapeCard(
    shape: String,
    description: String,
    modifier: Modifier = Modifier,
    onLearnMore: () -> Unit
) {
    ResultCard(modifier = modifier) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Face Shape", style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = RMuted))
            if (shape.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(RRose)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("Detected", style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color.White))
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Face shape illustration
        FaceShapeIllustration(
            shape = shape,
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(8.dp))

        Text(
            shape.lowercase().replaceFirstChar { it.uppercase() }.ifEmpty { "—" },
            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = RText),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        if (description.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                description,
                style = TextStyle(fontSize = 11.sp, color = RMuted, lineHeight = 16.sp)
            )
        }

        Spacer(Modifier.height(10.dp))

        TextButton(
            onClick = onLearnMore,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("Learn more", style = TextStyle(fontSize = 12.sp, color = RRose, fontWeight = FontWeight.SemiBold))
        }
    }
}

// ── Face shape Canvas illustration ───────────────────────────────────────────

@Composable
private fun FaceShapeIllustration(shape: String, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        val color = RRose

        when (shape.uppercase()) {
            "OVAL" -> drawOval(
                color = color,
                topLeft = Offset(w * 0.15f, h * 0.04f),
                size = Size(w * 0.70f, h * 0.92f),
                style = stroke
            )
            "ROUND" -> drawOval(
                color = color,
                topLeft = Offset(w * 0.08f, h * 0.08f),
                size = Size(w * 0.84f, h * 0.84f),
                style = stroke
            )
            "SQUARE" -> {
                val path = Path().apply {
                    moveTo(w * 0.20f, h * 0.06f)
                    lineTo(w * 0.80f, h * 0.06f)
                    lineTo(w * 0.88f, h * 0.50f)
                    lineTo(w * 0.80f, h * 0.94f)
                    lineTo(w * 0.20f, h * 0.94f)
                    lineTo(w * 0.12f, h * 0.50f)
                    close()
                }
                drawPath(path, color, style = stroke)
            }
            "HEART" -> {
                val path = Path().apply {
                    moveTo(w * 0.50f, h * 0.92f)
                    cubicTo(w * 0.05f, h * 0.60f, w * 0.05f, h * 0.20f, w * 0.30f, h * 0.12f)
                    cubicTo(w * 0.42f, h * 0.06f, w * 0.50f, h * 0.20f, w * 0.50f, h * 0.28f)
                    cubicTo(w * 0.50f, h * 0.20f, w * 0.58f, h * 0.06f, w * 0.70f, h * 0.12f)
                    cubicTo(w * 0.95f, h * 0.20f, w * 0.95f, h * 0.60f, w * 0.50f, h * 0.92f)
                }
                drawPath(path, color, style = stroke)
            }
            "OBLONG" -> drawOval(
                color = color,
                topLeft = Offset(w * 0.22f, h * 0.02f),
                size = Size(w * 0.56f, h * 0.96f),
                style = stroke
            )
            "DIAMOND" -> {
                val path = Path().apply {
                    moveTo(w * 0.50f, h * 0.04f)
                    lineTo(w * 0.90f, h * 0.40f)
                    lineTo(w * 0.50f, h * 0.96f)
                    lineTo(w * 0.10f, h * 0.40f)
                    close()
                }
                drawPath(path, color, style = stroke)
            }
            "TRIANGLE" -> {
                val path = Path().apply {
                    moveTo(w * 0.50f, h * 0.06f)
                    cubicTo(w * 0.34f, h * 0.06f, w * 0.14f, h * 0.58f, w * 0.10f, h * 0.88f)
                    lineTo(w * 0.90f, h * 0.88f)
                    cubicTo(w * 0.86f, h * 0.58f, w * 0.66f, h * 0.06f, w * 0.50f, h * 0.06f)
                    close()
                }
                drawPath(path, color, style = stroke)
            }
            else -> drawOval(
                color = color,
                topLeft = Offset(w * 0.15f, h * 0.04f),
                size = Size(w * 0.70f, h * 0.92f),
                style = stroke
            )
        }
    }
}

// ── 2b. Skin Tone card ────────────────────────────────────────────────────────

private val SKIN_TONE_SWATCHES = listOf(
    "FAIR"   to Color(0xFFF5DCCA),
    "LIGHT"  to Color(0xFFE8C4A0),
    "MEDIUM" to Color(0xFFC68642),
    "TAN"    to Color(0xFF8D5524),
    "DEEP"   to Color(0xFF4A2912)
)

@Composable
private fun SkinToneCard(
    skinTone: String,
    undertone: String,
    undertoneDescription: String,
    modifier: Modifier = Modifier
) {
    val activeIndex = SKIN_TONE_SWATCHES.indexOfFirst { it.first == skinTone.uppercase() }

    ResultCard(modifier = modifier) {
        Text("Skin Tone & Undertone", style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = RMuted))

        Spacer(Modifier.height(12.dp))

        // Swatch row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SKIN_TONE_SWATCHES.forEachIndexed { i, (_, color) ->
                val isActive = i == activeIndex
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(color)
                        .then(
                            if (isActive) Modifier.border(2.dp, RRose, CircleShape)
                            else Modifier
                        )
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(
            skinTone.lowercase().replaceFirstChar { it.uppercase() }.ifEmpty { "—" },
            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RText)
        )

        Text(
            undertone.lowercase().replaceFirstChar { it.uppercase() }.let {
                if (it.isNotEmpty()) "$it Undertone" else "—"
            },
            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = RText)
        )

        if (undertoneDescription.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                undertoneDescription,
                style = TextStyle(fontSize = 11.sp, color = RMuted, lineHeight = 15.sp)
            )
        }

        Spacer(Modifier.height(8.dp))

        if (undertone.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(RMutedBg)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    undertone.lowercase().replaceFirstChar { it.uppercase() },
                    style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = RText)
                )
            }
        }
    }
}

// ── 3. Eyes & Features ───────────────────────────────────────────────────────

private data class FeatureChip(val label: String, val value: String)

@Composable
private fun EyesFeaturesCard(
    eyeShape: String,
    browType: String,
    noseShape: String,
    lipType: String,
    onDetailClick: () -> Unit
) {
    val chips = listOf(
        FeatureChip("Eye Shape", eyeShape.formatFeature()),
        FeatureChip("Brows",     browType.formatFeature()),
        FeatureChip("Nose Shape", noseShape.formatFeature()),
        FeatureChip("Lips",      lipType.formatFeature())
    )

    ResultCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Eyes & Features", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = RText))
            IconButton(onClick = onDetailClick, modifier = Modifier.size(32.dp)) {
                Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, "Details", tint = RMuted, modifier = Modifier.size(14.dp))
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            chips.forEach { chip ->
                FeatureChipItem(chip = chip, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun FeatureChipItem(chip: FeatureChip, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(RMutedBg)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FeatureIcon(label = chip.label)
        Spacer(Modifier.height(6.dp))
        Text(chip.label, style = TextStyle(fontSize = 9.sp, color = RMuted, textAlign = TextAlign.Center))
        Text(chip.value, style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = RText, textAlign = TextAlign.Center))
    }
}

@Composable
private fun FeatureIcon(label: String) {
    Canvas(modifier = Modifier.size(28.dp)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        val c = RMuted

        when {
            label.contains("Eye", ignoreCase = true) -> {
                // Almond eye shape
                val path = Path().apply {
                    moveTo(w * 0.10f, h * 0.50f)
                    cubicTo(w * 0.30f, h * 0.20f, w * 0.70f, h * 0.20f, w * 0.90f, h * 0.50f)
                    cubicTo(w * 0.70f, h * 0.80f, w * 0.30f, h * 0.80f, w * 0.10f, h * 0.50f)
                }
                drawPath(path, c, style = stroke)
                drawCircle(c, radius = w * 0.12f, center = Offset(w * 0.50f, h * 0.50f))
            }
            label.contains("Brow", ignoreCase = true) -> {
                // Arch brow
                val path = Path().apply {
                    moveTo(w * 0.10f, h * 0.65f)
                    cubicTo(w * 0.30f, h * 0.30f, w * 0.65f, h * 0.25f, w * 0.90f, h * 0.45f)
                }
                drawPath(path, c, style = stroke)
            }
            label.contains("Nose", ignoreCase = true) -> {
                // Simple nose outline
                val path = Path().apply {
                    moveTo(w * 0.50f, h * 0.10f)
                    lineTo(w * 0.50f, h * 0.65f)
                    cubicTo(w * 0.50f, h * 0.80f, w * 0.25f, h * 0.85f, w * 0.20f, h * 0.80f)
                    moveTo(w * 0.50f, h * 0.65f)
                    cubicTo(w * 0.50f, h * 0.80f, w * 0.75f, h * 0.85f, w * 0.80f, h * 0.80f)
                }
                drawPath(path, c, style = stroke)
            }
            else -> {
                // Lips
                val upper = Path().apply {
                    moveTo(w * 0.12f, h * 0.48f)
                    cubicTo(w * 0.25f, h * 0.28f, w * 0.42f, h * 0.32f, w * 0.50f, h * 0.48f)
                    cubicTo(w * 0.58f, h * 0.32f, w * 0.75f, h * 0.28f, w * 0.88f, h * 0.48f)
                }
                val lower = Path().apply {
                    moveTo(w * 0.12f, h * 0.48f)
                    cubicTo(w * 0.25f, h * 0.78f, w * 0.75f, h * 0.78f, w * 0.88f, h * 0.48f)
                }
                drawPath(upper, c, style = stroke)
                drawPath(lower, c, style = stroke)
            }
        }
    }
}

private fun String.formatFeature(): String =
    this.replace("_", " ").lowercase()
        .split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
        .ifEmpty { "—" }

// ── 4. Feature lock banner ───────────────────────────────────────────────────

@Composable
private fun FeatureLockBannerCard(onUpgrade: () -> Unit) {
    ResultCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(RMutedBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Lock, null, tint = RText, modifier = Modifier.size(18.dp))
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Unlock detailed feature analysis",
                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = RText)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Get in-depth measurements, symmetry score, and personalized improvement tips.",
                    style = TextStyle(fontSize = 11.sp, color = RMuted, lineHeight = 16.sp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onUpgrade,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RDark)
        ) {
            Text("Upgrade Now", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White))
        }
    }
}

// ── 5. Celebrity Lookalikes ───────────────────────────────────────────────────

@Composable
private fun CelebrityLookalikesCard(
    matches: List<CelebrityMatch>,
    onPremiumClick: () -> Unit
) {
    ResultCard {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Celebrity Lookalikes", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = RText))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(RDark)
                    .clickable { onPremiumClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Outlined.Lock, null, tint = Color.White, modifier = Modifier.size(10.dp))
                Spacer(Modifier.width(4.dp))
                Text("Premium", style = TextStyle(fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.SemiBold))
                Spacer(Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, null, tint = Color.White, modifier = Modifier.size(10.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            matches.take(3).forEach { match ->
                CelebritySlot(match = match)
            }
        }
    }
}

@Composable
private fun CelebritySlot(match: CelebrityMatch) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box {
            // Avatar placeholder circle
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(RMutedBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    match.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                    style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = RMuted)
                )
            }
            // Rank badge
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(RDark)
                    .align(Alignment.TopStart),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${match.rank}",
                    style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(match.name, style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = RText, textAlign = TextAlign.Center))
        Text("${match.similarityPct}% match", style = TextStyle(fontSize = 10.sp, color = RMuted))
    }
}

// ── 6a. Overall Score card ───────────────────────────────────────────────────

@Composable
private fun OverallScoreCard(
    score: Int,
    verdictLabel: String,
    verdictBody: String,
    modifier: Modifier = Modifier
) {
    var animatedProgress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(score) {
        animate(
            initialValue = 0f,
            targetValue = score / 100f,
            animationSpec = tween(durationMillis = 600, easing = LinearEasing)
        ) { value, _ -> animatedProgress = value }
    }

    ResultCard(modifier = modifier) {
        Text("Overall Score", style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = RMuted))
        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Score ring
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(86.dp)) {
                Canvas(modifier = Modifier.size(86.dp)) {
                    val stroke = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    drawArc(
                        color = RMutedBg,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = stroke
                    )
                    drawArc(
                        color = RRose,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = stroke
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$score", style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = RText))
                    Text("/100", style = TextStyle(fontSize = 9.sp, color = RMuted))
                }
            }

            Spacer(Modifier.width(10.dp))

            Column {
                Text(verdictLabel, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RText))
                Spacer(Modifier.height(2.dp))
                Text(verdictBody, style = TextStyle(fontSize = 10.sp, color = RMuted, lineHeight = 14.sp))
            }
        }
    }
}

// ── 6b. Glow-Up Potential card ───────────────────────────────────────────────

@Composable
private fun GlowUpPotentialCard(
    potential: GlowUpPotential,
    modifier: Modifier = Modifier,
    onViewFullReport: () -> Unit = {}
) {
    val filledCount = when (potential) { GlowUpPotential.HIGH -> 4; GlowUpPotential.MEDIUM -> 3; GlowUpPotential.LOW -> 1 }
    val badgeLabel = when (potential) { GlowUpPotential.HIGH -> "High"; GlowUpPotential.MEDIUM -> "Medium"; GlowUpPotential.LOW -> "Low" }
    val description = when (potential) {
        GlowUpPotential.HIGH   -> "With a few personalized tips, your glow-up potential is excellent!"
        GlowUpPotential.MEDIUM -> "You have solid potential — personalized tips will make a big difference."
        GlowUpPotential.LOW    -> "Great foundation — consistent care will bring out your natural glow."
    }

    var visibleSegments by remember { mutableIntStateOf(0) }
    LaunchedEffect(potential) {
        visibleSegments = 0
        for (i in 1..filledCount) {
            kotlinx.coroutines.delay(80L)
            visibleSegments = i
        }
    }

    ResultCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Glow-Up\nPotential", style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = RMuted, lineHeight = 15.sp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(RRose)
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(badgeLabel, style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.White))
            }
        }

        Spacer(Modifier.height(12.dp))

        // Segmented bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (i in 1..5) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (i <= visibleSegments) RRose else RMutedBg)
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(description, style = TextStyle(fontSize = 10.sp, color = RMuted, lineHeight = 14.sp))

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = onViewFullReport,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("See Full Report", style = TextStyle(fontSize = 12.sp, color = RRose, fontWeight = FontWeight.SemiBold))
        }
    }
}

// ── 8. Share card ─────────────────────────────────────────────────────────────

@Composable
private fun ShareResultCard(onShare: () -> Unit, isGenerating: Boolean) {
    ResultCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(RMutedBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Share, null, tint = RText, modifier = Modifier.size(20.dp))
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("Share your result", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = RText))
                Text("Create and share your stylish result card", style = TextStyle(fontSize = 11.sp, color = RMuted))
            }

            Spacer(Modifier.width(8.dp))

            Button(
                onClick = onShare,
                enabled = !isGenerating,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RDark),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Outlined.Share, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Share Image", style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White))
                }
            }
        }
    }
}

// ── Re-scan sticky bar ────────────────────────────────────────────────────────

@Composable
private fun RescanBar(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = RDark,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.CameraAlt, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Re-scan", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White))
                Text("Scan again to track your progress", style = TextStyle(fontSize = 11.sp, color = Color.White.copy(alpha = 0.65f)))
            }
        }
    }
}

// ── Dialogs & sheets ─────────────────────────────────────────────────────────

@Composable
private fun RescanConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Start a new scan?") },
        text = { Text("This will replace your current results. Your previous scan will be saved in history.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = RRose)
            ) { Text("Start Scan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FaceShapeLearnMoreSheet(shape: String, onDismiss: () -> Unit) {
    val info = FACE_SHAPE_INFO[shape.uppercase()]

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FaceShapeIllustration(shape = shape, modifier = Modifier.size(48.dp))
                Spacer(Modifier.width(12.dp))
                Text(
                    info?.first ?: "${shape.lowercase().replaceFirstChar { it.uppercase() }} Face",
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = RText)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                info?.second ?: "Balanced, versatile features that complement many styles.",
                style = TextStyle(fontSize = 14.sp, color = RMuted, lineHeight = 22.sp)
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RDark)
            ) { Text("Got it") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComingSoonSheet(onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Outlined.CheckCircle, null, tint = RRose, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(12.dp))
            Text("Coming soon", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = RText))
            Spacer(Modifier.height(8.dp))
            Text(
                "Detailed eye, brow, nose & lip analysis is on its way.",
                style = TextStyle(fontSize = 14.sp, color = RMuted, textAlign = TextAlign.Center, lineHeight = 20.sp)
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RDark)
            ) { Text("OK") }
        }
    }
}

// ── Empty & error states ──────────────────────────────────────────────────────

@Composable
private fun ResultEmptyState(onStartScan: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Canvas(modifier = Modifier.size(96.dp)) {
            val stroke = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            drawOval(color = RRose, topLeft = Offset(size.width * 0.15f, size.height * 0.04f),
                size = Size(size.width * 0.70f, size.height * 0.92f), style = stroke)
        }
        Spacer(Modifier.height(24.dp))
        Text("No results yet", style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = RText))
        Spacer(Modifier.height(8.dp))
        Text(
            "Complete your first scan to see your personalised analysis.",
            style = TextStyle(fontSize = 14.sp, color = RMuted, textAlign = TextAlign.Center, lineHeight = 20.sp)
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = onStartScan,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RRose),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start your scan", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White))
        }
    }
}

@Composable
private fun ResultErrorState(onRescan: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Something went wrong", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = RText))
        Spacer(Modifier.height(8.dp))
        Text(
            "We couldn't load your results. Try scanning again.",
            style = TextStyle(fontSize = 14.sp, color = RMuted, textAlign = TextAlign.Center)
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onRescan,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RRose),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Re-scan", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White))
        }
    }
}

// ── Color Analysis teaser ─────────────────────────────────────────────────────

@Composable
private fun ColorAnalysisTeaserCard(onClick: () -> Unit) {
    ResultCard(modifier = Modifier.clickable { onClick() }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mini palette preview — 4 color circles
            Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                listOf(Color(0xFFD4A5A5), Color(0xFF8AA8C8), Color(0xFF7DB8A8), Color(0xFFA3A0B8)).forEach { c ->
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(c)
                            .border(1.5.dp, Color.White, CircleShape)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("Color Analysis", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = RText))
                Text("Discover your most flattering colours", style = TextStyle(fontSize = 11.sp, color = RMuted))
            }

            Icon(
                Icons.AutoMirrored.Outlined.ArrowForwardIos, null,
                tint = RMuted, modifier = Modifier.size(14.dp)
            )
        }
    }
}

// ── Shared card container ─────────────────────────────────────────────────────

@Composable
private fun ResultCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RCard)
            .border(1.dp, RBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        content()
    }
}

// ── Static face shape info (bundled copy) ─────────────────────────────────────

private val FACE_SHAPE_INFO: Map<String, Pair<String, String>> = mapOf(
    "OVAL"     to ("Oval Face" to "Oval faces have balanced proportions with slightly rounded features. This shape is considered the most versatile — most hairstyles and makeup looks complement it naturally."),
    "ROUND"    to ("Round Face" to "Round faces have equal width and height with soft, curved lines and full cheeks. Contouring and elongating styles create beautiful definition."),
    "SQUARE"   to ("Square Face" to "Square faces have a strong, angular jawline with a wide forehead and jaw of similar width. Soft, layered styles and gentle contouring can soften the angles beautifully."),
    "HEART"    to ("Heart Face" to "Heart-shaped faces are wider at the forehead and taper to a narrow chin. Styles that add width at the jaw and balance the forehead work best."),
    "OBLONG"   to ("Oblong Face" to "Oblong faces are longer than wide with balanced, even features. Volume at the sides and width-adding styles help create a more balanced appearance."),
    "DIAMOND"  to ("Diamond Face" to "Diamond faces have prominent, wide cheekbones with a narrow forehead and jawline. Styles that add width at the temples and jaw complement this striking shape."),
    "TRIANGLE" to ("Triangle Face" to "Triangle faces are wider at the jaw and taper toward a narrower forehead. Styles that add volume and width at the top create beautiful balance.")
)
