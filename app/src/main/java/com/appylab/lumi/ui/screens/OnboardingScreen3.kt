package com.appylab.lumi.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.appylab.lumi.ui.theme.PoppinsFont

private val Ob3Background  = Color(0xFFFCFCFC)
private val Ob3Rose        = Color(0xFFFF637E)
private val Ob3RoseCard    = Color(0xFFFFF1F2)
private val Ob3TextPrimary = Color(0xFF0A0A0A)
private val Ob3TextMuted   = Color(0xFF737373)
private val Ob3CardBorder  = Color(0xFFFFCCD3)

@Composable
fun OnboardingScreen3(
    onBack: () -> Unit = {},
    onContinue: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ob3Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 88.dp)
        ) {
            // Top bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = Ob3TextPrimary
                    )
                }
                Text(
                    text = "3 of 8",
                    style = TextStyle(fontFamily = PoppinsFont, 
                        fontSize = 13.sp,
                        color = Ob3TextMuted
                    )
                )
            }

            Spacer(Modifier.height(8.dp))

            InspirationHero(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(20.dp))

            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InspirationQuoteBox(
                    buildAnnotatedString {
                        append("A glow-up isn't transformation.\nIt's becoming ")
                        withStyle(SpanStyle(color = Ob3Rose, fontStyle = FontStyle.Italic)) {
                            append("more you")
                        }
                        append(".")
                    }
                )
                InspirationQuoteBox(
                    buildAnnotatedString {
                        append("You were ")
                        withStyle(SpanStyle(color = Ob3Rose, fontStyle = FontStyle.Italic)) {
                            append("beautiful")
                        }
                        append("\nbefore any app\ntold you so.")
                    }
                )
                InspirationQuoteBox(
                    buildAnnotatedString {
                        append("Your 'flaws' are just features\nthe world hasn't learned to ")
                        withStyle(SpanStyle(color = Ob3Rose, fontStyle = FontStyle.Italic)) {
                            append("celebrate")
                        }
                        append(" yet.")
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "LUMI was built to help you see what was always there.",
                style = TextStyle(fontFamily = PoppinsFont, 
                    fontSize = 11.sp,
                    color = Ob3TextMuted,
                    textAlign = TextAlign.Center,
                    fontStyle = FontStyle.Italic
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(24.dp))

            OnboardingPageDots(
                totalPages = 8,
                currentPage = 3,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }

        // Fixed bottom overlay
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Ob3Background)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Ob3TextPrimary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "I'm ready →",
                    style = TextStyle(fontFamily = PoppinsFont, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@Composable
private fun InspirationHero(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFFF1F2))
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("file:///android_asset/onboarding3_illustration.svg")
                .decoderFactory(SvgDecoder.Factory())
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun InspirationQuoteBox(text: AnnotatedString) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Ob3CardBorder, RoundedCornerShape(10.dp))
    ) {
        Text(
            text = text,
            style = TextStyle(fontFamily = PoppinsFont, 
                fontSize = 12.sp,
                color = Ob3TextPrimary,
                lineHeight = 18.sp
            ),
            modifier = Modifier.padding(12.dp)
        )
    }
}
