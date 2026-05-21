package com.appylab.lumi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appylab.lumi.ui.theme.LumiTheme
import com.appylab.lumi.ui.theme.PoppinsFont

private val Ob4Background  = Color(0xFFFCFCFC)
private val Ob4Rose        = Color(0xFFFF637E)
private val Ob4RoseCard    = Color(0xFFFFF1F2)
private val Ob4Text        = Color(0xFF0A0A0A)
private val Ob4Muted       = Color(0xFF737373)
private val Ob4DotInactive = Color(0xFFE0E0E0)
private val Ob4CardBg      = Color(0xFFFFFFFF)
private val Ob4CardBorder  = Color(0xFFFFCCD3)

private val AvatarPurple = Color(0xFF5E4C8A)
private val AvatarRose   = Color(0xFFFF637E)
private val AvatarTeal   = Color(0xFF3DA89A)

private const val OB4_TOTAL   = 8
private const val OB4_CURRENT = 3   // 0-indexed → page 4

private data class Testimonial(
    val initial: String,
    val avatarColor: Color,
    val tag: String,
    val quote: String
)

private val testimonials = listOf(
    Testimonial("A", AvatarPurple, "Soft Summer ✦",
        "\"Finally know what colors suit me.\""),
    Testimonial("M", AvatarRose,   "74 · 18 · ✦",
        "\"My skin has never looked better.\""),
    Testimonial("S", AvatarTeal,   "Oval · 4mo",
        "\"Understood my shape for the first time.\"")
)

@Composable
fun Onboarding4Screen(
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ob4Background)
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
                        tint = Ob4Text,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = "4 of 8",
                    style = TextStyle(
                        fontFamily = PoppinsFont,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = Ob4Muted
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Headline ───────────────────────────────────────────────────
            Text(
                text = "50,000+ women have discovered their best look",
                style = TextStyle(
                    fontFamily = PoppinsFont,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ob4Text,
                    lineHeight = 34.sp,
                    textAlign = TextAlign.Start
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(24.dp))

            // ── Testimonial cards ──────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                testimonials.forEach { t ->
                    TestimonialCard(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        testimonial = t
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Testimonial pager dots (3 cards indicator) ─────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val isActive = index == 0
                    Box(
                        modifier = Modifier
                            .size(if (isActive) 6.dp else 5.dp)
                            .clip(CircleShape)
                            .background(if (isActive) Ob4Rose else Ob4DotInactive)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Stats row ──────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(
                    icon = Icons.Outlined.DocumentScanner,
                    label = "50K+ scans"
                )
                StatDivider()
                StatItem(
                    icon = Icons.Outlined.Star,
                    label = "4.8★ rating"
                )
                StatDivider()
                StatItem(
                    icon = Icons.Outlined.AccessTime,
                    label = "8 sec avg"
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Quote card ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Ob4RoseCard)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Column {
                    Text(
                        text = "\u201C\u201C",
                        style = TextStyle(
                            fontFamily = PoppinsFont,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ob4Rose,
                            lineHeight = 18.sp
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "There\u2019s no one way to be beautiful.\nThere\u2019s only your way.",
                        style = TextStyle(
                            fontFamily = PoppinsFont,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Ob4Text,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = buildAnnotatedString {
                            append("— Lumi ")
                            withStyle(SpanStyle(color = Ob4Rose)) { append("\u2736") }
                        },
                        style = TextStyle(
                            fontFamily = PoppinsFont,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            fontStyle = FontStyle.Italic,
                            color = Ob4Muted,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(20.dp))

            // ── Page dots ──────────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(OB4_TOTAL) { index ->
                    val isActive = index == OB4_CURRENT
                    Box(
                        modifier = Modifier
                            .size(if (isActive) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (isActive) Ob4Rose else Ob4DotInactive)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── CTA button ─────────────────────────────────────────────────
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Ob4Text,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "I want this  \u2192",
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

// ─── Testimonial card ─────────────────────────────────────────────────────────

@Composable
private fun TestimonialCard(
    modifier: Modifier = Modifier,
    testimonial: Testimonial
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Ob4RoseCard)
            .padding(10.dp)
    ) {
        // Avatar + tag row
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(testimonial.avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = testimonial.initial,
                    style = TextStyle(
                        fontFamily = PoppinsFont,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                )
            }
            Spacer(Modifier.width(6.dp))
            Text(
                text = testimonial.tag,
                style = TextStyle(
                    fontFamily = PoppinsFont,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = Ob4Muted
                )
            )
        }

        Spacer(Modifier.height(8.dp))

        // Quote
        Text(
            text = testimonial.quote,
            style = TextStyle(
                fontFamily = PoppinsFont,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = Ob4Text,
                lineHeight = 15.sp
            ),
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.height(8.dp))

        // 5 stars
        Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
            repeat(5) {
                Text(
                    text = "★",
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = Ob4Rose
                    )
                )
            }
        }
    }
}

// ─── Stat item ────────────────────────────────────────────────────────────────

@Composable
private fun StatItem(icon: ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Ob4Muted,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = label,
            style = TextStyle(
                fontFamily = PoppinsFont,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Ob4Muted
            )
        )
    }
}

@Composable
private fun StatDivider() {
    Text(
        text = "·",
        style = TextStyle(
            fontSize = 16.sp,
            color = Ob4DotInactive
        )
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFCFCFC)
@Composable
private fun Onboarding4ScreenPreview() {
    LumiTheme {
        Onboarding4Screen(onBack = {}, onNext = {})
    }
}
