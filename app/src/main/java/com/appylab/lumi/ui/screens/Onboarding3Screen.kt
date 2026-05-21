package com.appylab.lumi.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.caverock.androidsvg.SVG

private val Ob3Background  = Color(0xFFFCFCFC)
private val Ob3Rose        = Color(0xFFFF637E)
private val Ob3RoseCard    = Color(0xFFFFF1F2)
private val Ob3RoseLight   = Color(0xFFFFD6DC)
private val Ob3Text        = Color(0xFF0A0A0A)
private val Ob3Muted       = Color(0xFF737373)
private val Ob3DotInactive = Color(0xFFE0E0E0)
private val Ob3QuoteCard   = Color(0xFFFFFFFF)

private const val OB3_TOTAL   = 8
private const val OB3_CURRENT = 2   // 0-indexed → page 3

@Composable
fun Onboarding3Screen(
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    val density = context.resources.displayMetrics.density

    // SVG viewBox 464×416 — render wide enough to fill the illustration card
    val illustrationBitmap = remember {
        val svg = SVG.getFromAsset(context.assets, "onboarding3.svg")
        val w = (360 * density).toInt()
        val h = (322 * density).toInt()   // preserve 464:416 ratio at 360dp wide
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        svg.renderToCanvas(android.graphics.Canvas(bmp))
        bmp.asImageBitmap()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ob3Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
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
                        tint = Ob3Text,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = "3 of 8",
                    style = TextStyle(
                        fontFamily = PoppinsFont,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = Ob3Muted
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Illustration card with decorative sparkles ─────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(288.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Ob3RoseCard),
                contentAlignment = Alignment.BottomCenter
            ) {
                // SVG illustration — bottom-aligned so head fills nicely
                Image(
                    bitmap = illustrationBitmap,
                    contentDescription = "Portrait illustration",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(288.dp)
                )

                // Decorative sparkle dots scattered around
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val sparklePositions = listOf(
                        Offset(size.width * 0.08f, size.height * 0.18f),
                        Offset(size.width * 0.88f, size.height * 0.12f),
                        Offset(size.width * 0.92f, size.height * 0.42f),
                        Offset(size.width * 0.06f, size.height * 0.55f),
                        Offset(size.width * 0.82f, size.height * 0.72f),
                        Offset(size.width * 0.14f, size.height * 0.80f)
                    )
                    val sparkleSizes = listOf(8f, 6f, 9f, 5f, 7f, 6f)
                    sparklePositions.forEachIndexed { i, pos ->
                        val r = sparkleSizes[i] * density
                        drawSparkle4(Ob3Rose.copy(alpha = 0.75f), pos, r)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── 3-column quote cards ───────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Card 1
                QuoteCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    bodyText = buildAnnotatedString {
                        append("A glow-up isn\u2019t transformation. It\u2019s becoming ")
                        withStyle(
                            SpanStyle(
                                color = Ob3Rose,
                                fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Italic
                            )
                        ) { append("more you.") }
                    }
                )

                // Card 2 — center, slightly more prominent
                QuoteCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    bodyText = buildAnnotatedString {
                        append("You were ")
                        withStyle(
                            SpanStyle(
                                color = Ob3Rose,
                                fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Italic
                            )
                        ) { append("beautiful") }
                        append(" before any app told you so.")
                    },
                    elevated = true
                )

                // Card 3
                QuoteCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    bodyText = buildAnnotatedString {
                        append("Your \u2018flaws\u2019 are just features the world hasn\u2019t learned to ")
                        withStyle(
                            SpanStyle(
                                color = Ob3Rose,
                                fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Italic
                            )
                        ) { append("celebrate") }
                        append(" yet.")
                    }
                )
            }

            Spacer(Modifier.height(14.dp))

            // ── Attribution ────────────────────────────────────────────────
            Text(
                text = "LUMI was built to help you see what was always there.",
                style = TextStyle(
                    fontFamily = PoppinsFont,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    fontStyle = FontStyle.Italic,
                    color = Ob3Muted,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(Modifier.weight(1f))

            // ── Page dots ──────────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(OB3_TOTAL) { index ->
                    val isActive = index == OB3_CURRENT
                    Box(
                        modifier = Modifier
                            .size(if (isActive) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (isActive) Ob3Rose else Ob3DotInactive)
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
                    containerColor = Ob3Text,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "I\u2019m ready  \u2192",
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

// ─── Quote card ───────────────────────────────────────────────────────────────

@Composable
private fun QuoteCard(
    modifier: Modifier = Modifier,
    bodyText: androidx.compose.ui.text.AnnotatedString,
    elevated: Boolean = false
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (elevated) Ob3RoseCard else Ob3QuoteCard
            )
            .then(
                if (!elevated) Modifier else Modifier
            )
            .padding(10.dp)
    ) {
        Column {
            Text(
                text = "\u201C\u201C",
                style = TextStyle(
                    fontFamily = PoppinsFont,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ob3Rose,
                    lineHeight = 12.sp
                )
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = bodyText,
                style = TextStyle(
                    fontFamily = PoppinsFont,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = Ob3Text,
                    lineHeight = 16.sp
                )
            )
        }
    }
}

// ─── 4-pointed sparkle canvas helper ─────────────────────────────────────────

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSparkle4(
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

@Preview(showBackground = true, backgroundColor = 0xFFFCFCFC)
@Composable
private fun Onboarding3ScreenPreview() {
    LumiTheme {
        Onboarding3Screen(onBack = {}, onNext = {})
    }
}
