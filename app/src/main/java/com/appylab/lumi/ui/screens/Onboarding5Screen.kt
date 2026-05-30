package com.appylab.lumi.ui.screens

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

private val Ob5Background   = Color(0xFFFCFCFC)
private val Ob5Rose         = Color(0xFFFF637E)
private val Ob5RoseCard     = Color(0xFFFFF1F2)
private val Ob5RoseBorder   = Color(0xFFFFCCD3)
private val Ob5Text         = Color(0xFF0A0A0A)
private val Ob5Muted        = Color(0xFF737373)
private val Ob5ChipBg       = Color(0xFFFFFFFF)
private val Ob5ChipBorder   = Color(0xFFEBEBEB)
private val Ob5DotInactive  = Color(0xFFE0E0E0)
private val Ob5StepLine     = Color(0xFFFFCCD3)

private const val OB5_TOTAL   = 8
private const val OB5_CURRENT = 4   // 0-indexed → page 5

// ─── Data model ──────────────────────────────────────────────────────────────

private enum class BeautyGoal(val label: String, val emoji: String) {
    Makeup("Makeup", "\uD83D\uDC84"),
    Skincare("Skincare", "\uD83D\uDCA7"),
    Style("Style", "\uD83D\uDC57"),
    GlowUp("Glow-Up", "\u2728"),
    ColorAnalysis("Color Analysis", "\uD83C\uDFA8"),
    AllAbove("All of the above", "\u2795")
}

private enum class SkinConcern(val label: String, val emoji: String) {
    Acne("Acne", "\uD83D\uDD34"),
    Dryness("Dryness", "\uD83D\uDCA7"),
    Oiliness("Oiliness", "\uD83D\uDCE6"),
    DarkSpots("Dark Spots", "\u26AB"),
    Texture("Texture", "\uD83C\uDF00"),
    Sensitivity("Sensitivity", "\uD83C\uDF3F")
}

// ─── Progress stepper ────────────────────────────────────────────────────────

private val stepLabels = listOf("Intro", "Proof", "Proof", "Goals", "Access")
private const val ACTIVE_STEP = 3   // "Goals" is current (0-indexed)

@Composable
private fun StepProgressBar(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            stepLabels.forEachIndexed { index, _ ->
                val filled = index <= ACTIVE_STEP

                // Dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (filled) Ob5Rose else Ob5DotInactive)
                )

                // Connector line (except after last)
                if (index < stepLabels.lastIndex) {
                    val lineFilled = index < ACTIVE_STEP
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(if (lineFilled) Ob5StepLine else Ob5DotInactive)
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            stepLabels.forEach { label ->
                Text(
                    text = label,
                    style = TextStyle(
                        fontFamily = PoppinsFont,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Normal,
                        color = Ob5Muted
                    )
                )
            }
        }
    }
}

// ─── Screen ──────────────────────────────────────────────────────────────────

@Composable
fun Onboarding5Screen(
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val selectedGoals    = remember { mutableStateSetOf<BeautyGoal>() }
    val selectedConcerns = remember { mutableStateSetOf<SkinConcern>() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ob5Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // ── Fixed top bar ──────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Ob5Text,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                StepProgressBar(modifier = Modifier.weight(1f))
            }

            Text(
                text = "Let\u2019s personalise LUMI for you. 30 seconds.",
                style = TextStyle(
                    fontFamily = PoppinsFont,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = Ob5Muted,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(8.dp))

            // ── Scrollable content ─────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(16.dp))

                // ── Section 1: Beauty goals ────────────────────────────────
                SectionHeader(
                    title = "What are your beauty goals?",
                    subtitle = "Select all that apply",
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(Modifier.height(12.dp))

                SelectionGrid(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    items = BeautyGoal.entries,
                    selectedItems = selectedGoals,
                    label = { it.label },
                    emoji = { it.emoji },
                    onToggle = { goal ->
                        if (goal == BeautyGoal.AllAbove) {
                            if (BeautyGoal.AllAbove in selectedGoals) {
                                selectedGoals.clear()
                            } else {
                                selectedGoals.clear()
                                selectedGoals.addAll(BeautyGoal.entries)
                            }
                        } else {
                            if (goal in selectedGoals) selectedGoals.remove(goal)
                            else selectedGoals.add(goal)
                        }
                    }
                )

                Spacer(Modifier.height(24.dp))

                // ── Section 2: Skin concerns ───────────────────────────────
                SectionHeader(
                    title = "What are your skin concerns?",
                    subtitle = "Select all that apply \u2014 no judgement here.",
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(Modifier.height(12.dp))

                SelectionGrid(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    items = SkinConcern.entries,
                    selectedItems = selectedConcerns,
                    label = { it.label },
                    emoji = { it.emoji },
                    onToggle = { concern ->
                        if (concern in selectedConcerns) selectedConcerns.remove(concern)
                        else selectedConcerns.add(concern)
                    }
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Every skin type is valid. Every concern is common.",
                    style = TextStyle(
                        fontFamily = PoppinsFont,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Italic,
                        color = Ob5Muted,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                )

                Spacer(Modifier.height(12.dp))
            }

            // ── Fixed bottom nav ───────────────────────────────────────────
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(OB5_TOTAL) { index ->
                            val isActive = index == OB5_CURRENT
                            Box(
                                modifier = Modifier
                                    .size(if (isActive) 8.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(if (isActive) Ob5Rose else Ob5DotInactive)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { onNext() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Ob5Text,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Continue  \u2192",
                        style = TextStyle(
                            fontFamily = PoppinsFont,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.2.sp
                        )
                    )
                }
                Spacer(Modifier.height(8.dp))
                if (selectedGoals.isEmpty()) {
                    Text(
                        text = "At least one goal required",
                        style = TextStyle(
                            fontFamily = PoppinsFont,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = Ob5Muted,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ─── Reusable composables ─────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = TextStyle(
                fontFamily = PoppinsFont,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Ob5Text
            )
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = subtitle,
            style = TextStyle(
                fontFamily = PoppinsFont,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Ob5Muted
            )
        )
    }
}

@Composable
private fun <T> SelectionGrid(
    modifier: Modifier = Modifier,
    items: List<T>,
    selectedItems: Set<T>,
    label: (T) -> String,
    emoji: (T) -> String,
    onToggle: (T) -> Unit
) {
    // Chunk into rows of 2
    val rows = items.chunked(2)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { item ->
                    SelectionChip(
                        modifier = Modifier.weight(1f),
                        label = label(item),
                        emoji = emoji(item),
                        selected = item in selectedItems,
                        onClick = { onToggle(item) }
                    )
                }
                // Fill empty slot if odd number
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SelectionChip(
    modifier: Modifier = Modifier,
    label: String,
    emoji: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgColor     = if (selected) Ob5RoseCard   else Ob5ChipBg
    val borderColor = if (selected) Ob5Rose       else Ob5ChipBorder
    val textColor   = if (selected) Ob5Text       else Ob5Text

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Leading: checkmark circle if selected, emoji icon otherwise
        if (selected) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Ob5Rose),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }
        } else {
            Text(
                text = emoji,
                fontSize = 16.sp
            )
        }

        Text(
            text = label,
            style = TextStyle(
                fontFamily = PoppinsFont,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) Ob5Rose else textColor
            )
        )
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFFCFCFC)
@Composable
private fun Onboarding5ScreenPreview() {
    LumiTheme {
        Onboarding5Screen(onBack = {}, onNext = {})
    }
}
