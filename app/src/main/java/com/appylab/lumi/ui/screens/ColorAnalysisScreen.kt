package com.appylab.lumi.ui.screens

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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
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
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Palette ───────────────────────────────────────────────────────────────────
private val CRose       = Color(0xFFFF637E)
private val CBackground = Color(0xFFFCFCFC)
private val CCard       = Color.White
private val CBorder     = Color(0xFFFFCCD3)
private val CText       = Color(0xFF0A0A0A)
private val CMuted      = Color(0xFF525252)
private val CMutedBg    = Color(0xFFF5F5F5)
private val CDark       = Color(0xFF0A0A0A)
private val CYellow     = Color(0xFFFFF9C4)
private val CYellowText = Color(0xFF7B6A00)

// ── Data model ────────────────────────────────────────────────────────────────
private data class ColorChip(val color: Color, val name: String, val hex: String = "")

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
    val eyeColors: List<ColorChip>,
    val metals: List<ColorChip>
)

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
            ColorChip(Color(0xFF2C2C6C), "Navy",    "#2C2C6C"),
            ColorChip(Color(0xFF5C1A6C), "Purple",  "#5C1A6C"),
            ColorChip(Color(0xFF1C1C1C), "Black",   "#1C1C1C"),
            ColorChip(Color(0xFFC0C8D8), "Ash Grey","#C0C8D8"),
            ColorChip(Color(0xFFC83058), "Cool Red","#C83058"),
            ColorChip(Color(0xFF6888B0), "Ice Blue","#6888B0")
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
            ColorChip(Color(0xFF905830), "Auburn"),
            ColorChip(Color(0xFFF0D890), "Light Blonde")
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
        ),
        metals = listOf(
            ColorChip(Color(0xFFFFD700), "Gold",          "#FFD700"),
            ColorChip(Color(0xFFD4A060), "Rose Gold",     "#D4A060"),
            ColorChip(Color(0xFFE8C870), "Yellow Gold",   "#E8C870")
        )
    ),
    SeasonData(
        name = "Soft Summer",
        subSeason = "Soft Summer",
        traits = listOf("Cool", "Soft", "Light"),
        description = "You're a Soft Summer! Cool and muted colours will enhance your natural vibrancy.",
        palette = listOf(
            ColorChip(Color(0xFFD4A5A5), "Dusty Rose",     "#D4A5A5"),
            ColorChip(Color(0xFFA0788C), "Mauve",          "#A0788C"),
            ColorChip(Color(0xFFA3A0B8), "Lavender Grey",  "#A3A0B8"),
            ColorChip(Color(0xFF6B88A0), "Slate Blue",     "#6B88A0"),
            ColorChip(Color(0xFF8AA8C8), "Soft Blue",      "#8AA8C8"),
            ColorChip(Color(0xFF7DB8A8), "Seafoam",        "#7DB8A8"),
            ColorChip(Color(0xFF8AAA78), "Sage",           "#8AAA78"),
            ColorChip(Color(0xFFC0C0C8), "Light Grey",     "#C0C0C8")
        ),
        avoidColors = listOf(
            ColorChip(Color(0xFFC8A030), "Mustard",  "#C8A030"),
            ColorChip(Color(0xFFE07830), "Orange",   "#E07830"),
            ColorChip(Color(0xFFC83030), "True Red", "#C83030"),
            ColorChip(Color(0xFFC040B0), "Fuchsia",  "#C040B0"),
            ColorChip(Color(0xFF90C020), "Lime",     "#90C020"),
            ColorChip(Color(0xFF101010), "Black",    "#101010")
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
            ColorChip(Color(0xFF606878), "Smoky Brown"),
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
        ),
        metals = listOf(
            ColorChip(Color(0xFFC0C0C0), "Silver",     "#C0C0C0"),
            ColorChip(Color(0xFFD4A0A0), "Rose Gold",  "#D4A0A0"),
            ColorChip(Color(0xFFE8E8F0), "White Gold", "#E8E8F0")
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
            ColorChip(Color(0xFFB0C0D8), "Ice Blue",    "#B0C0D8"),
            ColorChip(Color(0xFFD0A0C8), "Pastel Pink", "#D0A0C8"),
            ColorChip(Color(0xFF3848A0), "Royal Blue",  "#3848A0"),
            ColorChip(Color(0xFF101010), "Black",       "#101010"),
            ColorChip(Color(0xFFF0F0F0), "White",       "#F0F0F0"),
            ColorChip(Color(0xFFA040B0), "Bright Violet","#A040B0")
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
            ColorChip(Color(0xFFA07840), "Golden Brown"),
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
        ),
        metals = listOf(
            ColorChip(Color(0xFFFFD700), "Gold",        "#FFD700"),
            ColorChip(Color(0xFFD4A060), "Rose Gold",   "#D4A060"),
            ColorChip(Color(0xFFCD7F32), "Bronze",      "#CD7F32")
        )
    ),
    SeasonData(
        name = "Deep Winter",
        subSeason = "True Winter",
        traits = listOf("Cool", "Clear", "Deep"),
        description = "You're a Deep Winter! High-contrast, vivid and pure cool shades make your features pop.",
        palette = listOf(
            ColorChip(Color(0xFFC02030), "True Red",  "#C02030"),
            ColorChip(Color(0xFF101010), "Black",     "#101010"),
            ColorChip(Color(0xFF182060), "Navy",      "#182060"),
            ColorChip(Color(0xFFF8F8F8), "Pure White","#F8F8F8"),
            ColorChip(Color(0xFFC02880), "Fuchsia",   "#C02880"),
            ColorChip(Color(0xFF5090D0), "Ice Blue",  "#5090D0"),
            ColorChip(Color(0xFF207040), "Emerald",   "#207040"),
            ColorChip(Color(0xFF780030), "Burgundy",  "#780030")
        ),
        avoidColors = listOf(
            ColorChip(Color(0xFFD4A060), "Gold",     "#D4A060"),
            ColorChip(Color(0xFFB89038), "Mustard",  "#B89038"),
            ColorChip(Color(0xFFC87840), "Orange",   "#C87840"),
            ColorChip(Color(0xFFD4B8A0), "Beige",    "#D4B8A0"),
            ColorChip(Color(0xFF709068), "Sage",     "#709068"),
            ColorChip(Color(0xFFD4A5A5), "Dusty Rose","#D4A5A5")
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
            ColorChip(Color(0xFFA8A0B0), "Silver"),
            ColorChip(Color(0xFF604060), "Plum Brown")
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
        ),
        metals = listOf(
            ColorChip(Color(0xFFC0C0C0), "Silver",     "#C0C0C0"),
            ColorChip(Color(0xFFE8E8F0), "White Gold", "#E8E8F0"),
            ColorChip(Color(0xFFB8C0D0), "Platinum",   "#B8C0D0")
        )
    )
)

private val SEASON_LABELS = listOf("Spring", "Summer", "Autumn", "Winter")

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
internal fun ColorAnalysisScreen(
    skinTone: String = "",
    undertone: String = "",
    onBack: () -> Unit = {}
) {
    // Default to Summer (index 1) — matches mockup; warm undertone → Spring (0), cool → Summer (1)
    val defaultIndex = when (undertone.uppercase()) {
        "WARM" -> 0
        "COOL" -> 1
        else   -> 1
    }
    var selectedSeason by remember { mutableIntStateOf(defaultIndex) }
    val season = SEASONS[selectedSeason]
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = 8.dp, bottom = navBottom + 152.dp
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
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = CText, modifier = Modifier.size(22.dp))
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Color Analysis", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = CText))
                        Text("Discover your most flattering colors", style = TextStyle(fontSize = 11.sp, color = CMuted))
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Outlined.Share, "Share", tint = CText, modifier = Modifier.size(22.dp))
                    }
                }
            }

            // ── Best Season card
            item {
                ColorCard {
                    Text("Your Best Season", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CText))
                    Spacer(Modifier.height(12.dp))

                    // Season chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SEASON_LABELS.forEachIndexed { i, label ->
                            val active = i == selectedSeason
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (active) CRose else CMutedBg)
                                    .border(1.dp, if (active) CRose else Color.Transparent, RoundedCornerShape(20.dp))
                                    .clickable { selectedSeason = i }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    label,
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (active) Color.White else CText
                                    )
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            season.name,
                            style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = CText)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CRose)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Seasonal", style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.White))
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Trait pills
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        season.traits.forEach { trait ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CMutedBg)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(trait, style = TextStyle(fontSize = 11.sp, color = CText, fontWeight = FontWeight.Medium))
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        season.description,
                        style = TextStyle(fontSize = 12.sp, color = CMuted, lineHeight = 18.sp)
                    )

                    Spacer(Modifier.height(10.dp))

                    // Sub season row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CMutedBg)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sub Season", style = TextStyle(fontSize = 12.sp, color = CMuted))
                        Spacer(Modifier.weight(1f))
                        Text(season.subSeason, style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CText))
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, null, tint = CMuted, modifier = Modifier.size(12.dp))
                    }

                    Spacer(Modifier.height(8.dp))

                    // Info tip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CYellow)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Outlined.Info, null, tint = CYellowText, modifier = Modifier.size(16.dp))
                        Text(
                            "Tap any season to learn about its characteristics and see example palettes.",
                            style = TextStyle(fontSize = 11.sp, color = CYellowText, lineHeight = 15.sp)
                        )
                    }
                }
            }

            // ── Personal Color Palette
            item {
                ColorCard {
                    SectionTitle("Personal Color Palette")
                    Text("Colors that naturally enhance your features", style = TextStyle(fontSize = 11.sp, color = CMuted))
                    Spacer(Modifier.height(14.dp))
                    SwatchGrid(swatches = season.palette, columns = 4)
                }
            }

            // ── Colors to Avoid
            item {
                ColorCard {
                    SectionTitle("Colors to Avoid")
                    Spacer(Modifier.height(4.dp))
                    SwatchGrid(swatches = season.avoidColors, columns = 4)
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CMutedBg)
                            .padding(10.dp)
                    ) {
                        Text(
                            "These colors may overwhelm your natural tones and create unflattering contrast. Opt for soft, cool, and muted shades instead.",
                            style = TextStyle(fontSize = 11.sp, color = CMuted, lineHeight = 16.sp)
                        )
                    }
                }
            }

            // ── Clothing Colors
            item {
                ColorCard {
                    SectionTitle("Clothing Color Recommendations")
                    Spacer(Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(season.clothingColors) { swatch ->
                            SwatchItem(swatch = swatch, size = 44.dp)
                        }
                    }
                }
            }

            // ── Hair Color Suggestions
            item {
                ColorCard {
                    SectionTitle("Hair Color Suggestions")
                    Spacer(Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(season.hairColors) { swatch ->
                            SwatchItem(swatch = swatch, size = 40.dp)
                        }
                    }
                }
            }

            // ── Makeup Palette
            item {
                ColorCard {
                    SectionTitle("Makeup Palette")
                    Spacer(Modifier.height(12.dp))
                    Text("Lip", style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CMuted))
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(season.lipColors) { swatch ->
                            SwatchItem(swatch = swatch, size = 36.dp)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Eye Shadow", style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CMuted))
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(season.eyeColors) { swatch ->
                            SwatchItem(swatch = swatch, size = 36.dp)
                        }
                    }
                }
            }

            // ── Best Metals for You
            item {
                ColorCard {
                    SectionTitle("Best Metals for You")
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Cool-toned metals enhance your natural glow. Shine, subtle gold, and cool silver suit you best.",
                        style = TextStyle(fontSize = 11.sp, color = CMuted, lineHeight = 16.sp)
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        season.metals.forEach { metal ->
                            SwatchItem(swatch = metal, size = 52.dp, showHex = true)
                        }
                    }
                }
            }

            // ── Save Palette section
            item {
                ColorCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Save Palette to Profile", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CText))
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "Create a shareable profile with your colour season",
                                style = TextStyle(fontSize = 11.sp, color = CMuted, lineHeight = 15.sp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Button(
                            onClick = {},
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEB3B)),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text("Save", style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CDark))
                        }
                    }
                }
            }

        }

        // ── Sticky bottom bar
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
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CRose)
            ) {
                Text("Save Palette to Profile", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White))
            }
        }
    }
}

// ── Shared helpers ────────────────────────────────────────────────────────────

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CText))
}

@Composable
private fun SwatchGrid(swatches: List<ColorChip>, columns: Int) {
    swatches.chunked(columns).forEach { row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            row.forEach { swatch -> SwatchItem(swatch = swatch, size = 48.dp, showHex = true) }
            repeat(columns - row.size) { Spacer(Modifier.size(48.dp)) }
        }
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun SwatchItem(
    swatch: ColorChip,
    size: androidx.compose.ui.unit.Dp,
    showHex: Boolean = false
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
