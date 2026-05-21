package com.appylab.lumi.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appylab.lumi.ui.theme.LumiTheme
import com.appylab.lumi.ui.theme.PoppinsFont

// ─── Colours ─────────────────────────────────────────────────────────────────

private val Ob6Background   = Color(0xFFFCFCFC)
private val Ob6Rose         = Color(0xFFFF637E)
private val Ob6RoseCard     = Color(0xFFFFF1F2)
private val Ob6RoseBorder   = Color(0xFFFFCCD3)
private val Ob6Text         = Color(0xFF0A0A0A)
private val Ob6Muted        = Color(0xFF737373)
private val Ob6ChipBg       = Color(0xFFFFFFFF)
private val Ob6ChipBorder   = Color(0xFFEBEBEB)
private val Ob6DotInactive  = Color(0xFFE0E0E0)
private val Ob6DropdownBg   = Color(0xFFFFFFFF)

private const val OB6_TOTAL   = 8
private const val OB6_CURRENT = 5   // 0-indexed → page 6

// ─── Data ─────────────────────────────────────────────────────────────────────

private val ageRanges  = listOf("Under 18", "18–24", "25–34", "35–44", "45+")
private val skinTypes  = listOf("Oily", "Dry", "Combination", "Normal", "Sensitive")

private data class SkinTone(val name: String, val color: Color)
private val skinTones = listOf(
    SkinTone("Fair",   Color(0xFFF5CBA7)),
    SkinTone("Light",  Color(0xFFE8A87C)),
    SkinTone("Medium", Color(0xFFC68642)),
    SkinTone("Tan",    Color(0xFF8D5524)),
    SkinTone("Deep",   Color(0xFF3D1C02))
)

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun Onboarding6Screen(
    onBack: () -> Unit,
    onNext: (ageRange: String?, skinType: String?, skinTone: String?) -> Unit
) {
    var selectedAge      by remember { mutableStateOf<String?>(null) }
    var ageDropdownOpen  by remember { mutableStateOf(false) }
    var selectedSkinType by remember { mutableStateOf<String?>(null) }
    var selectedToneIdx  by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ob6Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Top bar ────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Ob6Text,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = "6 of 8",
                    style = TextStyle(
                        fontFamily = PoppinsFont,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = Ob6Muted
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = "A few more details to make your results truly personal.",
                style = TextStyle(
                    fontFamily = PoppinsFont,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = Ob6Muted,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(Modifier.height(24.dp))

            // ── Section 1: Age range ───────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = "What\u2019s your age range?",
                    style = TextStyle(
                        fontFamily = PoppinsFont,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Ob6Text
                    )
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "This helps personalise your results.",
                    style = TextStyle(
                        fontFamily = PoppinsFont,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = Ob6Muted
                    )
                )

                Spacer(Modifier.height(12.dp))

                // Dropdown trigger
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, Ob6ChipBorder, RoundedCornerShape(10.dp))
                        .background(Ob6DropdownBg)
                        .clickable { ageDropdownOpen = !ageDropdownOpen }
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedAge ?: "Select your age range",
                        style = TextStyle(
                            fontFamily = PoppinsFont,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (selectedAge != null) Ob6Text else Ob6Muted
                        )
                    )
                    Icon(
                        imageVector = if (ageDropdownOpen)
                            Icons.Filled.KeyboardArrowUp
                        else
                            Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Ob6Muted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Dropdown list
                AnimatedVisibility(
                    visible = ageDropdownOpen,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
                            .clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
                            .background(Ob6DropdownBg)
                    ) {
                        ageRanges.forEachIndexed { index, range ->
                            val isSelected = range == selectedAge
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isSelected) Ob6RoseCard else Ob6DropdownBg)
                                    .clickable {
                                        selectedAge = range
                                        ageDropdownOpen = false
                                    }
                                    .padding(horizontal = 14.dp, vertical = 14.dp)
                            ) {
                                Text(
                                    text = range,
                                    style = TextStyle(
                                        fontFamily = PoppinsFont,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) Ob6Rose else Ob6Text
                                    )
                                )
                            }
                            if (index < ageRanges.lastIndex) {
                                Divider(color = Ob6ChipBorder, thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Section 2: Skin type ───────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = "What\u2019s your skin type?",
                    style = TextStyle(
                        fontFamily = PoppinsFont,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Ob6Text
                    )
                )

                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    skinTypes.forEach { type ->
                        val isSelected = type == selectedSkinType
                        Text(
                            text = type,
                            style = TextStyle(
                                fontFamily = PoppinsFont,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Ob6Text
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(if (isSelected) Ob6Rose else Ob6ChipBg)
                                .border(
                                    1.dp,
                                    if (isSelected) Ob6Rose else Ob6ChipBorder,
                                    RoundedCornerShape(50.dp)
                                )
                                .clickable { selectedSkinType = type }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Section 3: Skin tone ───────────────────────────────────────
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "What\u2019s your skin tone?",
                    style = TextStyle(
                        fontFamily = PoppinsFont,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Ob6Text
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // Swatch row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    skinTones.forEachIndexed { index, tone ->
                        val isSelected = index == selectedToneIdx
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .then(
                                    if (isSelected)
                                        Modifier.border(3.dp, Ob6Rose, CircleShape)
                                    else
                                        Modifier.border(1.5.dp, Color.Transparent, CircleShape)
                                )
                                .padding(if (isSelected) 3.dp else 0.dp)
                                .clip(CircleShape)
                                .background(tone.color)
                                .clickable { selectedToneIdx = index }
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Selected tone label
                Text(
                    text = selectedToneIdx?.let { skinTones[it].name } ?: "",
                    style = TextStyle(
                        fontFamily = PoppinsFont,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Ob6Text,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Every skin tone is stunning.\nLUMI is built for all of them.",
                    style = TextStyle(
                        fontFamily = PoppinsFont,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Italic,
                        color = Ob6Muted,
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp
                    )
                )
            }

            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(20.dp))

            // ── Page dots ──────────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(OB6_TOTAL) { index ->
                    val isActive = index == OB6_CURRENT
                    Box(
                        modifier = Modifier
                            .size(if (isActive) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (isActive) Ob6Rose else Ob6DotInactive)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── CTA button ─────────────────────────────────────────────────
            Button(
                onClick = {
                    onNext(selectedAge, selectedSkinType, selectedToneIdx?.let { skinTones[it].name })
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Ob6Text,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Almost there  \u2192",
                    style = TextStyle(
                        fontFamily = PoppinsFont,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.2.sp
                    )
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFFCFCFC)
@Composable
private fun Onboarding6ScreenPreview() {
    LumiTheme {
        Onboarding6Screen(onBack = {}, onNext = { _, _, _ -> })
    }
}
