package com.appylab.lumi.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import android.graphics.Typeface
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkAdded
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appylab.lumi.ui.viewmodel.ColorAnalysisViewModel
import org.json.JSONArray
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// ── Palette ───────────────────────────────────────────────────────────────────
private val CRose       = Color(0xFFFF637E)
private val CBackground = Color(0xFFFCFCFC)
private val CCard       = Color.White
private val CBorder     = Color(0xFFFFCCD3)
private val CText       = Color(0xFF0A0A0A)
private val CMuted      = Color(0xFF525252)
private val CMutedBg    = Color(0xFFF5F5F5)
private val CYellow     = Color(0xFFFFF9C4)
private val CYellowText = Color(0xFF7B6A00)
private val CSavedGreen = Color(0xFF22C55E)

// ── Data models ───────────────────────────────────────────────────────────────
private data class ColorChip(
    val color: Color,
    val name: String,
    val hex: String = "",
    val reason: String = ""
)

private data class MetalData(val name: String, val hex: String, val descriptor: String)

private data class SeasonData(
    val name: String,
    val subSeason: String,
    val traits: List<String>,
    val description: String,
    val palette: List<ColorChip>,
    val avoidColors: List<ColorChip>,
    val clothingColors: List<ColorChip>,
    val hairColors: List<ColorChip>,
    val lipColors: List<ColorChip>,
    val eyeColors: List<ColorChip>
)

private data class SeasonEducation(
    val title: String,
    val characteristics: String,
    val keyColors: List<ColorChip>,
    val celebrities: List<String>
)

// ── Season education (static) ─────────────────────────────────────────────────
private val SEASON_EDUCATION = listOf(
    SeasonEducation(
        title = "Spring",
        characteristics = "Warm, bright, and clear. Springs have golden undertones with a fresh, vibrant quality that pairs beautifully with light, bright colours.",
        keyColors = listOf(
            ColorChip(Color(0xFFFF8C69), "Coral",        "#FF8C69"),
            ColorChip(Color(0xFFFFD9C0), "Peach",        "#FFD9C0"),
            ColorChip(Color(0xFFFFE066), "Warm Yellow",  "#FFE066"),
            ColorChip(Color(0xFF8BC34A), "Bright Green", "#8BC34A")
        ),
        celebrities = listOf("Jennifer Lopez", "Blake Lively")
    ),
    SeasonEducation(
        title = "Summer",
        characteristics = "Cool, soft, and muted. Summers have rosy undertones with a gentle, blended quality that looks stunning in dusty and muted shades.",
        keyColors = listOf(
            ColorChip(Color(0xFFD4A5A5), "Dusty Rose", "#D4A5A5"),
            ColorChip(Color(0xFFA3A0B8), "Lavender",   "#A3A0B8"),
            ColorChip(Color(0xFF8AA8C8), "Soft Blue",  "#8AA8C8"),
            ColorChip(Color(0xFF8AAA78), "Sage",        "#8AAA78")
        ),
        celebrities = listOf("Nicole Kidman", "Cate Blanchett")
    ),
    SeasonEducation(
        title = "Autumn",
        characteristics = "Warm, muted, and earthy. Autumns have golden-orange undertones with a rich, natural quality that shines in deep, spicy hues.",
        keyColors = listOf(
            ColorChip(Color(0xFFC87840), "Burnt Orange", "#C87840"),
            ColorChip(Color(0xFF708858), "Olive Green",  "#708858"),
            ColorChip(Color(0xFF905838), "Rust",         "#905838"),
            ColorChip(Color(0xFFC8A884), "Camel",        "#C8A884")
        ),
        celebrities = listOf("Julia Roberts", "Eva Longoria")
    ),
    SeasonEducation(
        title = "Winter",
        characteristics = "Cool, clear, and vivid. Winters have blue-pink undertones with a high-contrast, dramatic quality that commands attention in bold, pure colours.",
        keyColors = listOf(
            ColorChip(Color(0xFFC02030), "True Red",  "#C02030"),
            ColorChip(Color(0xFF182060), "Navy",       "#182060"),
            ColorChip(Color(0xFFF8F8F8), "Pure White", "#F8F8F8"),
            ColorChip(Color(0xFFC02880), "Fuchsia",    "#C02880")
        ),
        celebrities = listOf("Lupita Nyong'o", "Demi Moore")
    )
)

// ── Metals ────────────────────────────────────────────────────────────────────
private val METALS_WARM = listOf(
    MetalData("Gold",      "#FFD700", "Warm & Radiant"),
    MetalData("Rose Gold", "#D4A060", "Soft & Flattering"),
    MetalData("Bronze",    "#CD7F32", "Earthy & Rich")
)
private val METALS_COOL = listOf(
    MetalData("Silver",     "#C0C0C0", "Cool & Harmonious"),
    MetalData("White Gold", "#E8E8F0", "Elegant & Balanced"),
    MetalData("Platinum",   "#B8C0D0", "Crisp & Modern")
)
private val METALS_NEUTRAL = listOf(
    MetalData("Silver",     "#C0C0C0", "Cool & Harmonious"),
    MetalData("White Gold", "#E8E8F0", "Elegant & Balanced"),
    MetalData("Rose Gold",  "#D4A060", "Soft & Flattering")
)

// ── Season icon list (index matches SEASONS) ──────────────────────────────────
private val SEASON_ICONS: List<ImageVector> by lazy {
    listOf(
        Icons.Outlined.LocalFlorist,
        Icons.Outlined.WbSunny,
        Icons.Outlined.Park,
        Icons.Outlined.AcUnit
    )
}
private val SEASON_LABELS = listOf("Spring", "Summer", "Autumn", "Winter")

// ── Season data ───────────────────────────────────────────────────────────────
private val SEASONS = listOf(
    SeasonData(
        name = "Soft Spring",
        subSeason = "True Spring",
        traits = listOf("Warm", "Delicate", "Light"),
        description = "You're a Soft Spring! Warm, peachy and golden tones bring out your natural radiance beautifully.",
        palette = listOf(
            ColorChip(Color(0xFFFFD9C0), "Peach",       "#FFD9C0"),
            ColorChip(Color(0xFFFFE0A3), "Warm Yellow",  "#FFE0A3"),
            ColorChip(Color(0xFFF4C3A1), "Apricot",      "#F4C3A1"),
            ColorChip(Color(0xFFFFEDC8), "Ivory",        "#FFEDC8"),
            ColorChip(Color(0xFFC8A884), "Camel",        "#C8A884"),
            ColorChip(Color(0xFF8FAE88), "Moss",         "#8FAE88"),
            ColorChip(Color(0xFF6CA8A0), "Teal",         "#6CA8A0"),
            ColorChip(Color(0xFFD4B8A0), "Warm Tan",     "#D4B8A0")
        ),
        avoidColors = listOf(
            ColorChip(Color(0xFF2C2C6C), "Navy",     "#2C2C6C", "Dark navy creates harsh contrast against warm, delicate coloring"),
            ColorChip(Color(0xFF5C1A6C), "Purple",   "#5C1A6C", "Cool purple tones clash with your golden undertones"),
            ColorChip(Color(0xFF1C1C1C), "Black",    "#1C1C1C", "Pure black overwhelms your light, warm complexion"),
            ColorChip(Color(0xFFC0C8D8), "Ash Grey", "#C0C8D8", "Ashy tones make warm complexions look dull and washed out")
        ),
        clothingColors = listOf(
            ColorChip(Color(0xFFFFD9C0), "Peach"),
            ColorChip(Color(0xFFF4C3A1), "Apricot"),
            ColorChip(Color(0xFFFFE0A3), "Wheat"),
            ColorChip(Color(0xFFC8A884), "Camel"),
            ColorChip(Color(0xFF8FAE88), "Moss"),
            ColorChip(Color(0xFF6CA8A0), "Teal"),
            ColorChip(Color(0xFFD4B8A0), "Sand"),
            ColorChip(Color(0xFFE8D0B8), "Oat")
        ),
        hairColors = listOf(
            ColorChip(Color(0xFFC8A060), "Golden Brown"),
            ColorChip(Color(0xFFB07840), "Warm Chestnut"),
            ColorChip(Color(0xFFD4B870), "Honey Blonde"),
            ColorChip(Color(0xFFE8C880), "Strawberry Blonde"),
            ColorChip(Color(0xFF905830), "Auburn")
        ),
        lipColors = listOf(
            ColorChip(Color(0xFFE8988C), "Coral Pink"),
            ColorChip(Color(0xFFD87060), "Warm Coral"),
            ColorChip(Color(0xFFE0B8A0), "Nude Peach"),
            ColorChip(Color(0xFFC06858), "Terracotta")
        ),
        eyeColors = listOf(
            ColorChip(Color(0xFFC87060), "Coral"),
            ColorChip(Color(0xFF8FAE88), "Moss"),
            ColorChip(Color(0xFFC8A060), "Gold"),
            ColorChip(Color(0xFF6CA8A0), "Teal")
        )
    ),
    SeasonData(
        name = "Soft Summer",
        subSeason = "Soft Summer",
        traits = listOf("Cool", "Soft", "Light"),
        description = "You're a Soft Summer! Cool and muted colours will enhance your natural vibrancy.",
        palette = listOf(
            ColorChip(Color(0xFFD4A5A5), "Dusty Rose",    "#D4A5A5"),
            ColorChip(Color(0xFFA0788C), "Mauve",         "#A0788C"),
            ColorChip(Color(0xFFA3A0B8), "Lavender Grey", "#A3A0B8"),
            ColorChip(Color(0xFF6B88A0), "Slate Blue",    "#6B88A0"),
            ColorChip(Color(0xFF8AA8C8), "Soft Blue",     "#8AA8C8"),
            ColorChip(Color(0xFF7DB8A8), "Seafoam",       "#7DB8A8"),
            ColorChip(Color(0xFF8AAA78), "Sage",          "#8AAA78"),
            ColorChip(Color(0xFFC0C0C8), "Light Grey",    "#C0C0C8")
        ),
        avoidColors = listOf(
            ColorChip(Color(0xFFC8A030), "Mustard",  "#C8A030", "Warm mustard clashes with your cool, muted undertones"),
            ColorChip(Color(0xFFE07830), "Orange",   "#E07830", "Orange overwhelms the delicate balance of summer coloring"),
            ColorChip(Color(0xFFC83030), "True Red", "#C83030", "Saturated red creates harsh contrast with soft summer tones"),
            ColorChip(Color(0xFF101010), "Black",    "#101010", "Pure black is too severe against your soft, cool features")
        ),
        clothingColors = listOf(
            ColorChip(Color(0xFFD4A5A5), "Dusty Rose"),
            ColorChip(Color(0xFFA0788C), "Mauve"),
            ColorChip(Color(0xFF8AA8C8), "Powder Blue"),
            ColorChip(Color(0xFF6B88A0), "Slate Blue"),
            ColorChip(Color(0xFF7DB8A8), "Seafoam"),
            ColorChip(Color(0xFF8AAA78), "Sage"),
            ColorChip(Color(0xFFC0A8B8), "Cool Taupe"),
            ColorChip(Color(0xFF8090A8), "Navy Grey")
        ),
        hairColors = listOf(
            ColorChip(Color(0xFF909090), "Ash Brown"),
            ColorChip(Color(0xFFA89898), "Mushroom Brown"),
            ColorChip(Color(0xFF786868), "Cool Brunette"),
            ColorChip(Color(0xFFB8A8A8), "Taupe Blonde"),
            ColorChip(Color(0xFFD0C8C0), "Platinum")
        ),
        lipColors = listOf(
            ColorChip(Color(0xFFCE8898), "Rose"),
            ColorChip(Color(0xFFA87888), "Mauve"),
            ColorChip(Color(0xFFD4B0B8), "Nude Pink"),
            ColorChip(Color(0xFF905868), "Berry")
        ),
        eyeColors = listOf(
            ColorChip(Color(0xFF8090B8), "Periwinkle"),
            ColorChip(Color(0xFF8090A0), "Slate"),
            ColorChip(Color(0xFFA890A8), "Lavender"),
            ColorChip(Color(0xFF7088A0), "Slate Blue")
        )
    ),
    SeasonData(
        name = "Soft Autumn",
        subSeason = "Soft Autumn",
        traits = listOf("Warm", "Muted", "Deep"),
        description = "You're a Soft Autumn! Earthy, muted tones and warm spice shades complement you naturally.",
        palette = listOf(
            ColorChip(Color(0xFFC87840), "Burnt Orange", "#C87840"),
            ColorChip(Color(0xFF905838), "Rust",         "#905838"),
            ColorChip(Color(0xFFB89038), "Mustard",      "#B89038"),
            ColorChip(Color(0xFF708858), "Olive Green",  "#708858"),
            ColorChip(Color(0xFF605030), "Chocolate",    "#605030"),
            ColorChip(Color(0xFF508080), "Teal",         "#508080"),
            ColorChip(Color(0xFF987060), "Terracotta",   "#987060"),
            ColorChip(Color(0xFF907858), "Khaki",        "#907858")
        ),
        avoidColors = listOf(
            ColorChip(Color(0xFFB0C0D8), "Ice Blue",      "#B0C0D8", "Icy cool blues drain warmth from earthy autumn skin"),
            ColorChip(Color(0xFFD0A0C8), "Pastel Pink",   "#D0A0C8", "Cool pink pastels clash with your warm, muted palette"),
            ColorChip(Color(0xFF3848A0), "Royal Blue",    "#3848A0", "Cool royal blue conflicts with your earthy undertones"),
            ColorChip(Color(0xFFA040B0), "Bright Violet", "#A040B0", "Cool bright violet fights your warm, earthy palette")
        ),
        clothingColors = listOf(
            ColorChip(Color(0xFFC87840), "Rust"),
            ColorChip(Color(0xFFB89038), "Mustard"),
            ColorChip(Color(0xFF708858), "Olive"),
            ColorChip(Color(0xFF605030), "Chocolate"),
            ColorChip(Color(0xFF987060), "Terracotta"),
            ColorChip(Color(0xFF907858), "Camel"),
            ColorChip(Color(0xFF508080), "Teal"),
            ColorChip(Color(0xFFD0B090), "Warm Cream")
        ),
        hairColors = listOf(
            ColorChip(Color(0xFF806030), "Warm Chestnut"),
            ColorChip(Color(0xFF905838), "Auburn"),
            ColorChip(Color(0xFFB08040), "Copper Blonde"),
            ColorChip(Color(0xFF604028), "Dark Espresso"),
            ColorChip(Color(0xFFC09050), "Honey")
        ),
        lipColors = listOf(
            ColorChip(Color(0xFFC07060), "Terracotta"),
            ColorChip(Color(0xFF986050), "Brick"),
            ColorChip(Color(0xFFD09080), "Warm Nude"),
            ColorChip(Color(0xFFA05840), "Russet")
        ),
        eyeColors = listOf(
            ColorChip(Color(0xFF987040), "Bronze"),
            ColorChip(Color(0xFF708858), "Olive"),
            ColorChip(Color(0xFF806030), "Copper"),
            ColorChip(Color(0xFF905838), "Rust")
        )
    ),
    SeasonData(
        name = "Deep Winter",
        subSeason = "True Winter",
        traits = listOf("Cool", "Clear", "Deep"),
        description = "You're a Deep Winter! High-contrast, vivid and pure cool shades make your features pop.",
        palette = listOf(
            ColorChip(Color(0xFFC02030), "True Red",   "#C02030"),
            ColorChip(Color(0xFF101010), "Black",      "#101010"),
            ColorChip(Color(0xFF182060), "Navy",       "#182060"),
            ColorChip(Color(0xFFF8F8F8), "Pure White", "#F8F8F8"),
            ColorChip(Color(0xFFC02880), "Fuchsia",    "#C02880"),
            ColorChip(Color(0xFF5090D0), "Ice Blue",   "#5090D0"),
            ColorChip(Color(0xFF207040), "Emerald",    "#207040"),
            ColorChip(Color(0xFF780030), "Burgundy",   "#780030")
        ),
        avoidColors = listOf(
            ColorChip(Color(0xFFD4A060), "Gold",       "#D4A060", "Warm gold brings out yellow undertones unfavorably"),
            ColorChip(Color(0xFFB89038), "Mustard",    "#B89038", "Warm mustard makes cool winter complexions look sallow"),
            ColorChip(Color(0xFFD4B8A0), "Beige",      "#D4B8A0", "Warm beige makes winter features look dull and washed out"),
            ColorChip(Color(0xFFD4A5A5), "Dusty Rose", "#D4A5A5", "Muted rosy tones don't provide enough contrast for winter")
        ),
        clothingColors = listOf(
            ColorChip(Color(0xFF101010), "Black"),
            ColorChip(Color(0xFFF8F8F8), "White"),
            ColorChip(Color(0xFF182060), "Navy"),
            ColorChip(Color(0xFFC02030), "Red"),
            ColorChip(Color(0xFF780030), "Burgundy"),
            ColorChip(Color(0xFF207040), "Emerald"),
            ColorChip(Color(0xFF5090D0), "Ice Blue"),
            ColorChip(Color(0xFFC02880), "Fuchsia")
        ),
        hairColors = listOf(
            ColorChip(Color(0xFF181818), "Jet Black"),
            ColorChip(Color(0xFF383028), "Dark Espresso"),
            ColorChip(Color(0xFF503840), "Darkest Brown"),
            ColorChip(Color(0xFF202838), "Cool Black"),
            ColorChip(Color(0xFFA8A0B0), "Silver")
        ),
        lipColors = listOf(
            ColorChip(Color(0xFFC02030), "True Red"),
            ColorChip(Color(0xFF780030), "Burgundy"),
            ColorChip(Color(0xFFC02880), "Fuchsia"),
            ColorChip(Color(0xFF480030), "Deep Berry")
        ),
        eyeColors = listOf(
            ColorChip(Color(0xFF101010), "Smoky Black"),
            ColorChip(Color(0xFF182060), "Navy"),
            ColorChip(Color(0xFF5090D0), "Ice Blue"),
            ColorChip(Color(0xFFC02880), "Fuchsia")
        )
    )
)

// ── Helpers ───────────────────────────────────────────────────────────────────
private fun detectSeasonIndex(skinTone: String, undertone: String): Int {
    val tone = undertone.uppercase()
    val skin = skinTone.lowercase()
    val isLight = skin.contains("fair") || skin.contains("light")
    return when {
        tone == "WARM" && isLight -> 0
        tone == "WARM"            -> 2
        tone == "COOL" && isLight -> 1
        tone == "COOL"            -> 3
        else                      -> 1
    }
}

private fun getMetals(undertone: String): List<MetalData> = when (undertone.uppercase()) {
    "WARM" -> METALS_WARM
    "COOL" -> METALS_COOL
    else   -> METALS_NEUTRAL
}

private fun undertoneAdjective(undertone: String): String = when (undertone.uppercase()) {
    "WARM" -> "warm"
    "COOL" -> "cool"
    else   -> "balanced"
}

private fun hexToComposeColor(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (_: Exception) {
    Color.Gray
}

private fun parseColorChipsFromJson(json: String): List<ColorChip> {
    if (json.isBlank() || json == "[]") return emptyList()
    return runCatching {
        val arr = JSONArray(json)
        (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            val hex = obj.optString("hex", "#808080")
            ColorChip(
                color  = hexToComposeColor(hex),
                name   = obj.optString("name", ""),
                hex    = hex,
                reason = obj.optString("reason", "")
            )
        }
    }.getOrDefault(emptyList())
}

// ── Screen ────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ColorAnalysisScreen(
    faceAnalysisId: Long = 0L,
    onBack: () -> Unit = {}
) {
    val viewModel: ColorAnalysisViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(faceAnalysisId) { viewModel.load(faceAnalysisId) }

    val skinTone  = uiState.skinTone
    val undertone = uiState.undertone

    val detectedIndex = remember(skinTone, undertone) { detectSeasonIndex(skinTone, undertone) }
    val baseSeason    = SEASONS[detectedIndex]

    // Merge AI color overrides into the detected season (falls back to static when AI data absent)
    val season = remember(uiState, baseSeason) {
        baseSeason.copy(
            palette       = parseColorChipsFromJson(uiState.personalPaletteJson).ifEmpty { baseSeason.palette },
            avoidColors   = parseColorChipsFromJson(uiState.avoidColorsJson).ifEmpty { baseSeason.avoidColors },
            clothingColors = parseColorChipsFromJson(uiState.clothingRecsJson).ifEmpty { baseSeason.clothingColors },
            hairColors    = parseColorChipsFromJson(uiState.hairColorRecsJson).ifEmpty { baseSeason.hairColors },
            lipColors     = parseColorChipsFromJson(uiState.lipColorsJson).ifEmpty { baseSeason.lipColors },
            eyeColors     = parseColorChipsFromJson(uiState.eyeColorsJson).ifEmpty { baseSeason.eyeColors }
        )
    }

    val metals      = remember(undertone) { getMetals(undertone) }
    val undertoneAdj = remember(undertone) { undertoneAdjective(undertone) }

    var educationSheet by remember { mutableStateOf<Int?>(null) }
    var showAvoidSheet by remember { mutableStateOf(false) }
    var isSaved        by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.isPaletteSaved) { isSaved = uiState.isPaletteSaved }

    val context   = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope     = rememberCoroutineScope()
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    educationSheet?.let { idx ->
        ModalBottomSheet(
            onDismissRequest = { educationSheet = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            SeasonEducationContent(SEASON_EDUCATION[idx], isDetected = idx == detectedIndex)
        }
    }

    if (showAvoidSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAvoidSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            AvoidInfoContent(season.avoidColors)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(CBackground)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = 8.dp, bottom = navBottom + 152.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = CText, modifier = Modifier.size(22.dp))
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Color Analysis", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = CText))
                        Text("Discover your most flattering colors", style = TextStyle(fontSize = 11.sp, color = CMuted))
                    }
                    IconButton(onClick = { scope.launch { shareColorCard(context, season) } }) {
                        Icon(Icons.Outlined.Share, "Share", tint = CText, modifier = Modifier.size(22.dp))
                    }
                }
            }

            // 1. Season result card
            item {
                SeasonResultCard(season, detectedIndex, onSeasonTap = { educationSheet = it })
            }

            // 2. Personal color palette
            item {
                ColorCard {
                    SectionTitle("Your Personal Color Palette")
                    Spacer(Modifier.height(2.dp))
                    Text("Tap a swatch to copy its hex", style = TextStyle(fontSize = 11.sp, color = CMuted))
                    Spacer(Modifier.height(14.dp))
                    SwatchGrid(swatches = season.palette, columns = 4) { chip ->
                        if (chip.hex.isNotEmpty()) {
                            clipboard.setText(AnnotatedString(chip.hex))
                            Toast.makeText(context, "Hex ${chip.hex} copied", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            // 3. Colors to avoid
            item {
                ColorCard {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        SectionTitle("Colors to Avoid")
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { showAvoidSheet = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Outlined.Info, "Why?", tint = CMuted, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    SwatchGrid(swatches = season.avoidColors, columns = 4, onTap = null)
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CMutedBg)
                            .padding(10.dp)
                    ) {
                        Text(
                            "These colors may overwhelm your natural tones. Opt for soft, $undertoneAdj, and muted shades instead.",
                            style = TextStyle(fontSize = 11.sp, color = CMuted, lineHeight = 16.sp)
                        )
                    }
                }
            }

            // 4. Clothing recommendations
            item {
                ColorCard {
                    SectionTitle("Clothing Color Recommendations")
                    Spacer(Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(season.clothingColors) { swatch ->
                            SwatchItem(swatch = swatch, size = 52.dp)
                        }
                    }
                }
            }

            // 5. Hair + Makeup side-by-side
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ColorCard(modifier = Modifier.weight(1f)) {
                        SectionTitle("Hair Colors")
                        Spacer(Modifier.height(10.dp))
                        season.hairColors.forEach { swatch ->
                            Row(
                                modifier = Modifier.padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(28.dp)
                                        .clip(CircleShape)
                                        .background(swatch.color)
                                        .border(1.dp, Color.Black.copy(alpha = 0.07f), CircleShape)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(swatch.name, style = TextStyle(fontSize = 10.sp, color = CText))
                            }
                        }
                    }
                    ColorCard(modifier = Modifier.weight(1f)) {
                        SectionTitle("Makeup")
                        Spacer(Modifier.height(10.dp))
                        Text("Lip", style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = CMuted))
                        Spacer(Modifier.height(5.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            season.lipColors.forEach { swatch ->
                                Box(
                                    modifier = Modifier.size(22.dp)
                                        .clip(CircleShape)
                                        .background(swatch.color)
                                        .border(1.dp, Color.Black.copy(alpha = 0.07f), CircleShape)
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text("Eye", style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = CMuted))
                        Spacer(Modifier.height(5.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            season.eyeColors.forEach { swatch ->
                                Box(
                                    modifier = Modifier.size(22.dp)
                                        .clip(CircleShape)
                                        .background(swatch.color)
                                        .border(1.dp, Color.Black.copy(alpha = 0.07f), CircleShape)
                                )
                            }
                        }
                    }
                }
            }

            // 6. Best metals
            item {
                ColorCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            SectionTitle("Best Metals")
                            Spacer(Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                metals.forEach { metal ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier.size(40.dp)
                                                .clip(CircleShape)
                                                .background(hexToComposeColor(metal.hex))
                                                .border(1.dp, Color.Black.copy(alpha = 0.1f), CircleShape)
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            metal.name,
                                            style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = CText, textAlign = TextAlign.Center),
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            metal.descriptor,
                                            style = TextStyle(fontSize = 8.sp, color = CMuted, textAlign = TextAlign.Center, lineHeight = 10.sp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Icon(Icons.Outlined.AutoAwesome, null, tint = CRose, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.height(6.dp))
                            val metalText = metals.mapIndexed { i, m ->
                                if (i == metals.lastIndex && metals.size > 1) "and ${m.name}" else m.name
                            }.joinToString(", ")
                            Text(
                                "${undertoneAdj.replaceFirstChar { it.uppercase() }}-toned metals enhance your natural glow. $metalText are your best picks!",
                                style = TextStyle(fontSize = 11.sp, color = CMuted, lineHeight = 15.sp)
                            )
                        }
                    }
                }
            }

            // 7. Share card row
            item {
                ColorCard {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clickable { scope.launch { shareColorCard(context, season) } },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp)
                                .clip(CircleShape)
                                .background(CRose.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Share, null, tint = CRose, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Share Your Color Card", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CText))
                            Spacer(Modifier.height(2.dp))
                            Text("Create a shareable card with your palette", style = TextStyle(fontSize = 11.sp, color = CMuted))
                        }
                        Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, null, tint = CMuted, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        // Sticky bottom bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(CCard)
                .border(1.dp, CBorder.copy(alpha = 0.5f))
                .padding(bottom = 60.dp)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Button(
                onClick = {
                    val newVal = !isSaved
                    isSaved = newVal
                    viewModel.toggleSavePalette(faceAnalysisId, newVal)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isSaved) CSavedGreen else CRose)
            ) {
                Icon(
                    if (isSaved) Icons.Outlined.BookmarkAdded else Icons.Outlined.Bookmark,
                    null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.White
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isSaved) "Saved to Profile ✓" else "Save Palette to Profile",
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                )
            }
        }
    }
}

// ── Season result card ────────────────────────────────────────────────────────
@Composable
private fun SeasonResultCard(
    season: SeasonData,
    detectedIndex: Int,
    onSeasonTap: (Int) -> Unit
) {
    ColorCard {
        Text("Your Best Season", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CText))
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1.2f)) {
                Text(season.name, style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = CText))
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(CRose.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        season.traits.joinToString(" • "),
                        style = TextStyle(fontSize = 10.sp, color = CRose, fontWeight = FontWeight.Medium)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(season.description, style = TextStyle(fontSize = 11.sp, color = CMuted, lineHeight = 16.sp))
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CMutedBg)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sub Season", style = TextStyle(fontSize = 10.sp, color = CMuted))
                    Spacer(Modifier.weight(1f))
                    Text(season.subSeason, style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = CText))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Explore Seasons", style = TextStyle(fontSize = 10.sp, color = CMuted))
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SeasonIconItem(0, detectedIndex, onSeasonTap, Modifier.weight(1f))
                    SeasonIconItem(1, detectedIndex, onSeasonTap, Modifier.weight(1f))
                }
                Spacer(Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SeasonIconItem(2, detectedIndex, onSeasonTap, Modifier.weight(1f))
                    SeasonIconItem(3, detectedIndex, onSeasonTap, Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(CYellow)
                        .padding(6.dp)
                ) {
                    Text("Tap a season to learn more", style = TextStyle(fontSize = 9.sp, color = CYellowText))
                }
            }
        }
    }
}

@Composable
private fun SeasonIconItem(
    index: Int,
    detectedIndex: Int,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDetected = index == detectedIndex
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isDetected) CRose.copy(alpha = 0.08f) else CMutedBg)
            .border(
                width = if (isDetected) 1.5.dp else 0.dp,
                color = if (isDetected) CRose else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick(index) }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(SEASON_ICONS[index], SEASON_LABELS[index], tint = if (isDetected) CRose else CMuted, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(2.dp))
        Text(
            SEASON_LABELS[index],
            style = TextStyle(
                fontSize = 9.sp,
                fontWeight = if (isDetected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isDetected) CRose else CMuted
            )
        )
    }
}

// ── Season education sheet ────────────────────────────────────────────────────
@Composable
private fun SeasonEducationContent(edu: SeasonEducation, isDetected: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 4.dp, bottom = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(edu.title, style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = CText))
            if (isDetected) {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(CRose).padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("Your Season", style = TextStyle(fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.SemiBold))
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(edu.characteristics, style = TextStyle(fontSize = 13.sp, color = CMuted, lineHeight = 18.sp))
        Spacer(Modifier.height(18.dp))
        Text("Key Colors", style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CText))
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            edu.keyColors.forEach { chip ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(CircleShape).background(chip.color)
                            .border(1.dp, Color.Black.copy(alpha = 0.07f), CircleShape)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(chip.name, style = TextStyle(fontSize = 9.sp, color = CMuted, textAlign = TextAlign.Center))
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("Famous ${edu.title} Celebrities", style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CText))
        Spacer(Modifier.height(8.dp))
        edu.celebrities.forEach { celeb ->
            Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(CRose))
                Spacer(Modifier.width(8.dp))
                Text(celeb, style = TextStyle(fontSize = 12.sp, color = CMuted))
            }
        }
    }
}

// ── Avoid info sheet ──────────────────────────────────────────────────────────
@Composable
private fun AvoidInfoContent(avoidColors: List<ColorChip>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 4.dp, bottom = 24.dp)
    ) {
        Text("Why Avoid These Colors?", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CText))
        Spacer(Modifier.height(16.dp))
        avoidColors.forEach { chip ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(chip.color)
                        .border(1.dp, Color.Black.copy(alpha = 0.07f), CircleShape)
                )
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(chip.name, style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CText))
                    Spacer(Modifier.height(3.dp))
                    Text(chip.reason, style = TextStyle(fontSize = 11.sp, color = CMuted, lineHeight = 15.sp))
                }
            }
        }
    }
}

// ── Shared helpers ────────────────────────────────────────────────────────────
@Composable
private fun SwatchGrid(
    swatches: List<ColorChip>,
    columns: Int,
    onTap: ((ColorChip) -> Unit)?
) {
    swatches.chunked(columns).forEach { row ->
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            row.forEach { swatch ->
                SwatchItem(swatch = swatch, size = 48.dp, showHex = true,
                    onClick = onTap?.let { handler -> { handler(swatch) } })
            }
            repeat(columns - row.size) { Spacer(Modifier.size(56.dp)) }
        }
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun SwatchItem(
    swatch: ColorChip,
    size: Dp,
    showHex: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(size + 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(swatch.color)
                .border(1.dp, Color.Black.copy(alpha = 0.07f), CircleShape)
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            swatch.name,
            style = TextStyle(fontSize = 9.sp, color = CMuted, textAlign = TextAlign.Center, lineHeight = 11.sp),
            textAlign = TextAlign.Center
        )
        if (showHex && swatch.hex.isNotEmpty()) {
            Text(
                swatch.hex,
                style = TextStyle(fontSize = 8.sp, color = Color(0xFFAAAAAA), textAlign = TextAlign.Center)
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CText))
}

@Composable
private fun ColorCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CCard)
            .border(1.dp, CBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        content()
    }
}

// ── Share card generation ─────────────────────────────────────────────────────
private fun generateShareCard(season: SeasonData): Bitmap {
    val size = 1080
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)

    val bgPaint = Paint().apply { color = android.graphics.Color.parseColor("#FCFCFC") }
    canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), bgPaint)

    val wordmarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#FF637E")
        textSize = 72f
        typeface = Typeface.DEFAULT_BOLD
    }
    canvas.drawText("Lumi", 80f, 120f, wordmarkPaint)

    val divPaint = Paint().apply { color = android.graphics.Color.parseColor("#FFCCD3") }
    canvas.drawRect(80f, 140f, (size - 80).toFloat(), 143f, divPaint)

    val seasonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#0A0A0A")
        textSize = 80f
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText(season.name, size / 2f, 290f, seasonPaint)

    val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#525252")
        textSize = 44f
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText(season.subSeason, size / 2f, 360f, subPaint)

    val traitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#FF637E")
        textSize = 38f
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText(season.traits.joinToString("  ·  "), size / 2f, 420f, traitPaint)

    val swatchR = 42f
    val gap = 16f
    val totalW = 8 * swatchR * 2 + 7 * gap
    val startX = (size - totalW) / 2f + swatchR
    season.palette.forEachIndexed { i, chip ->
        val cx = startX + i * (swatchR * 2 + gap)
        val swatchPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(
                (chip.color.red * 255).toInt(),
                (chip.color.green * 255).toInt(),
                (chip.color.blue * 255).toInt()
            )
        }
        canvas.drawCircle(cx, 540f, swatchR, swatchPaint)
    }

    canvas.drawRect(80f, 620f, (size - 80).toFloat(), 623f, divPaint)

    val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#A4A4A4")
        textSize = 34f
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText("Generated by Lumi", size / 2f, 1040f, footerPaint)

    return bitmap
}

private suspend fun shareColorCard(context: Context, season: SeasonData) {
    val bitmap = withContext(Dispatchers.Default) { generateShareCard(season) }
    val uri = withContext(Dispatchers.IO) {
        val file = File(context.cacheDir, "lumi_color_card.jpg")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/jpeg"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    withContext(Dispatchers.Main) {
        context.startActivity(Intent.createChooser(intent, "Share your colour season"))
    }
}
