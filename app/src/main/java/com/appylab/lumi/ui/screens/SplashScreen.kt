package com.appylab.lumi.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appylab.lumi.R
import com.appylab.lumi.ui.theme.LumiTheme

private val Rose = Color(0xFFFF637E)
private val Background = Color(0xFFFCFCFC)
private val TextPrimary = Color(0xFF0A0A0A)
private val TextMuted = Color(0xFF737373)
private val DotActive = Color(0xFF525252)
private val DotInactive = Color(0xFFD4D4D4)

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Face illustration — corners, sparkle, and decorations are baked into the SVG
            Image(
                painter = painterResource(id = R.drawable.ic_lumi_face),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(width = 220.dp, height = 236.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // LUMI wordmark with rose sparkle superscript
            Box {
                Text(
                    text = "LUMI",
                    style = TextStyle(
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 9.sp,
                        color = TextPrimary
                    )
                )
                Canvas(
                    modifier = Modifier
                        .size(11.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 11.dp, y = (-1).dp)
                ) {
                    drawSparkle(Rose, size)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tagline
            Text(
                text = "Analyze  ·  Discover  ·  Glow Up",
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.4.sp,
                    color = TextMuted
                )
            )
        }

        // Onboarding page indicator dots
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 44.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(DotActive)
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(DotInactive)
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(DotInactive)
            )
        }
    }
}

private fun DrawScope.drawSparkle(color: Color, size: Size) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val r = size.minDimension / 2f
    val inner = r * 0.22f

    val path = Path().apply {
        moveTo(cx, cy - r)
        quadraticBezierTo(cx + inner, cy - inner, cx + r, cy)
        quadraticBezierTo(cx + inner, cy + inner, cx, cy + r)
        quadraticBezierTo(cx - inner, cy + inner, cx - r, cy)
        quadraticBezierTo(cx - inner, cy - inner, cx, cy - r)
        close()
    }
    drawPath(path, color)
}

@Preview(showBackground = true, backgroundColor = 0xFFFCFCFC)
@Composable
private fun SplashScreenPreview() {
    LumiTheme {
        SplashScreen()
    }
}
