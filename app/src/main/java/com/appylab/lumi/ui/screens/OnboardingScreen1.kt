package com.appylab.lumi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.appylab.lumi.ui.theme.LumiTheme

private val OnbBackground = Color(0xFFFCFCFC)
private val OnbCard = Color(0xFFFFF1F2)
private val OnbTextPrimary = Color(0xFF0A0A0A)
private val OnbTextMuted = Color(0xFF737373)
private val OnbIconTint = Color(0xFF2A2A2A)
private val OnbDotActive = Color(0xFF525252)
private val OnbDotInactive = Color(0xFFD4D4D4)
private val OnbProgressActive = Color(0xFF0A0A0A)
private val OnbProgressInactive = Color(0xFFE0E0E0)

@Composable
fun OnboardingScreen1(
    onGetStarted: () -> Unit = {},
    onSkip: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OnbBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(28.dp))

        // Top heading
        Text(
            text = "Analyze · Discover · Glow Up",
            style = TextStyle(
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnbTextPrimary,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = "Your beauty, personalized by AI",
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = OnbTextMuted,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Feature card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(252.dp),
            shape = RoundedCornerShape(16.dp),
            color = OnbCard
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left features
                Column(
                    modifier = Modifier
                        .width(80.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.Start
                ) {
                    FeatureItem(Icons.Outlined.CameraAlt, "Face\nAnalysis")
                    FeatureItem(Icons.Outlined.Brush, "Makeup\nRecomm.")
                    FeatureItem(Icons.Outlined.Spa, "Skincare\nInsights")
                }

                // Center illustration
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("file:///android_asset/onboarding1_illustration.svg")
                        .decoderFactory(SvgDecoder.Factory())
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )

                // Right features
                Column(
                    modifier = Modifier
                        .width(80.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.End
                ) {
                    FeatureItem(Icons.Outlined.AutoAwesome, "Glow-Up\nScore", alignEnd = true)
                    FeatureItem(Icons.Outlined.Palette, "Color\nAnalysis", alignEnd = true)
                    FeatureItem(Icons.Outlined.Checkroom, "Style\nSuggestions", alignEnd = true)
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Section title
        Text(
            text = "Discover your personalized\nbeauty insights",
            style = TextStyle(
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnbTextPrimary,
                lineHeight = 30.sp
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Description
        Text(
            text = "Our AI analyzes your face, skin, and style to deliver personalized makeup tips, skincare advice, and style recommendations just for you.",
            style = TextStyle(
                fontSize = 13.sp,
                color = OnbTextMuted,
                lineHeight = 20.sp
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Step label + segmented progress
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Step 1 of 3",
                style = TextStyle(fontSize = 11.sp, color = OnbTextMuted)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(2.dp)
                        .background(OnbProgressActive, RoundedCornerShape(1.dp))
                )
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(2.dp)
                        .background(OnbProgressInactive, RoundedCornerShape(1.dp))
                )
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(2.dp)
                        .background(OnbProgressInactive, RoundedCornerShape(1.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Page dots
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(OnbDotActive))
            Spacer(modifier = Modifier.width(6.dp))
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(OnbDotInactive))
            Spacer(modifier = Modifier.width(6.dp))
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(OnbDotInactive))
        }

        Spacer(modifier = Modifier.weight(1f))

        // Get Started
        Button(
            onClick = onGetStarted,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = OnbTextPrimary,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Get Started",
                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Skip
        TextButton(
            onClick = onSkip,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
        ) {
            Text(
                text = "Skip for now",
                style = TextStyle(fontSize = 14.sp, color = OnbTextMuted)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun FeatureItem(
    icon: ImageVector,
    label: String,
    alignEnd: Boolean = false
) {
    Column(
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = OnbIconTint
        )
        Text(
            text = label,
            style = TextStyle(
                fontSize = 9.sp,
                color = OnbTextMuted,
                lineHeight = 12.5.sp,
                textAlign = if (alignEnd) TextAlign.End else TextAlign.Start
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFCFCFC)
@Composable
private fun OnboardingScreen1Preview() {
    LumiTheme {
        OnboardingScreen1()
    }
}
