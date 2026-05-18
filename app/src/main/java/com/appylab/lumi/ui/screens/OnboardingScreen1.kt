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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

private val OnbBackground    = Color(0xFFFCFCFC)
private val OnbCard          = Color(0xFFFFF1F2)
private val OnbTextPrimary   = Color(0xFF0A0A0A)
private val OnbTextMuted     = Color(0xFF737373)
private val OnbIconTint      = Color(0xFF2A2A2A)

@Composable
fun OnboardingScreen1(
    onGetStarted: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OnbBackground)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))

        // ── Top bar ───────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Step 1 of 3",
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnbTextPrimary
                )
            )
        }

        Spacer(Modifier.height(12.dp))

        // ── Step progress indicator (shared across all 3 screens) ─────────────
        OnboardingStepIndicator(
            currentStep = 1,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Heading ───────────────────────────────────────────────────────
            Text(
                text = "Analyze · Discover · Glow Up",
                style = TextStyle(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnbTextPrimary,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(Modifier.height(5.dp))

            Text(
                text = "Your beauty, personalized by AI",
                style = TextStyle(
                    fontSize = 13.sp,
                    color = OnbTextMuted,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(Modifier.height(20.dp))

            // ── Feature card ──────────────────────────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                shape = RoundedCornerShape(16.dp),
                color = OnbCard
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

                    Column(
                        modifier = Modifier
                            .width(80.dp)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.End
                    ) {
                        FeatureItem(Icons.Outlined.AutoAwesome, "Glow-Up\nScore",     alignEnd = true)
                        FeatureItem(Icons.Outlined.Palette,     "Color\nAnalysis",    alignEnd = true)
                        FeatureItem(Icons.Outlined.Checkroom,   "Style\nSuggestions", alignEnd = true)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Copy ─────────────────────────────────────────────────────────
            Text(
                text = "Discover your personalized\nbeauty insights",
                style = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnbTextPrimary,
                    lineHeight = 30.sp,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Our AI analyzes your face, skin, and style to deliver personalized makeup tips, skincare advice, and style recommendations just for you.",
                style = TextStyle(
                    fontSize = 13.sp,
                    color = OnbTextMuted,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.weight(1f))

        // ── CTA ───────────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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

            Spacer(Modifier.height(8.dp))
        }
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
