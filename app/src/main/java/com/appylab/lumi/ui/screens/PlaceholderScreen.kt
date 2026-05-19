package com.appylab.lumi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appylab.lumi.ui.theme.PoppinsFont

private val PHBackground  = Color(0xFFFCFCFC)
private val PHRose        = Color(0xFFFF637E)
private val PHTextPrimary = Color(0xFF0A0A0A)
private val PHTextMuted   = Color(0xFF737373)
private val PHBorder      = Color(0xFFEDEDED)

@Composable
fun PlaceholderScreen(
    title: String,
    subtitle: String = "This screen is coming soon",
    onBack: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PHBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header
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
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = PHTextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = title,
                    style = TextStyle(fontFamily = PoppinsFont, 
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PHTextPrimary
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Center content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF1F2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = PHRose,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFFF1F2)
                ) {
                    Text(
                        text = "Coming soon",
                        style = TextStyle(fontFamily = PoppinsFont, 
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PHRose
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = title,
                    style = TextStyle(fontFamily = PoppinsFont, 
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PHTextPrimary
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = subtitle,
                    style = TextStyle(fontFamily = PoppinsFont, 
                        fontSize = 13.sp,
                        color = PHTextMuted,
                        lineHeight = 20.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
