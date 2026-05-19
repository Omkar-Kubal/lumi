package com.appylab.lumi.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import android.graphics.Typeface
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ScrollableTabRow
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appylab.lumi.ui.viewmodel.FeatureDetailViewModel
import org.json.JSONArray
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.appylab.lumi.data.model.FaceAnalysis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import com.appylab.lumi.ui.theme.PoppinsFont

// ── Palette ───────────────────────────────────────────────────────────────────
private val FRose       = Color(0xFFFF637E)
private val FBackground = Color(0xFFFCFCFC)
private val FCardBg     = Color.White
private val FBorder     = Color(0xFFFFCCD3)
private val FText       = Color(0xFF0A0A0A)
private val FMuted      = Color(0xFF525252)
private val FMutedBg    = Color(0xFFF5F5F5)
private val FPositiveBg = Color(0xFFE8F5E9)
private val FPositive   = Color(0xFF2E7D32)
private val FNeutralBg  = Color(0xFFE3F2FD)
private val FNeutral    = Color(0xFF1565C0)

// ── Data models ───────────────────────────────────────────────────────────────
private enum class FeatureTab { OVERVIEW, EYES, BROWS, NOSE, LIPS, JAWLINE, CHEEKBONES }

private data class FeatureDetail(
    val tab: FeatureTab,
    val typeLabel: String,
    val label: String,
    val status: String,
    val description: String,
    val tips: String,
    val tipsLabel: String = "Enhancement Tips"
)

private data class ImprovementItem(val rank: Int, val area: String, val action: String)

// ── Static data ───────────────────────────────────────────────────────────────
private val FEATURE_TAB_LABELS = listOf("Overview", "Eyes", "Brows", "Nose", "Lips", "Jawline", "Cheekbones")

private val LEGEND_ITEMS = listOf(
    Color(0xFFFF637E) to "Eyes",
    Color(0xFF9C27B0) to "Brows",
    Color(0xFF2196F3) to "Nose",
    Color(0xFFE53935) to "Lips",
    Color(0xFF43A047) to "Face Contour"
)

private val LANDMARK_POSITIONS = listOf(
    0.33f to 0.38f, 0.38f to 0.35f, 0.43f to 0.38f, 0.38f to 0.41f,
    0.57f to 0.38f, 0.62f to 0.35f, 0.67f to 0.38f, 0.62f to 0.41f,
    0.36f to 0.29f, 0.50f to 0.27f, 0.64f to 0.29f,
    0.50f to 0.52f, 0.44f to 0.60f, 0.56f to 0.60f,
    0.50f to 0.73f, 0.42f to 0.78f, 0.58f to 0.78f
)

private val LANDMARK_DOT_COLORS = listOf(
    Color(0xFFFF637E), Color(0xFFFF637E), Color(0xFFFF637E), Color(0xFFFF637E), // Eyes L
    Color(0xFFFF637E), Color(0xFFFF637E), Color(0xFFFF637E), Color(0xFFFF637E), // Eyes R
    Color(0xFF9C27B0), Color(0xFF9C27B0), Color(0xFF9C27B0),                    // Brows
    Color(0xFF2196F3), Color(0xFF2196F3), Color(0xFF2196F3),                    // Nose
    Color(0xFFE53935), Color(0xFFE53935), Color(0xFFE53935)                     // Lips
)

private val EXPERT_TIPS = mapOf(
    "OVAL"     to "You have naturally balanced features. Small enhancements in the priority areas above will elevate your overall harmony even more!",
    "ROUND"    to "Contouring along the jawline and elongating techniques will enhance your natural features beautifully.",
    "SQUARE"   to "Soft, rounded makeup techniques complement your strong bone structure perfectly.",
    "HEART"    to "Balance your features by drawing attention to your lips and jaw with subtle enhancement.",
    "OBLONG"   to "Horizontal emphasis in brows and blush placement creates beautiful proportion.",
    "DIAMOND"  to "Your prominent cheekbones are an asset — soft highlighting enhances them naturally.",
    "TRIANGLE" to "Drawing attention upward with defined brows and eye makeup creates stunning balance."
)

private val IMPROVEMENT_LIST = listOf(
    ImprovementItem(1, "Undereye Area",   "Apply eye cream nightly and use colour-correcting concealer to brighten the under-eye area."),
    ImprovementItem(2, "Skin Texture",    "Exfoliate twice weekly and maintain a consistent moisturising routine for smoother skin."),
    ImprovementItem(3, "Brow Definition", "Shape and fill brows with hair-like strokes to frame your eyes and create better facial symmetry."),
    ImprovementItem(4, "Jawline Contour", "Use contouring along the jawline and a highlight on the chin to enhance definition and structure."),
    ImprovementItem(5, "Lip Definition",  "Define your natural lip border with a liner before applying colour for cleaner, fuller-looking results.")
)

// ── Feature detail helpers ────────────────────────────────────────────────────
private fun eyeDetails(shape: String): Triple<String, String, String> {
    val s = shape.lowercase()
    return when {
        s.contains("almond")     -> Triple("Ideal",    "Almond-shaped eyes are considered universally elegant, with a slight upward tilt at the outer corner and full iris visibility.", "Almost any eye makeup technique suits almond eyes. Winged liner and volumising mascara enhance your natural elegance beautifully.")
        s.contains("round")      -> Triple("Balanced", "Round eyes have a circular appearance with full iris visibility, creating an open and expressive look that draws people in.", "Elongating liner at the outer corners and a light shade in the inner corner adds depth and beautiful dimension.")
        s.contains("hooded")     -> Triple("Defined",  "Hooded eyes feature a skin fold over the mobile lid, creating a deep-set, sophisticated appearance with added mystery.", "Apply eyeshadow above the crease so it remains visible when eyes are open. Matte shades define better than shimmer.")
        s.contains("monolid")    -> Triple("Defined",  "Monolid eyes have a smooth lid without a visible crease, giving a distinctive, striking appearance that is widely admired.", "Cut-crease techniques and graphic liner create beautiful definition. Lighter shades on the lid add dimension and lift.")
        s.contains("upturned")   -> Triple("Balanced", "Upturned eyes have outer corners positioned higher than the inner corners, creating a naturally lifted, youthful appearance.", "Balance the natural lift by extending liner slightly downward at the outer corner for a harmonious, balanced look.")
        s.contains("downturned") -> Triple("Balanced", "Downturned eyes have outer corners that slope gently downward, creating a soft, gentle and approachable expression.", "A winged liner sweeping upward at the outer corner and lifting the outer brow creates a beautifully lifted effect.")
        s.contains("deep")       -> Triple("Defined",  "Deep-set eyes are positioned further back in the eye socket, creating a dramatic and intense appearance with strong brow prominence.", "Light shades on the lid and a highlighter on the brow bone bring your eyes forward and open them beautifully.")
        else                     -> Triple("Balanced", "Your eye shape has unique characteristics that beautifully complement your overall facial structure and expression.", "Experiment with different liner styles and eyeshadow placements to discover what enhances your natural eye shape best.")
    }
}

private fun browDetails(brow: String): Triple<String, String, String> {
    val s = brow.lowercase()
    return when {
        s.contains("straight")  -> Triple("Defined",  "Straight brows create a youthful, modern look with minimal arch, giving a fresh and clean-lined appearance.", "A slight arch added at the tail creates beautiful lift. Fill with light strokes following your natural hair direction.")
        s.contains("arch")      -> Triple("Ideal",    "Well-arched brows frame the face beautifully and create a polished, classic look that opens the eyes.", "Maintain your natural arch and fill any sparse areas with hair-like strokes for a natural, full finish.")
        s.contains("soft")      -> Triple("Balanced", "Soft arch brows have a gentle, gradual curve that creates a naturally elegant and approachable appearance.", "A brow pencil used lightly following the natural curve gives definition without appearing overdone.")
        s.contains("s-shape")   -> Triple("Balanced", "S-shaped brows feature both an arch and a gradual curve at the tail, creating a distinctive and characterful look.", "Focus on grooming to keep the natural S-curve clean and defined. A clear brow gel maintains the shape all day.")
        s.contains("angular")   -> Triple("Defined",  "Angular brows have a sharp peak that creates a striking, strong framing effect for the face and eyes.", "Soften the peak slightly with a spoolie brush after filling for a more refined, polished finish.")
        else                    -> Triple("Balanced", "Your brow shape naturally frames your eyes and complements your overall facial features with a balanced appearance.", "Regular grooming and filling sparse areas with fine hair-like strokes keeps your brows looking polished and defined.")
    }
}

private fun noseDetails(nose: String): Triple<String, String, String> {
    val s = nose.lowercase()
    return when {
        s.contains("button")    -> Triple("Proportioned", "A button nose is small and rounded with an upturned tip, contributing to a cute and balanced facial appearance.", "Light contouring along the sides and a subtle highlight on the tip accentuates its charming natural shape.")
        s.contains("straight")  -> Triple("Ideal",        "A straight nose bridge with balanced proportions creates a classic, harmonious facial profile that is widely admired.", "Very minimal contouring is needed. A small highlight on the bridge accentuates this ideal, balanced shape.")
        s.contains("aquiline")  -> Triple("Prominent",    "An aquiline nose features a curved bridge that is strong and distinctive, often considered a mark of character and strength.", "Contouring along the bridge softens the curve while a highlight at the tip draws attention forward attractively.")
        s.contains("bulbous")   -> Triple("Balanced",     "A bulbous nose tip has a rounded, full quality that adds character and warmth to the facial appearance.", "Contouring along the sides of the tip with a subtle highlight creates definition and visual refinement.")
        s.contains("wide")      -> Triple("Balanced",     "A wider nose bridge and base creates a grounded, strong facial presence that pairs well with bold features.", "Contouring along the sides of the nose and a highlight down the bridge creates a more defined, refined appearance.")
        s.contains("narrow")    -> Triple("Defined",      "A narrow nose bridge creates a refined, delicate appearance that adds elegance to the facial profile.", "A subtle highlight down the bridge enhances the natural refinement. Avoid heavy contouring that narrows it further.")
        else                    -> Triple("Proportioned", "Your nose shape is proportionate to your facial features and contributes to a balanced, harmonious overall appearance.", "Subtle contouring along the sides of the bridge and a light highlight on the tip will enhance your natural shape.")
    }
}

private fun lipDetails(lip: String): Triple<String, String, String> {
    val s = lip.lowercase()
    return when {
        s.contains("full")      -> Triple("Ideal",    "Full lips are naturally voluminous with defined edges, creating a sensual and youthful appearance that is universally admired.", "Define the border with a lip liner matching your natural colour. A gloss in the centre creates beautiful dimension.")
        s.contains("thin")      -> Triple("Balanced", "Thinner lips have a delicate, refined quality that pairs beautifully with bold makeup choices and strong eye looks.", "Slightly overline with a liner just outside the natural edge. A plumping gloss and lighter colours add visual volume.")
        s.contains("heart")     -> Triple("Ideal",    "A heart-shaped lip has a defined dip in the upper lip creating a romantic, classic Cupid's bow appearance.", "Emphasise the natural bow with a precise liner. A lighter shade in the centre highlights the beautiful shape.")
        s.contains("bow")       -> Triple("Ideal",    "A Cupid's bow lip has a distinctly defined upper lip with elegant peaks, creating a timeless, classic appearance.", "Use a lip liner to follow the natural peaks precisely. A gloss in the centre adds beautiful volume and dimension.")
        s.contains("wide")      -> Triple("Balanced", "Wide lips create a warm, expressive appearance and are particularly striking with bold lip colours and defined liner.", "Avoid liner extending beyond the natural corners. Darker shades help create a more defined appearance overall.")
        s.contains("defined")   -> Triple("Defined",  "Well-defined lips have clear, crisp edges that create a polished appearance and hold lip colour beautifully.", "Use a lip liner to maintain the natural definition and a long-wearing formula to preserve the crisp, clean edges.")
        else                    -> Triple("Balanced", "Your lip shape has natural definition that complements your overall facial harmony and suits a wide range of looks.", "Precise liner application following your natural lip border and a hydrating gloss will enhance your natural beauty.")
    }
}

private fun jawlineLabel(faceShape: String): String = when (faceShape.uppercase()) {
    "SQUARE"   -> "Square"
    "OVAL"     -> "Oval"
    "ROUND"    -> "Soft"
    "HEART"    -> "Tapered"
    "DIAMOND"  -> "Angular"
    "OBLONG"   -> "Defined"
    "TRIANGLE" -> "Wide"
    else       -> "Defined"
}

private fun jawlineDetails(label: String): Triple<String, String, String> {
    val s = label.lowercase()
    return when {
        s.contains("square") || s.contains("defined") ->
            Triple("Defined",  "A defined jawline creates strong facial structure and projects confidence and vitality, creating a striking overall appearance.", "Soft contouring at the jaw angles creates a refined silhouette. A light highlight along the jawline adds luminosity.")
        s.contains("oval")   ->
            Triple("Balanced", "An oval jawline offers a naturally harmonious facial balance that is versatile and complements most hairstyles and styles.", "A subtle contour under the jaw and a highlight on the chin creates a beautifully refined, sculpted appearance.")
        s.contains("soft") || s.contains("round") ->
            Triple("Balanced", "A soft jawline creates a gentle, approachable facial structure that pairs well with many hairstyles and makeup styles.", "Contouring just below the jawline and through the chin area creates beautiful definition and elongation.")
        s.contains("angular") || s.contains("tapered") ->
            Triple("Defined",  "Your jawline has distinctive angular qualities that create a striking, memorable appearance with strong natural structure.", "Light contouring at the jaw angles and a highlight along the bone softens sharper lines while keeping the beautiful structure.")
        else ->
            Triple("Balanced", "Your jawline contributes to a balanced facial structure with natural proportions and a pleasing overall shape.", "Gentle contouring along the jaw enhances your natural bone structure and creates a more defined, refined silhouette.")
    }
}

private fun cheekbonesLabel(faceShape: String): String = when (faceShape.uppercase()) {
    "DIAMOND", "HEART" -> "Prominent"
    "SQUARE", "OBLONG" -> "High"
    "OVAL"             -> "Balanced"
    "ROUND"            -> "Soft"
    "TRIANGLE"         -> "Low"
    else               -> "Medium"
}

private fun cheekbonesDetails(label: String): Triple<String, String, String> {
    val s = label.lowercase()
    return when {
        s.contains("prominent") || s.contains("high") ->
            Triple("Prominent", "High, prominent cheekbones are universally admired, creating natural facial dimension and a sculpted, striking appearance.", "A light highlighter on the apex of the cheekbones and a subtle blush just below creates a stunning, lifted effect.")
        s.contains("soft") || s.contains("low") ->
            Triple("Balanced",  "Softer cheekbone structure creates a gentle, approachable facial appearance that pairs beautifully with warm, natural makeup.", "Contouring beneath the cheekbone hollow and blush on the high points of the cheek creates beautiful lift and definition.")
        else ->
            Triple("Balanced",  "Your cheekbone structure contributes to the natural harmony of your facial proportions and overall balanced appearance.", "Blush applied on the upper cheeks and swept gently toward the temples enhances your natural bone structure beautifully.")
    }
}

private fun buildFeatureDetails(analysis: FaceAnalysis?): List<FeatureDetail> {
    val faceShape = analysis?.faceShape ?: "Oval"
    val (eyeStatus, eyeDesc, eyeTips) = eyeDetails(analysis?.eyeShape ?: "")
    val (browStatus, browDesc, browTips) = browDetails(analysis?.browType ?: "")
    val (noseStatus, noseDesc, noseTips) = noseDetails(analysis?.noseShape ?: "")
    val (lipStatus, lipDesc, lipTips) = lipDetails(analysis?.lipType ?: "")
    val jawLabel = jawlineLabel(faceShape)
    val (jawStatus, jawDesc, jawTips) = jawlineDetails(jawLabel)
    val cheekLabel = cheekbonesLabel(faceShape)
    val (cheekStatus, cheekDesc, cheekTips) = cheekbonesDetails(cheekLabel)

    return listOf(
        FeatureDetail(FeatureTab.EYES,       "Eye Shape",       analysis?.eyeShape?.toFeatureLabel() ?: "Almond",    eyeStatus,   eyeDesc,   eyeTips),
        FeatureDetail(FeatureTab.BROWS,      "Brow Shape",      analysis?.browType?.toFeatureLabel() ?: "Soft Arch", browStatus,  browDesc,  browTips),
        FeatureDetail(FeatureTab.NOSE,       "Nose Shape",      analysis?.noseShape?.toFeatureLabel() ?: "Straight", noseStatus,  noseDesc,  noseTips, "Contour Tips"),
        FeatureDetail(FeatureTab.LIPS,       "Lip Shape",       analysis?.lipType?.toFeatureLabel() ?: "Full",       lipStatus,   lipDesc,   lipTips),
        FeatureDetail(FeatureTab.JAWLINE,    "Jawline",         jawLabel,                                             jawStatus,   jawDesc,   jawTips),
        FeatureDetail(FeatureTab.CHEEKBONES, "Cheekbone Structure", cheekLabel,                                       cheekStatus, cheekDesc, cheekTips)
    )
}

// ── Symmetry helpers ──────────────────────────────────────────────────────────
private fun symmetryVerdict(score: Int): String = when {
    score >= 90 -> "Excellent"
    score >= 80 -> "Very Good"
    score >= 65 -> "Good"
    else        -> "Average"
}

private fun symmetryDescription(score: Int): String = when {
    score >= 90 -> "Your face has exceptional symmetry!"
    score >= 80 -> "Your face has excellent symmetry. Small variations make you unique."
    score >= 65 -> "Your face has good symmetry with natural variation."
    else        -> "Natural asymmetry is normal and makes you uniquely beautiful."
}

private fun symmetryScaleLevel(score: Int): Int = when {
    score >= 90 -> 3
    score >= 80 -> 2
    score >= 65 -> 1
    else        -> 0
}

// ── Tab helpers ───────────────────────────────────────────────────────────────
private fun nextTab(tab: FeatureTab): FeatureTab = when (tab) {
    FeatureTab.EYES       -> FeatureTab.BROWS
    FeatureTab.BROWS      -> FeatureTab.NOSE
    FeatureTab.NOSE       -> FeatureTab.LIPS
    FeatureTab.LIPS       -> FeatureTab.JAWLINE
    FeatureTab.JAWLINE    -> FeatureTab.CHEEKBONES
    FeatureTab.CHEEKBONES -> FeatureTab.EYES
    FeatureTab.OVERVIEW   -> FeatureTab.EYES
}

private fun statusIsPositive(status: String): Boolean =
    status in setOf("Ideal", "Balanced", "Proportioned", "Proportional", "Defined")

private fun parseImprovementItems(json: String): List<ImprovementItem> {
    if (json.isBlank() || json == "[]") return emptyList()
    return runCatching {
        val arr = JSONArray(json)
        (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            ImprovementItem(
                rank   = obj.optInt("rank", i + 1),
                area   = obj.optString("area", ""),
                action = obj.optString("action", "")
            )
        }
    }.getOrDefault(emptyList())
}

// ── Screen ────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FeatureAnalysisScreen(
    faceAnalysisId: Long = 0L,
    onBack: () -> Unit = {}
) {
    val viewModel: FeatureDetailViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(faceAnalysisId) { viewModel.load(faceAnalysisId) }

    val analysis = uiState.analysis

    var activeTab by remember { mutableStateOf(FeatureTab.OVERVIEW) }

    val features      = remember(analysis) { buildFeatureDetails(analysis) }
    val symmetryScore = uiState.symmetryScore
    val faceShapeKey  = remember(analysis) { analysis?.faceShape?.uppercase() ?: "OVAL" }
    val expertTip     = remember(faceShapeKey) { EXPERT_TIPS[faceShapeKey] ?: EXPERT_TIPS["OVAL"]!! }
    val faceShape     = remember(analysis) {
        analysis?.faceShape?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Oval"
    }

    val effectiveImprovementList = remember(uiState.improvementPriorityJson) {
        parseImprovementItems(uiState.improvementPriorityJson).ifEmpty { IMPROVEMENT_LIST }
    }

    var showLandmarkInfo    by remember { mutableStateOf(false) }
    var showImprovementInfo by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    if (showLandmarkInfo) {
        ModalBottomSheet(
            onDismissRequest = { showLandmarkInfo = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            InfoSheetContent(
                title = "About Landmark Detection",
                body  = "Landmark detection uses on-device ML to map 68 facial points across eyes, brows, nose, mouth, and jaw. This data powers your personalised analysis."
            )
        }
    }

    if (showImprovementInfo) {
        ModalBottomSheet(
            onDismissRequest = { showImprovementInfo = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            InfoSheetContent(
                title = "Improvement Priority",
                body  = "These areas were identified as having the highest positive impact on your overall look. Focus on these first for the best results."
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(FBackground)) {
        // Top bar
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
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Feature Detail Analysis", style = TextStyle(fontFamily = PoppinsFont, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = FText))
                Text("In-depth analysis of your facial features", style = TextStyle(fontFamily = PoppinsFont, fontSize = 11.sp, color = FMuted))
            }
            IconButton(onClick = { scope.launch { shareFeatureCard(context, features, symmetryScore, faceShape) } }) {
                Icon(Icons.Outlined.Share, "Share", tint = FText, modifier = Modifier.size(22.dp))
            }
        }

        // Scrollable tab bar
        ScrollableTabRow(
            selectedTabIndex = activeTab.ordinal,
            containerColor = FCardBg,
            contentColor = FRose,
            edgePadding = 8.dp,
            indicator = { tabPositions ->
                val pos = tabPositions[activeTab.ordinal]
                Box(
                    modifier = Modifier
                        .tabIndicatorOffset(pos)
                        .height(2.dp)
                        .background(FRose, RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                )
            },
            divider = {}
        ) {
            FEATURE_TAB_LABELS.forEachIndexed { i, label ->
                val isSelected = activeTab.ordinal == i
                Tab(
                    selected = isSelected,
                    onClick = { activeTab = FeatureTab.entries[i] },
                    text = {
                        Text(
                            label,
                            style = TextStyle(fontFamily = PoppinsFont, 
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) FRose else FMuted
                            )
                        )
                    }
                )
            }
        }

        // Tab content
        val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        if (activeTab == FeatureTab.OVERVIEW) {
            OverviewTabContent(
                features           = features,
                symmetryScore      = symmetryScore,
                expertTip          = expertTip,
                faceShape          = faceShape,
                faceShapeDesc      = analysis?.faceShapeDescription.orEmpty(),
                improvementList    = effectiveImprovementList,
                navBottom          = navBottom.value,
                onFeatureTap       = { activeTab = it },
                onLandmarkInfo     = { showLandmarkInfo = true },
                onImprovementInfo  = { showImprovementInfo = true }
            )
        } else {
            val feature = features.firstOrNull { it.tab == activeTab }
                ?: features.first()
            FeatureTabContent(
                feature    = feature,
                navBottom  = navBottom.value,
                onNextTab  = { activeTab = nextTab(activeTab) },
                nextFeature = features.firstOrNull { it.tab == nextTab(activeTab) }
            )
        }
    }
}

// ── Overview tab ──────────────────────────────────────────────────────────────
@Composable
private fun OverviewTabContent(
    features: List<FeatureDetail>,
    symmetryScore: Int,
    expertTip: String,
    faceShape: String,
    faceShapeDesc: String,
    improvementList: List<ImprovementItem>,
    navBottom: Float,
    onFeatureTap: (FeatureTab) -> Unit,
    onLandmarkInfo: () -> Unit,
    onImprovementInfo: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = 12.dp, bottom = navBottom.dp + 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Landmark map
        item {
            FCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left: description + legend
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Full-Face Landmark Map",
                                modifier = Modifier.weight(1f),
                                style = TextStyle(fontFamily = PoppinsFont, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = FText)
                            )
                            IconButton(onClick = onLandmarkInfo, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Outlined.Info, null, tint = FMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "We detected 68 key facial landmarks to analyse your unique features.",
                            style = TextStyle(fontFamily = PoppinsFont, fontSize = 10.sp, color = FMuted, lineHeight = 14.sp)
                        )
                        Spacer(Modifier.height(12.dp))
                        LEGEND_ITEMS.forEach { (dotColor, label) ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(dotColor))
                                Spacer(Modifier.width(6.dp))
                                Text(label, style = TextStyle(fontFamily = PoppinsFont, fontSize = 10.sp, color = FMuted))
                            }
                        }
                    }
                    // Right: face illustration
                    Box(
                        modifier = Modifier.width(120.dp).height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LandmarkFaceCanvas(modifier = Modifier.size(120.dp))
                    }
                }
            }
        }

        // 2. Feature deep-dive grid (3×2)
        item {
            FCard {
                Text("Feature Analysis", style = TextStyle(fontFamily = PoppinsFont, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = FText))
                Spacer(Modifier.height(12.dp))
                features.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { feature ->
                            FeatureOverviewCard(
                                feature  = feature,
                                modifier = Modifier.weight(1f),
                                onTap    = { onFeatureTap(feature.tab) }
                            )
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }
        }

        // 3. Symmetry score
        item { SymmetryScoreCard(symmetryScore) }

        // 4. Improvement priority
        item {
            FCard {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Improvement Priority", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = PoppinsFont, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = FText))
                    IconButton(onClick = onImprovementInfo, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Outlined.Info, null, tint = FMuted, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(Modifier.height(10.dp))
                improvementList.forEach { item ->
                    ImprovementRow(item)
                    if (item.rank < improvementList.size) Spacer(Modifier.height(8.dp))
                }
            }
        }

        // 5. Expert tip
        item {
            FCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Outlined.AutoAwesome, null, tint = FRose, modifier = Modifier.size(18.dp).padding(top = 1.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Expert Tip", style = TextStyle(fontFamily = PoppinsFont, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = FText))
                        Spacer(Modifier.height(4.dp))
                        Text(expertTip, style = TextStyle(fontFamily = PoppinsFont, fontSize = 11.sp, color = FMuted, lineHeight = 16.sp))
                    }
                }
            }
        }
    }
}

// ── Individual feature tab ────────────────────────────────────────────────────
@Composable
private fun FeatureTabContent(
    feature: FeatureDetail,
    navBottom: Float,
    onNextTab: () -> Unit,
    nextFeature: FeatureDetail?
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = 12.dp, bottom = navBottom.dp + 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Feature illustration + header
        item {
            FCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FeatureIllustration(tab = feature.tab, modifier = Modifier.size(100.dp))
                    Spacer(Modifier.height(12.dp))
                    Text(feature.typeLabel, style = TextStyle(fontFamily = PoppinsFont, fontSize = 12.sp, color = FMuted))
                    Spacer(Modifier.height(4.dp))
                    Text(feature.label, style = TextStyle(fontFamily = PoppinsFont, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = FText))
                    Spacer(Modifier.height(8.dp))
                    StatusBadge(feature.status)
                }
            }
        }

        // Description
        item {
            FCard {
                Text("Description", style = TextStyle(fontFamily = PoppinsFont, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = FMuted))
                Spacer(Modifier.height(8.dp))
                Text(feature.description, style = TextStyle(fontFamily = PoppinsFont, fontSize = 13.sp, color = FText, lineHeight = 20.sp))
            }
        }

        // Tips
        item {
            FCard {
                Text(feature.tipsLabel, style = TextStyle(fontFamily = PoppinsFont, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = FMuted))
                Spacer(Modifier.height(8.dp))
                Text(feature.tips, style = TextStyle(fontFamily = PoppinsFont, fontSize = 13.sp, color = FText, lineHeight = 20.sp))
            }
        }

        // Related feature card
        if (nextFeature != null) {
            item {
                FCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onNextTab() },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FeatureIllustration(tab = nextFeature.tab, modifier = Modifier.size(48.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Next Feature →",
                                style = TextStyle(fontFamily = PoppinsFont, fontSize = 10.sp, color = FMuted)
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                nextFeature.typeLabel,
                                style = TextStyle(fontFamily = PoppinsFont, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = FText)
                            )
                            Spacer(Modifier.height(4.dp))
                            StatusBadge(nextFeature.status)
                        }
                        Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, null, tint = FMuted, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

// ── Feature overview card (grid item) ─────────────────────────────────────────
@Composable
private fun FeatureOverviewCard(
    feature: FeatureDetail,
    modifier: Modifier = Modifier,
    onTap: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(FMutedBg)
            .border(1.dp, Color.Transparent, RoundedCornerShape(10.dp))
            .clickable { onTap() }
            .padding(10.dp)
    ) {
        FeatureIllustration(tab = feature.tab, modifier = Modifier.size(32.dp))
        Spacer(Modifier.height(6.dp))
        Text(feature.typeLabel, style = TextStyle(fontFamily = PoppinsFont, fontSize = 9.sp, color = FMuted))
        Spacer(Modifier.height(2.dp))
        Text(feature.label, style = TextStyle(fontFamily = PoppinsFont, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FText))
        Spacer(Modifier.height(5.dp))
        StatusBadge(feature.status)
        Spacer(Modifier.height(6.dp))
        Text(
            feature.description,
            style = TextStyle(fontFamily = PoppinsFont, fontSize = 9.sp, color = FMuted, lineHeight = 13.sp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(5.dp))
        Text(
            feature.tipsLabel,
            style = TextStyle(fontFamily = PoppinsFont, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = FMuted)
        )
        Text(
            feature.tips,
            style = TextStyle(fontFamily = PoppinsFont, fontSize = 9.sp, color = FMuted, lineHeight = 13.sp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, null, tint = FMuted, modifier = Modifier.size(10.dp))
        }
    }
}

// ── Symmetry score card ───────────────────────────────────────────────────────
@Composable
private fun SymmetryScoreCard(score: Int) {
    var animatedProgress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(score) {
        animate(
            initialValue = 0f,
            targetValue  = score / 100f,
            animationSpec = tween(durationMillis = 600, easing = LinearEasing)
        ) { v, _ -> animatedProgress = v }
    }

    val verdict     = symmetryVerdict(score)
    val description = symmetryDescription(score)
    val scaleLevel  = symmetryScaleLevel(score)
    val scaleLabels = listOf("Low", "Average", "High", "Excellent")

    FCard {
        Text("Symmetry Score", style = TextStyle(fontFamily = PoppinsFont, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = FText))
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Animated ring
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(88.dp)) {
                Canvas(modifier = Modifier.size(88.dp)) {
                    val strokeW = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round)
                    drawArc(FMutedBg, -90f, 360f, false, style = strokeW)
                    drawArc(FRose, -90f, 360f * animatedProgress, false, style = strokeW)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$score", style = TextStyle(fontFamily = PoppinsFont, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = FText))
                    Text("%", style = TextStyle(fontFamily = PoppinsFont, fontSize = 9.sp, color = FMuted))
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(verdict, style = TextStyle(fontFamily = PoppinsFont, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = FText))
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    scaleLabels.forEachIndexed { i, level ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (i <= scaleLevel) FRose else FMutedBg)
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                level,
                                style = TextStyle(fontFamily = PoppinsFont, 
                                    fontSize = 8.sp,
                                    color = if (i == scaleLevel) FRose else FMuted,
                                    fontWeight = if (i == scaleLevel) FontWeight.SemiBold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(description, style = TextStyle(fontFamily = PoppinsFont, fontSize = 10.sp, color = FMuted, lineHeight = 14.sp))
            }
        }
    }
}

// ── Improvement row ───────────────────────────────────────────────────────────
@Composable
private fun ImprovementRow(item: ImprovementItem) {
    val badgeSize = when (item.rank) {
        1    -> 34.dp
        2    -> 30.dp
        else -> 26.dp
    }
    val badgeBg = when (item.rank) {
        1    -> FRose
        2    -> FRose.copy(alpha = 0.7f)
        else -> FMutedBg
    }
    val badgeTextColor = if (item.rank <= 2) Color.White else FText

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(badgeSize)
                .clip(CircleShape)
                .background(badgeBg),
            contentAlignment = Alignment.Center
        ) {
            Text("${item.rank}", style = TextStyle(fontFamily = PoppinsFont, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = badgeTextColor))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(item.area, style = TextStyle(fontFamily = PoppinsFont, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = FText))
            Spacer(Modifier.height(2.dp))
            Text(item.action, style = TextStyle(fontFamily = PoppinsFont, fontSize = 11.sp, color = FMuted, lineHeight = 15.sp))
        }
    }
}

// ── Status badge ──────────────────────────────────────────────────────────────
@Composable
private fun StatusBadge(status: String) {
    val isPositive = statusIsPositive(status)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isPositive) FPositiveBg else FNeutralBg)
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            status,
            style = TextStyle(fontFamily = PoppinsFont, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = if (isPositive) FPositive else FNeutral)
        )
    }
}

// ── Info sheet content ────────────────────────────────────────────────────────
@Composable
private fun InfoSheetContent(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 24.dp)
    ) {
        Text(title, style = TextStyle(fontFamily = PoppinsFont, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = FText))
        Spacer(Modifier.height(10.dp))
        Text(body, style = TextStyle(fontFamily = PoppinsFont, fontSize = 13.sp, color = FMuted, lineHeight = 20.sp))
    }
}

// ── Landmark face canvas ──────────────────────────────────────────────────────
@Composable
private fun LandmarkFaceCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)

        // Face oval
        drawOval(
            color = FRose.copy(alpha = 0.4f),
            topLeft = Offset(w * 0.15f, h * 0.04f),
            size = Size(w * 0.70f, h * 0.92f),
            style = stroke
        )

        // Landmark dots
        LANDMARK_POSITIONS.forEachIndexed { i, (xF, yF) ->
            val cx = w * 0.15f + w * 0.70f * xF
            val cy = h * 0.04f + h * 0.92f * yF
            val dotColor = LANDMARK_DOT_COLORS.getOrElse(i) { FMuted }
            drawCircle(color = dotColor, radius = 3.5.dp.toPx(), center = Offset(cx, cy))
            drawCircle(color = Color.White, radius = 1.5.dp.toPx(), center = Offset(cx, cy))
        }
    }
}

// ── Feature illustration canvas ───────────────────────────────────────────────
@Composable
private fun FeatureIllustration(tab: FeatureTab, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)

        when (tab) {
            FeatureTab.EYES -> {
                // Two almond shapes
                val path = Path()
                // Left eye
                path.moveTo(w * 0.10f, h * 0.50f)
                path.cubicTo(w * 0.15f, h * 0.35f, w * 0.30f, h * 0.30f, w * 0.42f, h * 0.50f)
                path.cubicTo(w * 0.30f, h * 0.65f, w * 0.15f, h * 0.65f, w * 0.10f, h * 0.50f)
                // Right eye
                path.moveTo(w * 0.58f, h * 0.50f)
                path.cubicTo(w * 0.65f, h * 0.35f, w * 0.80f, h * 0.30f, w * 0.90f, h * 0.50f)
                path.cubicTo(w * 0.80f, h * 0.65f, w * 0.65f, h * 0.65f, w * 0.58f, h * 0.50f)
                drawPath(path, FRose, style = stroke)
                // Pupils
                drawCircle(FRose.copy(alpha = 0.5f), radius = w * 0.05f, center = Offset(w * 0.26f, h * 0.50f))
                drawCircle(FRose.copy(alpha = 0.5f), radius = w * 0.05f, center = Offset(w * 0.74f, h * 0.50f))
            }
            FeatureTab.BROWS -> {
                // Two arched brows
                val leftPath = Path()
                leftPath.moveTo(w * 0.08f, h * 0.58f)
                leftPath.cubicTo(w * 0.18f, h * 0.30f, w * 0.32f, h * 0.25f, w * 0.45f, h * 0.45f)
                drawPath(leftPath, FRose, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                val rightPath = Path()
                rightPath.moveTo(w * 0.55f, h * 0.45f)
                rightPath.cubicTo(w * 0.68f, h * 0.25f, w * 0.82f, h * 0.30f, w * 0.92f, h * 0.58f)
                drawPath(rightPath, FRose, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
            }
            FeatureTab.NOSE -> {
                // Bridge + nostrils
                val bridgePath = Path()
                bridgePath.moveTo(w * 0.50f, h * 0.10f)
                bridgePath.lineTo(w * 0.50f, h * 0.72f)
                drawPath(bridgePath, FRose.copy(alpha = 0.5f), style = Stroke(width = 1.5.dp.toPx()))
                // Nostrils
                drawOval(FRose, topLeft = Offset(w * 0.20f, h * 0.62f), size = Size(w * 0.22f, h * 0.22f), style = stroke)
                drawOval(FRose, topLeft = Offset(w * 0.58f, h * 0.62f), size = Size(w * 0.22f, h * 0.22f), style = stroke)
                // Nose tip line
                val tipPath = Path()
                tipPath.moveTo(w * 0.22f, h * 0.73f)
                tipPath.cubicTo(w * 0.35f, h * 0.90f, w * 0.65f, h * 0.90f, w * 0.78f, h * 0.73f)
                drawPath(tipPath, FRose, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
            }
            FeatureTab.LIPS -> {
                // Upper lip bow + lower lip
                val upperPath = Path()
                upperPath.moveTo(w * 0.12f, h * 0.50f)
                upperPath.cubicTo(w * 0.25f, h * 0.30f, w * 0.38f, h * 0.25f, w * 0.50f, h * 0.38f)
                upperPath.cubicTo(w * 0.62f, h * 0.25f, w * 0.75f, h * 0.30f, w * 0.88f, h * 0.50f)
                drawPath(upperPath, FRose, style = stroke)
                val lowerPath = Path()
                lowerPath.moveTo(w * 0.12f, h * 0.50f)
                lowerPath.cubicTo(w * 0.30f, h * 0.80f, w * 0.70f, h * 0.80f, w * 0.88f, h * 0.50f)
                drawPath(lowerPath, FRose, style = stroke)
                // Fill hint
                drawPath(lowerPath, FRose.copy(alpha = 0.1f))
            }
            FeatureTab.JAWLINE -> {
                // Lower face oval – jawline portion
                val path = Path()
                path.moveTo(w * 0.20f, h * 0.20f)
                path.cubicTo(w * 0.10f, h * 0.50f, w * 0.22f, h * 0.90f, w * 0.50f, h * 0.95f)
                path.cubicTo(w * 0.78f, h * 0.90f, w * 0.90f, h * 0.50f, w * 0.80f, h * 0.20f)
                drawPath(path, FRose, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
                // Chin dot
                drawCircle(FRose, radius = 4.dp.toPx(), center = Offset(w * 0.50f, h * 0.92f))
            }
            FeatureTab.CHEEKBONES -> {
                // Face oval with highlighted cheekbone lines
                drawOval(
                    color = FRose.copy(alpha = 0.2f),
                    topLeft = Offset(w * 0.15f, h * 0.05f),
                    size = Size(w * 0.70f, h * 0.90f),
                    style = Stroke(width = 1.5.dp.toPx())
                )
                // Left cheekbone
                val leftPath = Path()
                leftPath.moveTo(w * 0.15f, h * 0.42f)
                leftPath.lineTo(w * 0.38f, h * 0.52f)
                drawPath(leftPath, FRose, style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round))
                // Right cheekbone
                val rightPath = Path()
                rightPath.moveTo(w * 0.85f, h * 0.42f)
                rightPath.lineTo(w * 0.62f, h * 0.52f)
                drawPath(rightPath, FRose, style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round))
            }
            FeatureTab.OVERVIEW -> {
                // Simple face outline
                drawOval(FRose, topLeft = Offset(w * 0.15f, h * 0.05f), size = Size(w * 0.70f, h * 0.90f), style = stroke)
            }
        }
    }
}

// ── Shared card wrapper ───────────────────────────────────────────────────────
@Composable
private fun FCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FCardBg)
            .border(1.dp, FBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) { content() }
}

// ── Tab indicator helper ──────────────────────────────────────────────────────
private fun Modifier.tabIndicatorOffset(
    tabPosition: androidx.compose.material3.TabPosition
): Modifier = this.then(
    Modifier
        .fillMaxWidth()
        .padding(horizontal = tabPosition.left)
        .width(tabPosition.width)
)

// ── String helper ─────────────────────────────────────────────────────────────
private fun String.toFeatureLabel(): String =
    this.replace("_", " ").lowercase()
        .split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
        .ifEmpty { "—" }

// ── Share card generation ─────────────────────────────────────────────────────
private fun generateFeatureShareBitmap(
    features: List<FeatureDetail>,
    symmetryScore: Int,
    faceShape: String
): Bitmap {
    val w = 1080; val h = 1920
    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)

    val bgPaint = Paint().apply { color = android.graphics.Color.parseColor("#FCFCFC") }
    canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

    val wordmarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#FF637E")
        textSize = 72f
        typeface = Typeface.DEFAULT_BOLD
    }
    canvas.drawText("Lumi", 80f, 130f, wordmarkPaint)

    val divPaint = Paint().apply { color = android.graphics.Color.parseColor("#FFCCD3") }
    canvas.drawRect(80f, 155f, (w - 80).toFloat(), 158f, divPaint)

    val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#525252")
        textSize = 42f
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText("Feature Detail Analysis", w / 2f, 230f, headerPaint)

    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#0A0A0A")
        textSize = 64f
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText("$faceShape Face", w / 2f, 320f, titlePaint)

    // Top 3 features
    val featurePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#0A0A0A")
        textSize = 44f
        textAlign = Paint.Align.CENTER
    }
    val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#FF637E")
        textSize = 34f
        textAlign = Paint.Align.CENTER
    }
    features.take(3).forEachIndexed { i, f ->
        val y = 440f + i * 100f
        canvas.drawText("${f.typeLabel}: ${f.label}", w / 2f, y, featurePaint)
        canvas.drawText(f.status, w / 2f, y + 38f, subPaint)
    }

    // Symmetry score
    val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#0A0A0A")
        textSize = 120f
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }
    val scoreSubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#525252")
        textSize = 44f
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText("$symmetryScore%", w / 2f, 830f, scorePaint)
    canvas.drawText("Symmetry Score — ${symmetryVerdict(symmetryScore)}", w / 2f, 900f, scoreSubPaint)

    canvas.drawRect(80f, 950f, (w - 80).toFloat(), 953f, divPaint)

    val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#A4A4A4")
        textSize = 34f
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText("Generated by Lumi", w / 2f, 1880f, footerPaint)

    return bitmap
}

private suspend fun shareFeatureCard(
    context: Context,
    features: List<FeatureDetail>,
    symmetryScore: Int,
    faceShape: String
) {
    val bitmap = withContext(Dispatchers.Default) { generateFeatureShareBitmap(features, symmetryScore, faceShape) }
    val uri = withContext(Dispatchers.IO) {
        val file = File(context.cacheDir, "lumi_feature_card.jpg")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }
    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "image/jpeg"
        putExtra(android.content.Intent.EXTRA_STREAM, uri)
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    withContext(Dispatchers.Main) {
        context.startActivity(android.content.Intent.createChooser(intent, "Share your feature analysis"))
    }
}
