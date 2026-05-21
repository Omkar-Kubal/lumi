package com.appylab.lumi.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
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

private val Ob2Background  = Color(0xFFFCFCFC)
private val Ob2Rose        = Color(0xFFFF637E)
private val Ob2RoseCard    = Color(0xFFFFF1F2)
private val Ob2RoseIcon    = Color(0xFFFFE4E8)
private val Ob2Text        = Color(0xFF0A0A0A)
private val Ob2Muted       = Color(0xFF737373)
private val Ob2DotInactive = Color(0xFFE0E0E0)

private const val OB2_TOTAL  = 8
private const val OB2_CURRENT = 1   // 0-indexed → page 2

@Composable
fun Onboarding2Screen(
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ob2Background)
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
                        tint = Ob2Text,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = "2 of 8",
                    style = TextStyle(
                        fontFamily = PoppinsFont,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = Ob2Muted
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Headline ───────────────────────────────────────────────────
            Text(
                text = "One scan. Everything changes.",
                style = TextStyle(
                    fontFamily = PoppinsFont,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ob2Text,
                    textAlign = TextAlign.Start,
                    lineHeight = 32.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(24.dp))

            // ── Feature grid 2×2 ──────────────────────────────────────────
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FeatureTile(
                        modifier = Modifier.weight(1f),
                        title = "Face & Skin Analysis",
                        description = "Know your features inside out",
                        drawIcon = { drawFaceIcon(Ob2Rose) }
                    )
                    FeatureTile(
                        modifier = Modifier.weight(1f),
                        title = "AI Glow-Up Score",
                        description = "Track your progress over time",
                        drawIcon = { drawSparkleIcon(Ob2Rose) }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FeatureTile(
                        modifier = Modifier.weight(1f),
                        title = "Color Season",
                        description = "Wear what actually suits you",
                        drawIcon = { drawColorSeasonIcon(Ob2Rose) }
                    )
                    FeatureTile(
                        modifier = Modifier.weight(1f),
                        title = "Feature Detail",
                        description = "Understand every detail of your face",
                        drawIcon = { drawEyeIcon(Ob2Rose) }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Quote card ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Ob2RoseCard)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Column {
                    Text(
                        text = "\u201C\u201C",
                        style = TextStyle(
                            fontFamily = PoppinsFont,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ob2Rose,
                            lineHeight = 18.sp
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Beauty isn\u2019t one size.\nIt never was.",
                        style = TextStyle(
                            fontFamily = PoppinsFont,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Ob2Text,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = buildAnnotatedString {
                            append("— Lumi ")
                            withStyle(SpanStyle(color = Ob2Rose)) { append("\u2736") }
                        },
                        style = TextStyle(
                            fontFamily = PoppinsFont,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            fontStyle = FontStyle.Italic,
                            color = Ob2Muted,
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
                repeat(OB2_TOTAL) { index ->
                    val isActive = index == OB2_CURRENT
                    Box(
                        modifier = Modifier
                            .size(if (isActive) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (isActive) Ob2Rose else Ob2DotInactive)
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
                    containerColor = Ob2Text,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Sounds good  \u2192",
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

// ─── Feature tile ────────────────────────────────────────────────────────────

@Composable
private fun FeatureTile(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    drawIcon: DrawScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Ob2RoseCard)
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Icon circle
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Ob2RoseIcon),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(28.dp)) {
                drawIcon()
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = title,
            style = TextStyle(
                fontFamily = PoppinsFont,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Ob2Text,
                lineHeight = 18.sp
            )
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = description,
            style = TextStyle(
                fontFamily = PoppinsFont,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = Ob2Muted,
                lineHeight = 16.sp
            )
        )
    }
}

// ─── Canvas icon drawers ─────────────────────────────────────────────────────

/** Face outline with 4 scan-dot markers */
private fun DrawScope.drawFaceIcon(color: Color) {
    val sw = 1.8.dp.toPx()
    val cx = size.width / 2f
    val cy = size.height / 2f
    val r  = size.minDimension * 0.42f

    // Head oval
    drawOval(
        color = color,
        topLeft = Offset(cx - r * 0.75f, cy - r),
        size = Size(r * 1.5f, r * 2f),
        style = Stroke(width = sw)
    )
    // Left eye dot
    drawCircle(color, radius = sw * 1.2f, center = Offset(cx - r * 0.28f, cy - r * 0.2f))
    // Right eye dot
    drawCircle(color, radius = sw * 1.2f, center = Offset(cx + r * 0.28f, cy - r * 0.2f))
    // Nose dot
    drawCircle(color, radius = sw * 0.9f, center = Offset(cx, cy + r * 0.15f))
    // Smile arc
    val smilePath = Path().apply {
        val sr = r * 0.38f
        arcTo(
            rect = Rect(center = Offset(cx, cy + r * 0.35f), radius = sr),
            startAngleDegrees = 20f,
            sweepAngleDegrees = 140f,
            forceMoveTo = true
        )
    }
    drawPath(smilePath, color, style = Stroke(width = sw, cap = StrokeCap.Round))
}

/** 4-pointed sparkle star */
private fun DrawScope.drawSparkleIcon(color: Color) {
    val cx    = size.width / 2f
    val cy    = size.height / 2f
    val outer = size.minDimension * 0.45f
    val inner = outer * 0.22f

    val path = Path().apply {
        moveTo(cx, cy - outer)
        quadraticTo(cx + inner, cy - inner, cx + outer, cy)
        quadraticTo(cx + inner, cy + inner, cx, cy + outer)
        quadraticTo(cx - inner, cy + inner, cx - outer, cy)
        quadraticTo(cx - inner, cy - inner, cx, cy - outer)
        close()
    }
    drawPath(path, color)

    // Small center circle cutout effect — draw inner circle in background color
    drawCircle(Ob2RoseIcon, radius = inner * 1.2f, center = Offset(cx, cy))
}

/** Color-season palette: 4 arc segments in a circle */
private fun DrawScope.drawColorSeasonIcon(color: Color) {
    val sw = 1.8.dp.toPx()
    val cx = size.width / 2f
    val cy = size.height / 2f
    val r  = size.minDimension * 0.44f

    // Outer circle
    drawCircle(color, radius = r, center = Offset(cx, cy), style = Stroke(width = sw))
    // Cross dividers
    drawLine(color, Offset(cx - r, cy), Offset(cx + r, cy), sw, StrokeCap.Round)
    drawLine(color, Offset(cx, cy - r), Offset(cx, cy + r), sw, StrokeCap.Round)
    // Fill two opposite quadrants with a small filled arc hint
    val fillR = r * 0.45f
    drawCircle(color.copy(alpha = 0.35f), radius = fillR, center = Offset(cx - r * 0.4f, cy - r * 0.4f))
    drawCircle(color.copy(alpha = 0.35f), radius = fillR, center = Offset(cx + r * 0.4f, cy + r * 0.4f))
}

/** Eye shape with radiating lashes */
private fun DrawScope.drawEyeIcon(color: Color) {
    val sw = 1.8.dp.toPx()
    val cx = size.width / 2f
    val cy = size.height / 2f
    val rx = size.width * 0.44f
    val ry = size.height * 0.28f

    // Eye outline (two arcs forming lens shape)
    val eyePath = Path().apply {
        moveTo(cx - rx, cy)
        quadraticTo(cx, cy - ry * 1.8f, cx + rx, cy)
        quadraticTo(cx, cy + ry * 1.8f, cx - rx, cy)
        close()
    }
    drawPath(eyePath, color, style = Stroke(width = sw, cap = StrokeCap.Round))

    // Iris
    drawCircle(color, radius = ry * 0.85f, center = Offset(cx, cy), style = Stroke(width = sw))

    // Pupil dot
    drawCircle(color, radius = ry * 0.3f, center = Offset(cx, cy))

    // Top lashes (3 short lines)
    val lashSw = sw * 0.9f
    for (i in -1..1) {
        val lx = cx + i * rx * 0.38f
        val topY = cy - ry * 1.5f
        drawLine(color, Offset(lx, topY), Offset(lx, topY - ry * 0.45f), lashSw, StrokeCap.Round)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFCFCFC)
@Composable
private fun Onboarding2ScreenPreview() {
    LumiTheme {
        Onboarding2Screen(onBack = {}, onNext = {})
    }
}
