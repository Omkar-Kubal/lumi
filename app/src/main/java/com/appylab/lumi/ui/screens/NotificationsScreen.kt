package com.appylab.lumi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val NBackground  = Color(0xFFFCFCFC)
private val NText        = Color(0xFF0A0A0A)
private val NMuted       = Color(0xFF737373)
private val NRose        = Color(0xFFFF637E)

@Composable
fun NotificationsScreen(
    onBack: () -> Unit = {},
    onOpen: () -> Unit = {}
) {
    // Caller should call homeViewModel.onBellTapped() before opening this screen
    // to clear the badge; this effect provides a safety net if they forget.
    LaunchedEffect(Unit) { onOpen() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NBackground)
    ) {
        // Top bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 8.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = NText,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                "Notifications",
                style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = NText),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Empty state
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFFF5F5F5), androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.NotificationsNone,
                    contentDescription = null,
                    tint = NRose,
                    modifier = Modifier.size(38.dp)
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "You're all caught up",
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NText)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Scan reminders and personalised updates will appear here.",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = NMuted,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}
