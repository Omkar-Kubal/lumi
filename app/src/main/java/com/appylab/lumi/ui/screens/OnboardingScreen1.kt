package com.appylab.lumi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.appylab.lumi.ui.theme.PoppinsFont

private val Ob1Background  = Color(0xFFFCFCFC)
private val Ob1Rose        = Color(0xFFFF637E)
private val Ob1RoseCard    = Color(0xFFFFF1F2)
private val Ob1TextPrimary = Color(0xFF0A0A0A)
private val Ob1TextMuted   = Color(0xFF737373)

@Composable
fun OnboardingScreen1(
    onGetStarted: () -> Unit = {},
    onSkip: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ob1Background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))

        FaceScanIllustration(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        )

        Spacer(Modifier.height(28.dp))

        Text(
            text = "Your face tells a story.",
            style = TextStyle(fontFamily = PoppinsFont, 
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Ob1TextPrimary,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "LUMI reads it in seconds.",
            style = TextStyle(fontFamily = PoppinsFont, 
                fontSize = 15.sp,
                color = Ob1TextMuted,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        QuoteCard(
            text = "Every face is unique.\nYours deserves to be understood.",
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(Modifier.weight(1f))

        OnboardingPageDots(
            totalPages = 8,
            currentPage = 1,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onGetStarted,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Ob1TextPrimary,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "See what LUMI finds →",
                style = TextStyle(fontFamily = PoppinsFont, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            )
        }

        Spacer(Modifier.height(12.dp))

        TextButton(
            onClick = onSkip,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = "Skip for now",
                style = TextStyle(fontFamily = PoppinsFont, fontSize = 13.sp, color = Ob1TextMuted)
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun FaceScanIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Ob1RoseCard)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("file:///android_asset/onboarding1_illustration.svg")
                .decoderFactory(SvgDecoder.Factory())
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun QuoteCard(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFFF1F2)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Text(
                "❝",
                style = TextStyle(fontFamily = PoppinsFont, 
                    fontSize = 22.sp,
                    color = Color(0xFFFF637E),
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp
                )
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text,
                    style = TextStyle(fontFamily = PoppinsFont, 
                        fontSize = 13.sp,
                        color = Color(0xFF0A0A0A),
                        lineHeight = 20.sp,
                        fontStyle = FontStyle.Italic
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "— Lumi ✦",
                    style = TextStyle(fontFamily = PoppinsFont, fontSize = 11.sp, color = Color(0xFFFF637E))
                )
            }
        }
    }
}
