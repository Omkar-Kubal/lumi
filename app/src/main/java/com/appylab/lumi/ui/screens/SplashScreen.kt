package com.appylab.lumi.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appylab.lumi.ui.theme.LumiTheme
import com.appylab.lumi.ui.theme.PoppinsFont
import com.caverock.androidsvg.SVG

private val Rose = Color(0xFFFF637E)
private val Background = Color(0xFFFCFCFC)
private val TextPrimary = Color(0xFF0A0A0A)
private val TextMuted = Color(0xFF737373)

@Composable
fun SplashScreen() {
    val context = LocalContext.current
    val density = context.resources.displayMetrics.density
    val splashBitmap = remember {
        val svg = SVG.getFromAsset(context.assets, "splashscreen.svg")
        val w = (220 * density).toInt()
        val h = (236 * density).toInt()
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        svg.renderToCanvas(android.graphics.Canvas(bmp))
        bmp.asImageBitmap()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                bitmap = splashBitmap,
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
                        fontFamily = PoppinsFont,
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

            Text(
                text = "Analyze  ·  Discover  ·  Glow Up",
                style = TextStyle(
                    fontFamily = PoppinsFont,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.4.sp,
                    color = TextMuted
                )
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
