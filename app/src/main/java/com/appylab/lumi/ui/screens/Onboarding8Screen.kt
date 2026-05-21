package com.appylab.lumi.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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

// ─── Colours ─────────────────────────────────────────────────────────────────

private val Ob8Background = Color(0xFFFCFCFC)
private val Ob8Rose       = Color(0xFFFF637E)
private val Ob8RoseCard   = Color(0xFFFFF1F2)
private val Ob8RoseLight  = Color(0xFFFFD6DC)
private val Ob8Text       = Color(0xFF0A0A0A)
private val Ob8Muted      = Color(0xFF737373)
private val Ob8BadgeBg    = Color(0xFFFFF1F2)

// ─── Sparkle positions (relative 0..1 within the decoration area) ─────────────

private data class SparkleSpec(val rx: Float, val ry: Float, val radius: Float, val alpha: Float)

private val sparkleSpecs = listOf(
    // top cluster
    SparkleSpec(0.50f, 0.08f, 12f, 1.0f),
    SparkleSpec(0.22f, 0.18f,  8f, 0.9f),
    SparkleSpec(0.78f, 0.16f, 10f, 0.9f),
    SparkleSpec(0.38f, 0.05f,  5f, 0.7f),
    SparkleSpec(0.65f, 0.04f,  4f, 0.6f),
    // left arc
    SparkleSpec(0.08f, 0.35f, 14f, 1.0f),
    SparkleSpec(0.14f, 0.58f,  6f, 0.7f),
    SparkleSpec(0.05f, 0.72f,  4f, 0.5f),
    // right arc
    SparkleSpec(0.90f, 0.30f,  9f, 0.85f),
    SparkleSpec(0.85f, 0.52f, 13f, 1.0f),
    SparkleSpec(0.93f, 0.68f,  5f, 0.6f),
    // small scatter
    SparkleSpec(0.30f, 0.28f,  4f, 0.5f),
    SparkleSpec(0.72f, 0.38f,  4f, 0.55f),
    SparkleSpec(0.18f, 0.82f,  5f, 0.4f),
    SparkleSpec(0.82f, 0.82f,  4f, 0.4f),
)

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun Onboarding8Screen(
    userName: String = "Ayesha",
    onStartScan: () -> Unit,
    onExploreFirst: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ob8Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            // ── Sparkle decoration + avatar ────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                // Scattered sparkles drawn on Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    sparkleSpecs.forEach { spec ->
                        val cx = size.width  * spec.rx
                        val cy = size.height * spec.ry
                        val r  = spec.radius * density
                        drawSparkle4pt(Ob8Rose.copy(alpha = spec.alpha), Offset(cx, cy), r)
                    }
                }

                // Avatar circle — centered at bottom of the decoration zone
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Ob8Rose),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.firstOrNull()?.uppercaseChar()?.toString() ?: "A",
                        style = TextStyle(
                            fontFamily = PoppinsFont,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Welcome headline ───────────────────────────────────────────
            Text(
                text = buildAnnotatedString {
                    append("Welcome, $userName. ")
                    withStyle(SpanStyle(color = Ob8Rose)) { append("\u2736") }
                },
                style = TextStyle(
                    fontFamily = PoppinsFont,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ob8Text,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Your personalised beauty profile is ready.",
                style = TextStyle(
                    fontFamily = PoppinsFont,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = Ob8Muted,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 40.dp)
            )

            Spacer(Modifier.height(24.dp))

            // ── Quote card ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Ob8RoseCard)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "\u201C\u201C",
                        style = TextStyle(
                            fontFamily = PoppinsFont,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ob8Rose,
                            lineHeight = 18.sp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "You were beautiful before LUMI told you so.\nWe\u2019re just here to help you see it.",
                        style = TextStyle(
                            fontFamily = PoppinsFont,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Ob8Text,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = buildAnnotatedString {
                            append("— Lumi ")
                            withStyle(SpanStyle(color = Ob8Rose)) { append("\u2736") }
                        },
                        style = TextStyle(
                            fontFamily = PoppinsFont,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            fontStyle = FontStyle.Italic,
                            color = Ob8Muted,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Achievement badges ─────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    "Goals saved",
                    "Skin profile ready",
                    "LUMI activated"
                ).forEach { label ->
                    AchievementBadge(label = label)
                }
            }

            Spacer(Modifier.weight(1f))

            // ── CTA button — rose primary ──────────────────────────────────
            Button(
                onClick = onStartScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Ob8Rose,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.size(10.dp))
                Text(
                    text = "Start my first scan  \u2192",
                    style = TextStyle(
                        fontFamily = PoppinsFont,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.2.sp
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Explore first",
                style = TextStyle(
                    fontFamily = PoppinsFont,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = Ob8Muted
                ),
                modifier = Modifier
                    .clickable(onClick = onExploreFirst)
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            )

            Spacer(Modifier.height(12.dp))
        }
    }
}

// ─── Achievement badge ────────────────────────────────────────────────────────

@Composable
private fun AchievementBadge(label: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(Ob8BadgeBg)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = PoppinsFont,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = Ob8Muted
            )
        )
        Text(
            text = "\u2713",
            style = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Ob8Rose
            )
        )
    }
}

// ─── Sparkle helper ───────────────────────────────────────────────────────────

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSparkle4pt(
    color: Color,
    center: Offset,
    radius: Float
) {
    val inner = radius * 0.22f
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        quadraticTo(center.x + inner, center.y - inner, center.x + radius, center.y)
        quadraticTo(center.x + inner, center.y + inner, center.x, center.y + radius)
        quadraticTo(center.x - inner, center.y + inner, center.x - radius, center.y)
        quadraticTo(center.x - inner, center.y - inner, center.x, center.y - radius)
        close()
    }
    drawPath(path, color)
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFFCFCFC)
@Composable
private fun Onboarding8ScreenPreview() {
    LumiTheme {
        Onboarding8Screen(
            userName = "Ayesha",
            onStartScan = {},
            onExploreFirst = {}
        )
    }
}
