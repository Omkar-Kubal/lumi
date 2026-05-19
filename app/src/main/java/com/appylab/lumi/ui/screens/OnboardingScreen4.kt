package com.appylab.lumi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appylab.lumi.ui.theme.PoppinsFont

private val Ob4Background  = Color(0xFFFCFCFC)
private val Ob4Rose        = Color(0xFFFF637E)
private val Ob4TextPrimary = Color(0xFF0A0A0A)
private val Ob4TextMuted   = Color(0xFF737373)
private val Ob4CardBorder  = Color(0xFFFFCCD3)
private val Ob4Divider     = Color(0xFFE0E0E0)

private data class Testimonial(val initial: String, val tag: String, val quote: String)

private val testimonials = listOf(
    Testimonial("A", "Soft Summer",  "\"Finally know what colors suit me.\""),
    Testimonial("M", "Tan · 40+",    "\"My skin has never looked better.\""),
    Testimonial("S", "Deep · 40ms",  "\"Understood my face shape for the first time.\"")
)

@Composable
fun OnboardingScreen4(
    onBack: () -> Unit = {},
    onContinue: () -> Unit = {}
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
                        tint = Ob4TextPrimary
                    )
                }
                Text(
                    text = "4 of 8",
                    style = TextStyle(fontFamily = PoppinsFont, 
                        fontSize = 13.sp,
                        color = Ob4TextMuted
                    )
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "50,000+ women have\ndiscovered their best look",
                style = TextStyle(fontFamily = PoppinsFont, 
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ob4TextPrimary,
                    lineHeight = 32.sp
                ),
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(20.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp)
            ) {
                items(testimonials) { t ->
                    TestimonialCard(t)
                }
            }

            Spacer(Modifier.height(20.dp))

            // Stats row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(value = "50K+", label = "scans")
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(Ob4Divider)
                )
                StatItem(value = "4.8★", label = "rating")
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(Ob4Divider)
                )
                StatItem(value = "8 sec", label = "avg")
            }

            Spacer(Modifier.height(24.dp))

            QuoteCard(
                text = "There's no one way to be beautiful.\nThere's only your way.",
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(24.dp))

            OnboardingPageDots(
                totalPages = 8,
                currentPage = 4,
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
                .background(Ob4Background)
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
                    containerColor = Ob4TextPrimary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "I want this →",
                    style = TextStyle(fontFamily = PoppinsFont, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@Composable
private fun TestimonialCard(t: Testimonial) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        modifier = Modifier
            .width(200.dp)
            .border(1.dp, Ob4CardBorder, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Ob4Rose),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = t.initial,
                    style = TextStyle(fontFamily = PoppinsFont, 
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = t.tag,
                style = TextStyle(fontFamily = PoppinsFont, fontSize = 11.sp, color = Ob4TextMuted)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = t.quote,
                style = TextStyle(fontFamily = PoppinsFont, 
                    fontSize = 12.sp,
                    color = Ob4TextPrimary,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 18.sp
                )
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(5) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Ob4Rose
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = TextStyle(fontFamily = PoppinsFont, 
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Ob4TextPrimary
            )
        )
        Text(
            text = label,
            style = TextStyle(fontFamily = PoppinsFont, fontSize = 11.sp, color = Ob4TextMuted)
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
