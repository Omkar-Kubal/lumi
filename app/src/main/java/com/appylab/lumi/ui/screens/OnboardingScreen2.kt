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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Visibility
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appylab.lumi.ui.theme.PoppinsFont

private val Ob2Background  = Color(0xFFFCFCFC)
private val Ob2Rose        = Color(0xFFFF637E)
private val Ob2RoseCard    = Color(0xFFFFF1F2)
private val Ob2TextPrimary = Color(0xFF0A0A0A)
private val Ob2TextMuted   = Color(0xFF737373)
private val Ob2CardBorder  = Color(0xFFFFCCD3)

private data class Ob2FeatureTile(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val subtitle: String
)

private val featureTiles = listOf(
    Ob2FeatureTile(Icons.Outlined.Face,        "Face & Skin Analysis", "Know your features inside out"),
    Ob2FeatureTile(Icons.Outlined.AutoAwesome, "AI Glow-Up Score",     "Track your progress over time"),
    Ob2FeatureTile(Icons.Outlined.Palette,     "Color Season",         "Wear what actually suits you"),
    Ob2FeatureTile(Icons.Outlined.Visibility,  "Feature Detail",       "Understand every detail of your face")
)

@Composable
fun OnboardingScreen2(
    onBack: () -> Unit = {},
    onContinue: () -> Unit = {}
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
                        tint = Ob2TextPrimary
                    )
                }
                Text(
                    text = "2 of 8",
                    style = TextStyle(fontFamily = PoppinsFont, 
                        fontSize = 13.sp,
                        color = Ob2TextMuted
                    )
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "One scan. Everything changes.",
                style = TextStyle(fontFamily = PoppinsFont, 
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ob2TextPrimary
                ),
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(20.dp))

            FeatureGrid(modifier = Modifier.padding(horizontal = 24.dp))

            Spacer(Modifier.height(24.dp))

            QuoteCard(
                text = "Beauty isn't one size.\nIt never was.",
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(24.dp))

            OnboardingPageDots(
                totalPages = 8,
                currentPage = 2,
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
                .background(Ob2Background)
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
                    containerColor = Ob2TextPrimary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Sounds good →",
                    style = TextStyle(fontFamily = PoppinsFont, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@Composable
private fun FeatureGrid(modifier: Modifier = Modifier) {
    val rows = featureTiles.chunked(2)
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        rows.forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { tile ->
                    FeatureTileCard(
                        tile = tile,
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(2 - rowItems.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun FeatureTileCard(
    tile: Ob2FeatureTile,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        modifier = modifier.border(1.dp, Ob2CardBorder, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Ob2RoseCard),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tile.icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = Ob2Rose
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = tile.title,
                style = TextStyle(fontFamily = PoppinsFont, 
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ob2TextPrimary
                )
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = tile.subtitle,
                style = TextStyle(fontFamily = PoppinsFont, 
                    fontSize = 11.sp,
                    color = Ob2TextMuted,
                    lineHeight = 16.sp
                )
            )
        }
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
