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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PRose      = Color(0xFFFF637E)
private val PDark      = Color(0xFF1A1A2E)
private val PCard      = Color(0xFFFFFFFF)
private val PMuted     = Color(0xFF737373)
private val PBackground = Color(0xFFFFF5F7)

private val FEATURES = listOf(
    "Unlimited face scans",
    "Full feature detail analysis",
    "Glow-Up image generation",
    "Color season & palette analysis",
    "Progress tracker with history",
    "Personalized skincare tips",
    "Priority support"
)

@Composable
fun PaywallScreen(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Close button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = PMuted
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Hero icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        brush = Brush.radialGradient(listOf(PRose.copy(alpha = 0.2f), Color.Transparent)),
                        shape = RoundedCornerShape(40.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = PRose,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Lumi PRO",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = PDark
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Unlock your full beauty potential\nwith unlimited AI-powered analysis",
                fontSize = 15.sp,
                color = PMuted,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(Modifier.height(28.dp))

            // Feature list
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                colors = CardDefaults.cardColors(containerColor = PCard),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Everything in PRO",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PDark
                    )
                    Spacer(Modifier.height(16.dp))
                    FEATURES.forEach { feature ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                                tint = PRose,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = feature,
                                fontSize = 14.sp,
                                color = PDark,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Pricing
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                colors = CardDefaults.cardColors(containerColor = PRose.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$4.99",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = PRose
                    )
                    Text(
                        text = "per month",
                        fontSize = 14.sp,
                        color = PMuted
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "or $39.99 / year  •  Save 33%",
                        fontSize = 13.sp,
                        color = PMuted
                    )
                }
            }
        }

        // Sticky CTA
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(PBackground)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { /* TODO: purchase flow */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PRose),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Start Free Trial",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PMuted)
            ) {
                Text(
                    text = "Maybe Later",
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Cancel anytime  •  Restore purchases",
                fontSize = 11.sp,
                color = PMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}
