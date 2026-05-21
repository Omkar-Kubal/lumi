package com.appylab.lumi.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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

private val OnbBackground = Color(0xFFFCFCFC)
private val OnbRose       = Color(0xFFFF637E)
private val OnbRoseCard   = Color(0xFFFFF1F2)
private val OnbText       = Color(0xFF0A0A0A)
private val OnbMuted      = Color(0xFF737373)
private val OnbDotInactive = Color(0xFFE0E0E0)

private const val TOTAL_PAGES  = 8
private const val CURRENT_PAGE = 0

@Composable
fun Onboarding1Screen(
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    val context = LocalContext.current
    val density = context.resources.displayMetrics.density

    val illustrationBitmap = remember {
        val svg = SVG.getFromAsset(context.assets, "onboarding1.svg")
        // SVG viewBox is 464×480 — render at 320×330dp
        val w = (320 * density).toInt()
        val h = (330 * density).toInt()
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        svg.renderToCanvas(android.graphics.Canvas(bmp))
        bmp.asImageBitmap()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OnbBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            // ── Illustration + scan-frame corners ──────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp),
                contentAlignment = Alignment.Center
            ) {
                // Face illustration
                Image(
                    bitmap = illustrationBitmap,
                    contentDescription = "Face illustration",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(width = 320.dp, height = 330.dp)
                )

                // Scan-frame corner brackets overlay
                Canvas(modifier = Modifier.matchParentSize()) {
                    val sw     = 2.5.dp.toPx()
                    val armLen = 24.dp.toPx()
                    val inset  = 28.dp.toPx()
                    val color  = OnbRose

                    // Top-left
                    drawLine(color, Offset(inset, inset), Offset(inset + armLen, inset), sw, StrokeCap.Round)
                    drawLine(color, Offset(inset, inset), Offset(inset, inset + armLen), sw, StrokeCap.Round)
                    // Top-right
                    drawLine(color, Offset(size.width - inset, inset), Offset(size.width - inset - armLen, inset), sw, StrokeCap.Round)
                    drawLine(color, Offset(size.width - inset, inset), Offset(size.width - inset, inset + armLen), sw, StrokeCap.Round)
                    // Bottom-left
                    drawLine(color, Offset(inset, size.height - inset), Offset(inset + armLen, size.height - inset), sw, StrokeCap.Round)
                    drawLine(color, Offset(inset, size.height - inset), Offset(inset, size.height - inset - armLen), sw, StrokeCap.Round)
                    // Bottom-right
                    drawLine(color, Offset(size.width - inset, size.height - inset), Offset(size.width - inset - armLen, size.height - inset), sw, StrokeCap.Round)
                    drawLine(color, Offset(size.width - inset, size.height - inset), Offset(size.width - inset, size.height - inset - armLen), sw, StrokeCap.Round)
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Headline ───────────────────────────────────────────────────
            Text(
                text = "Your face tells a story.",
                style = TextStyle(
                    fontFamily = PoppinsFont,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnbText,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(Modifier.height(6.dp))

            // ── Sub-headline ───────────────────────────────────────────────
            Text(
                text = "LUMI reads it in seconds.",
                style = TextStyle(
                    fontFamily = PoppinsFont,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = OnbMuted,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(Modifier.height(20.dp))

            // ── Quote card ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(OnbRoseCard)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Column {
                    Text(
                        text = "\u201C\u201C",
                        style = TextStyle(
                            fontFamily = PoppinsFont,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnbRose,
                            lineHeight = 18.sp
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Every face is unique.\nYours deserves to be understood.",
                        style = TextStyle(
                            fontFamily = PoppinsFont,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = OnbText,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = buildAnnotatedString {
                            append("— Lumi ")
                            withStyle(SpanStyle(color = OnbRose)) { append("\u2736") }
                        },
                        style = TextStyle(
                            fontFamily = PoppinsFont,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            fontStyle = FontStyle.Italic,
                            color = OnbMuted,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // ── Page dots ──────────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(TOTAL_PAGES) { index ->
                    val isActive = index == CURRENT_PAGE
                    Box(
                        modifier = Modifier
                            .size(if (isActive) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (isActive) OnbRose else OnbDotInactive)
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
                    containerColor = OnbText,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "See what LUMI finds  \u2192",
                    style = TextStyle(
                        fontFamily = PoppinsFont,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.2.sp
                    )
                )
            }

            Spacer(Modifier.height(14.dp))

            // ── Skip ───────────────────────────────────────────────────────
            Text(
                text = "Skip for now",
                style = TextStyle(
                    fontFamily = PoppinsFont,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = OnbMuted
                ),
                modifier = Modifier
                    .clickable(onClick = onSkip)
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFCFCFC)
@Composable
private fun Onboarding1ScreenPreview() {
    LumiTheme {
        Onboarding1Screen(onNext = {}, onSkip = {})
    }
}
